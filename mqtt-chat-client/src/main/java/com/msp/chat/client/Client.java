package com.msp.chat.client;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.msp.chat.client.commons.Constants;
import com.msp.chat.client.commons.MessageIDGenerator;
import com.msp.chat.client.exceptions.ConnectionException;
import com.msp.chat.client.exceptions.PublishException;
import com.msp.chat.client.exceptions.SubscribeException;
import com.msp.chat.core.mqtt.MQTTException;
import com.msp.chat.core.mqtt.proto.messages.*;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.Channel;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */

public final class Client {

    final static int DEFAULT_RETRIES = 3;
    final static int RETRIES_QOS_GT0 = 1;
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private static final long CONNECT_TIMEOUT = 10 * 1000L; // 3 seconds
    private static final long SUBACK_TIMEOUT = 4 * 1000L;
    private static final int KEEPALIVE_SECS = 60; //ping 1분에 한번씩 보냄
    private static final int NUM_SCHEDULER_TIMER_THREAD = 1;
    private int m_connectRetries = DEFAULT_RETRIES;
    private String m_hostname;
    private int m_port;

    private CountDownLatch m_connectBarrier;
    private CountDownLatch m_subscribeBarrier;
    private int m_receivedSubAckMessageID;
    private byte m_returnCode;
    //TODO synchronize the access
    //Refact the da model should be a list of callback for each topic
    private static Map<String, IPublishCallback> m_subscribersList = new ConcurrentHashMap<String, IPublishCallback>();
    private ScheduledExecutorService m_scheduler;
    private ScheduledFuture m_pingerHandler;
    private String m_macAddress;
    private MessageIDGenerator m_messageIDGenerator = new MessageIDGenerator();

    private String m_clientID;

    private Bootstrap b;
    private Channel channel;
     EventLoopGroup group;
    
    final Runnable pingerDeamon = new Runnable() {
        public void run() {
            LOG.info("Pingreq sent");
            //send a ping req
            channel.writeAndFlush(new PingReqMessage());
        }
    };

    public Client(String host, int port) {
        m_hostname = host;
        m_port = port;
//        init();
        sslInit();
    }
    
    /**
     * Constructor in which the user could provide it's own ClientID
     */
    public Client(String host, int port, String clientID) {
        this(host, port);
        m_clientID = clientID;
    }

    protected void init() {
        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ClientMQTTInitializer(this));
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        } finally {
            // Shut down executor threads to exit.
//            group.shutdownGracefully();
        }
        m_scheduler = Executors.newScheduledThreadPool(NUM_SCHEDULER_TIMER_THREAD);

//        m_macAddress = readMACAddress();
        m_macAddress = "MAC";
        LOG.info("MACADDRESS :"+m_macAddress);
    }

    protected void sslInit() {

        SslContext sslCtx = null;
        try {
            sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
        } catch (SSLException e) {
            e.printStackTrace();
        }

        group = new NioEventLoopGroup();
        try {
            b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ClientSslMQTTInitializer(this,sslCtx));
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        } finally {
            // Shut down executor threads to exit.
//            group.shutdownGracefully();
        }
        m_scheduler = Executors.newScheduledThreadPool(NUM_SCHEDULER_TIMER_THREAD);
        
//        m_macAddress = readMACAddress();
        m_macAddress = "MAC";
        LOG.info("MACADDRESS :"+m_macAddress);
    }

    /**
     * Connects to the server with clean session set to true, do not maintains
     * client topic subscriptions
     */
    public void connect() throws InterruptedException {
        connect(false, null);
    }

    public void connect(boolean cleanSession, String m_clientID) throws InterruptedException {

        ChannelFuture channelFuture = b.connect(m_hostname, m_port);
        channelFuture.awaitUninterruptibly();

        // Now we are sure the future is completed.
        assert channelFuture.isDone();

        if (channelFuture.isCancelled()) { //사용자의 의해 Connection 취소
            throw new InterruptedException("Connection attempt cancelled by user");
            // Connection attempt cancelled by user
        } else if (!channelFuture.isSuccess()) { // 실패
            throw new InterruptedException(channelFuture.cause().toString());
        } else { //Connection 성공
            System.out.println("###### 접속성공 ");
            // Connection established successfully
            channel = channelFuture.sync().channel();
            m_connectBarrier = new CountDownLatch(1);
            //send a message over the session
            ConnectMessage connMsg = new ConnectMessage();
            connMsg.setKeepAlive(KEEPALIVE_SECS);
            if (m_clientID == null) {
                m_clientID = generateClientID();
            }
            connMsg.setClientID(m_clientID);
            connMsg.setCleanSession(cleanSession);
            connMsg.setWillTopic("/topic");
            channel.writeAndFlush(connMsg);

            //suspend until the server respond with CONN_ACK
            boolean unlocked = false;
            try {
                unlocked = m_connectBarrier.await(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS); //TODO parametrize
            } catch (InterruptedException ex) {
                throw new InterruptedException(ex.getMessage());
            }
            //if not arrive into certain limit, raise an error
            if (!unlocked) {
                throw new InterruptedException("Connection timeout elapsed unless server responded with a CONN_ACK");
            }

            //also raise an error when CONN_ACK is received with some error code inside
            if (m_returnCode != ConnAckMessage.CONNECTION_ACCEPTED) {
                String errMsg;
                switch (m_returnCode) {
                    case ConnAckMessage.UNNACEPTABLE_PROTOCOL_VERSION:
                        errMsg = "Unacceptable protocol version";
                        break;
                    case ConnAckMessage.IDENTIFIER_REJECTED:
                        errMsg = "Identifier rejected";
                        break;
                    case ConnAckMessage.SERVER_UNAVAILABLE:
                        errMsg = "Server unavailable";
                        break;
                    case ConnAckMessage.BAD_USERNAME_OR_PASSWORD:
                        errMsg = "Bad username or password";
                        break;
                    case ConnAckMessage.NOT_AUTHORIZED:
                        errMsg = "Not authorized";
                        break;
                    default:
                        errMsg = "Not idetified erro code " + m_returnCode;
                }
                throw new InterruptedException(errMsg);
            }
            updatePinger();
        }
    }

    /**
     * Publish to the connected server the payload message to the given topic.
     * It's admitted to publish a 0 -length payload.
     */
    public void publish(String topic, byte[] payload) throws PublishException {
        String str = new String(payload,0,payload.length);
        LOG.info("#############publish 11111: topic:" + topic + "  payload: " + str);
        publish(topic, payload, false);
    }

    /**
     * Publish by default with QoS 0
     * */
    public void publish(String topic, byte[] payload, boolean retain) throws PublishException {
        LOG.info("#############publish 22222: topic:"+topic);
        publish(topic, payload, AbstractMessage.QOSType.MOST_ONE, retain);  // 전달 여부 확인 하지 않고 한번
//        publish(topic, payload, AbstractMessage.QOSType.LEAST_ONE, retain);  //전달여부를 확인 하고 여러번 일수 있음.
//        publish(topic, payload, AbstractMessage.QOSType.EXACTLY_ONCE, retain); // 정확희 한번
    }

    public void publish(String topic, byte[] payload, AbstractMessage.QOSType qos, boolean retain) throws PublishException {
        LOG.info("#############publish : topic:"+topic);
        PublishMessage msg = new PublishMessage();
        msg.setRetainFlag(retain);
        msg.setTopicName(topic);
        ByteBuffer publishBuff = ByteBuffer.wrap(payload);
        msg.setPayload(publishBuff);
        int messageID = m_messageIDGenerator.next();
        msg.setMessageID(messageID);
        //Untill the server could handle all the Qos 2 level
        if (qos != AbstractMessage.QOSType.MOST_ONE) {
            msg.setQos(qos);
            try {
                if(qos == AbstractMessage.QOSType.EXACTLY_ONCE){
                    manageSendQoS2(msg);
                }else {
                    manageSendQoS1(msg);
                }
            } catch (Throwable ex) {
                throw new MQTTException(ex);
            }
        } else {
            //QoS 0 case
            msg.setQos(AbstractMessage.QOSType.MOST_ONE);
            ChannelFuture cf = channel.writeAndFlush(msg);
            try {
                cf.await();
            } catch (InterruptedException ex) {
                LOG.debug(null, ex);
                throw new PublishException(ex);
            }

            Throwable ex = cf.cause();
            if (ex != null) {
                throw new PublishException(ex);
            }
        }

//        updatePinger();
    }

/*    public void sendPushMsg(List<String> revUsers, String message, AbstractMessage.QOSType qos, boolean retain){
        ByteBuffer sendMsgBuf = ByteBuffer.wrap(message.getBytes());
        SendPushMessage sendPushMessage = new SendPushMessage();
        sendPushMessage.setPayload(sendMsgBuf);
        sendPushMessage.setRetainFlag(retain);
        sendPushMessage.setSendPushUsers(revUsers);

        int messageID = m_messageIDGenerator.next();
        sendPushMessage.setMessageID(messageID);
        //Untill the server could handle all the Qos 2 level
        if (qos != AbstractMessage.QOSType.MOST_ONE) {
            sendPushMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);
            try {
                manageSendQoS1(sendPushMessage);
            } catch (Throwable ex) {
                throw new MQTTException(ex);
            }
        } else {
            //QoS 0 case
            sendPushMessage.setQos(AbstractMessage.QOSType.MOST_ONE);
            ChannelFuture cf = channel.writeAndFlush(sendPushMessage);
            try {
                cf.await();
            } catch (InterruptedException ex) {
                LOG.debug(null, ex);
                throw new PublishException(ex);
            }

            Throwable ex = cf.cause();
            if (ex != null) {
                throw new PublishException(ex);
            }
        }
    }*/

    public synchronized void  subscribe(String[] topic, IPublishCallback publishCallback) {
//        LOG.info("#####################subscribe invoked topic.length:"+topic.length);
        SubscribeMessage msg = new SubscribeMessage();
        for(int i=0; i<topic.length; i++){
            //구독요청메세지 작성
            LOG.info("#####################구독신청한 토픽:"+topic[i]);
            SubscribeMessage.Couple couple=new SubscribeMessage.Couple((byte) AbstractMessage.QOSType.MOST_ONE.ordinal(), topic[i]);
            msg.addSubscription(couple);
//            msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
            int messageID = m_messageIDGenerator.next();
            msg.setMessageID(messageID);
        }

        register(msg, publishCallback);   //구독후 응답 받을 콜백 등록
//        m_inflightIDs.add(messageID);

        try {
//            manageSendQoS0(msg);
            manageSendQoS1(msg);         // 서버로 구독 메세지전송
        } catch(Throwable ex) {
            //in case errors arise, remove the registration because the subscription
            // hasn't get well
            register(msg, publishCallback);   //구독후 응답 받을 콜백 등록
            throw new MQTTException(ex);
        }

//        updatePinger();
    }

//    public void addGroupSubscribe(String groupName, List<String> clients){
//        GroupSubscribeMessage groupSubscribeMessage = new GroupSubscribeMessage();
//        System.out.println("$$$$$$$$$$$ 사이즈:"+clients.size());
//        for(int i=0; i<clients.size(); i++){
//            groupSubscribeMessage.addClientID(clients.get(i));
//        }
//        groupSubscribeMessage.setM_topic(groupName);
//
//        channel.writeAndFlush(groupSubscribeMessage);
//    }
    
    
    public void unsubscribe(String... topics) {
        LOG.info("unsubscribe invoked");
        UnsubscribeMessage msg = new UnsubscribeMessage();
        for (String topic : topics) {
            msg.addTopic(topic);
        }
        msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
        int messageID = m_messageIDGenerator.next();
        msg.setMessageID(messageID);
//        m_inflightIDs.add(messageID);
//        register(topic, publishCallback);
        try {
            manageSendQoS1(msg);
        } catch(Throwable ex) {
            //in case errors arise, remove the registration because the subscription
            // hasn't get well
//            unregister(topic);
            throw new MQTTException(ex);
        }
        
        for (String topic : topics) {
            unregister(topic);
        }

//        register(topic, publishCallback);
        updatePinger();
    }
    private void manageSendQoS0(MessageIDMessage msg) throws Throwable{
        int messageID = msg.getMessageID();

        ChannelFuture cf = channel.writeAndFlush(msg);

        try {
            cf.await();
        } catch (InterruptedException ex) {
            LOG.debug(null, ex);
            throw new PublishException(ex);
        }

        Throwable ex = cf.cause();
        if (ex != null) {
            throw new PublishException(ex);
        }
        LOG.info("#############message sent");
    }

    private void manageSendQoS1(MessageIDMessage msg) throws Throwable{
        int messageID = msg.getMessageID();
        boolean unlocked = false;
        for (int retries = 0; retries < RETRIES_QOS_GT0 || !unlocked; retries++) {
            LOG.info("manageSendQoS1 retry " + retries);
            if (retries > 0) {
                msg.setDupFlag(true);
            }

            ChannelFuture cf = channel.writeAndFlush(msg);
            cf.await();
            LOG.info("#############message sent");

            Throwable ex = cf.cause();
            if (ex != null) {
                throw ex;
            }

            //wait for the SubAck
            m_subscribeBarrier = new CountDownLatch(1);

            //suspend until the server respond with CONN_ACK
            LOG.info("##########################subscribe waiting for suback");
            unlocked = m_subscribeBarrier.await(SUBACK_TIMEOUT, TimeUnit.MILLISECONDS); //TODO parametrize
        }

        //if not arrive into certain limit, raise an error
        if (!unlocked) {
            throw new SubscribeException(String.format("Server doesn't replyed with a SUB_ACK after %d replies", RETRIES_QOS_GT0));
        } else {
            //check if message ID match
            if (m_receivedSubAckMessageID != messageID) {
                throw new SubscribeException(String.format("Server replyed with "
                + "a broken MessageID in SUB_ACK, expected %d but received %d", 
                messageID, m_receivedSubAckMessageID));
            }
        }
    }

    private void manageSendQoS2(MessageIDMessage msg) throws Throwable{
        int messageID = msg.getMessageID();
        ChannelFuture cf = channel.writeAndFlush(msg);
        cf.await();
        LOG.info("#############message sent");

    }

    /**
     * TODO extract this SPI method in a SPI
     */
    protected void connectionAckCallback(byte returnCode) {
        LOG.info("connectionAckCallback invoked");
        m_returnCode = returnCode;
        m_connectBarrier.countDown();
    }

    protected void subscribeAckCallback(int messageID) {
        LOG.info("subscribeAckCallback invoked");
        m_subscribeBarrier.countDown();
        m_receivedSubAckMessageID = messageID;
    }
    
    void unsubscribeAckCallback(int messageID) {
        LOG.info("unsubscribeAckCallback invoked");
        //NB we share the barrier because in futur will be a single barrier for all
        m_subscribeBarrier.countDown();
        m_receivedSubAckMessageID = messageID;
    }

    void publishAckCallback(Integer messageID) {
        LOG.info("publishAckCallback invoked : messageID"+messageID);
        m_subscribeBarrier.countDown();
        m_receivedSubAckMessageID = messageID;
    }

    protected void publishCallback(String topic, byte[] payload) {
        String str = new String(payload,0,payload.length);
        LOG.info("################ publishCallback 메세지1:"+str);
        IPublishCallback callback = m_subscribersList.get(topic);
        if (callback == null) {
            String msg = String.format("Can't find any publish callback fr topic %s", topic);
            LOG.error(msg);
            throw new MQTTException(msg);
        }

        callback.published(topic, str);

    }

    protected void publishCallback(PublishMessage publishMessage){
        ByteBuffer buffer = publishMessage.getPayload();
        String str = new String(buffer.array());
//        LOG.info("################ publishCallback 메세지2:"+str);
        IPublishCallback callback = m_subscribersList.get(publishMessage.getTopicName());
        if (callback == null) {
            String msg = String.format("Can't find any publish callback fr topic %s", publishMessage.getTopicName());
            LOG.error(msg);
            throw new MQTTException(msg);
        }

        callback.published(publishMessage.getTopicName(),str);
        System.out.println("##### publishMessage.getQos():"+publishMessage.getQos());
        if(publishMessage.getQos()== AbstractMessage.QOSType.LEAST_ONE){
            sendPublishAckMsg(publishMessage.getMessageID());
        }
    }

    protected void sendPublishAckMsg(int messageID){
        PubAckMessage msg = new PubAckMessage();
        msg.setMessageID(messageID);
        channel.writeAndFlush(msg);
    }

    /**
     * In the current pinger is not ye executed, then cancel it and schedule
     * another by KEEPALIVE_SECS
     */
    private void updatePinger() {
        if (m_pingerHandler != null) {
            m_pingerHandler.cancel(false);
        }
        m_pingerHandler = m_scheduler.scheduleWithFixedDelay(pingerDeamon, KEEPALIVE_SECS, KEEPALIVE_SECS, TimeUnit.SECONDS);
    }

    private String readMACAddress() {
        try {
            NetworkInterface network = NetworkInterface.getNetworkInterfaces().nextElement();

            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], ""));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new MQTTException("Can't retrieve host MAC address", ex);
        }
    }
    
    private String generateClientID() {
        double rnd = Math.random();
//        String id =  "Moque" + m_macAddress + Math.round(rnd*1000);
        String id = "MoqueMAC959";
        LOG.info("Generated ClientID " + id);
        return id;
    }

    public void close() {
        //stop the pinger
        m_pingerHandler.cancel(false);

        //send the CLOSE message
        channel.writeAndFlush(new DisconnectMessage());

        // wait until the summation is done
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        m_connector.dispose();
    }

    
    public void shutdown() {
        group.shutdownGracefully();
    }

    /**
     * Used only to re-register the callback with the topic, not to send any
     * subscription message to the server, used when a client reconnect to an 
     * existing session on the server
     */
    public void register(String topic, IPublishCallback publishCallback) {
        //register the publishCallback in some registry to be notified
        m_subscribersList.put(topic, publishCallback);
    }

    public void register(SubscribeMessage subscribeMessage, IPublishCallback publishCallback) {
        //register the publishCallback in some registry to be notified
//        for(int i=0; i<subscribeMessage.subscriptions().size(); i++){
//            SubscribeMessage.Couple req = subscribeMessage.subscriptions().get(i);
//            m_subscribersList.put(req.getTopic(), publishCallback);
//        }
        for (SubscribeMessage.Couple req : subscribeMessage.subscriptions()) {
            System.out.println("############### 구독 리스트 : "+req.getTopic());
            m_subscribersList.put(req.getTopic(), publishCallback);
        }
    }
    
    //Remove the registration of the callback from the topic
    private void unregister(String topic) {
        m_subscribersList.remove(topic);
    }

    //Remove the registration of the callback from the topic
    private void unregister(SubscribeMessage subscribeMessage) {
        for (SubscribeMessage.Couple req : subscribeMessage.subscriptions()) {
            m_subscribersList.remove(req.getTopic());
        }
    }

    public Map<String, IPublishCallback> getM_subscribersList() {
        return m_subscribersList;
    }
}
