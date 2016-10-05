package com.mium2.messenger.util.client;

import com.mium2.messenger.util.AutoMsgSendThread;
import com.mium2.messenger.util.ConfigLoader;
import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.ConnectMessage;
import com.msp.chat.core.mqtt.proto.messages.PingReqMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 10. 4..
 */
public class BrokerConnectManager {

    private final static Logger logger = LoggerFactory.getLogger("com.mium2.messenger.util");
    private static final int KEEPALIVE_SECS = 60;
    private Map<String,ChannelFuture> channelFutureMap = new HashMap<String, ChannelFuture>();
    private Map<String,String> channelIdClientIdMap = new HashMap<String, String>();
    private NioEventLoopGroup group;
    private Bootstrap bootstrap;
    private boolean running = true;
    private static BrokerConnectManager instance = null;
    private String brokerIP = "";
    private int brokerPort = 1883;
    private String clientIdPrefix = "";
    private int makeClientStartPos = 0;
    private int makeClientEndPos = 1;

    public static BrokerConnectManager getInstance(){
        if(instance==null){
            instance = new BrokerConnectManager();
        }
        return instance;
    }

    public void init(String _brokerIP, int _brokerPort, String _clientIdPrefix, int _makeClientStartPos, int _makeClientEndPos){
        this.brokerIP = _brokerIP;
        this.brokerPort = _brokerPort;
        this.clientIdPrefix = _clientIdPrefix;
        this.makeClientStartPos = _makeClientStartPos;
        this.makeClientEndPos = _makeClientEndPos;
    }

    public void reqAllConnection(){

        if(brokerIP.equals("") || clientIdPrefix.equals("")){
            logger.error("#### 브로커 서버에 연결할 정보를 초기화 하지 못했습니다. 컨피그를 확인 하세요.");
            System.exit(-1);
        }

        group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new MQTTDecoder());
                    pipeline.addLast("encoder", new MQTTEncoder());
                    pipeline.addLast("handler", new ClientMQTTHandler());
                }
            });
        // Start the client.
        for(int i=makeClientStartPos; i<makeClientEndPos; i++) {
            try {
                ChannelFuture f = bootstrap.connect(brokerIP, brokerPort);
                f.awaitUninterruptibly();
                assert f.isDone();

                if(!f.isSuccess()){
                    f.cause().printStackTrace();
                    break;
                }

                ConnectMessage connMsg = new ConnectMessage();
                connMsg.setKeepAlive(KEEPALIVE_SECS);
                connMsg.setQos(AbstractMessage.QOSType.LEAST_ONE);
                connMsg.setClientID(clientIdPrefix + i);
                connMsg.setCleanSession(true);
                f.channel().writeAndFlush(connMsg);
                channelIdClientIdMap.put(f.channel().id().asShortText(),clientIdPrefix+i);
                channelFutureMap.put(clientIdPrefix+i, f);
            }catch (Exception e){
                logger.error("!!! Connection Error ClientID :{}    Err Msg : {}",clientIdPrefix+i,e.getMessage());
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(ConfigLoader.getProperty(ConfigLoader.AUTOMESSAGESEND_YN).equals("Y")) {
            // 부하테스트를 위한 자동 메세지 발송
            int threadCnt = ConfigLoader.getIntProperty(ConfigLoader.AUTOSENDTHREAD_CNT);
            for (int i = 0; i < threadCnt; i++) {
                AutoMsgSendThread autoMsgSendThread = new AutoMsgSendThread("Thread-" + i);
                autoMsgSendThread.start();
            }
        }

        try {
            while(true) {
                Thread.sleep(1000 * KEEPALIVE_SECS);
                sendPing();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendPing() {
        Set<Map.Entry<String,ChannelFuture>> channelFutureMapSet = channelFutureMap.entrySet();
        for(Map.Entry<String,ChannelFuture> channelFutureMapEntry : channelFutureMapSet){
            ChannelFuture channelFuture = channelFutureMapEntry.getValue();
            if(channelFuture.channel().isActive()) {
                PingReqMessage ping = new PingReqMessage();
                channelFuture.channel().writeAndFlush(ping);
                if(logger.isDebugEnabled()) {
                    logger.debug("###[BrokerConnectManager sendPing] send ping");
                }
            }else{
                channelFutureMap.remove(channelFuture);
            }

        }
    }


    public ChannelFuture getChannelFuture(String clientID){
        return channelFutureMap.get(clientID);
    }

    public String getClientID(String channelID){
        return channelIdClientIdMap.get(channelID);
    }

}
