package com.msp.chat.server.storage.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msp.chat.client.BrokerClientManager;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import com.msp.chat.server.bean.ConnectionDescriptor;
import com.msp.chat.server.bean.WebSocketMsgBean;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.netty.NettyChannel;
import com.msp.chat.server.storage.redis.bean.OffMsgBean;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.redis.bean.PubMsgBean;
import com.msp.chat.server.worker.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 7. 20..
 */
@Service
public class RedisStorageService {
    private Logger logger = LoggerFactory.getLogger("server");
    public final static String REDIS_RETAIN_MSG = "H_RETAIN_MSG";
    public final static String REDIS_OFFLINE_MSG = "H_OFFMSG"; // 유저아이디 : 발송메세지정보(json string)
    public final static String REDIS_OFFLINE_MSG_EXPIRE = "_OFFMSG_EXPIRE"; // 유저아이디 : 만료시간(가장오래된메세지)
    public final static String REDIS_LOGINUSER_BROKERID = "H_LOGINUSER_BROKERID";  // 로그인중인유저아이디 : 접속할브로커아이디
    public final static String REDIS_PUBMSG = "H_PUBMSG"; //발송자아이디|메세지번호 : 발송메세지정보(PubMsgBean Json String)
    public final static String REDIS_PUBACK_CNT = "H_PUBACK_CNT"; //발송자아이디|메세지번호 : 전달하지못한메세지카운트
    public final static String REDIS_ROOMID_MSG = "L_ROOMID_"; //채팅방아이디:보낸메세지. 설명==>채팅방별 보낸메세지리스트
    public String OFFMSG_KEYTABLE = null;
    public String OFFMSG_EXPIRE_KEYTABLE = null;
    public int CACHE_USER_MSG_COUNT = 0;
    public long EXPIRE_MILISECOND = 0;
    private boolean IS_SEND_RECEIVED_ACK; //발송자에게 수신카운트 정보 보낼지 여부

    public static String ORG_PUBLISH_LAST_CLEAN_DAY = "";

    @Autowired(required = true)
    private RedisTemplate masterRedisTemplate;

    @Autowired(required = true)
    private RedisTemplate slaveRedisTemplate;

    private Gson gson = new Gson();

    public RedisStorageService(){
        String sendReceivedAck = BrokerConfig.getProperty(BrokerConfig.SEND_RECEIVED_ACK_YN);
        if(sendReceivedAck.equals("Y")){
            IS_SEND_RECEIVED_ACK = true;
        }else{
            IS_SEND_RECEIVED_ACK = false;
        }
    }


    public void putUserIDBrokerID(String clientID, String brokerID){
        masterRedisTemplate.opsForHash().put(REDIS_LOGINUSER_BROKERID,clientID,brokerID);
    }

    public Object getUserAsignBrokerID(String clientID){
        return slaveRedisTemplate.opsForHash().get(REDIS_LOGINUSER_BROKERID,clientID);
    }

    public Object getBrokerID(String clientID){
        return slaveRedisTemplate.opsForHash().get(REDIS_LOGINUSER_BROKERID,clientID);
    }
    public void putPubMsg(PublishEvent publishEvent, int subscriberCnt){
        //챗팅방아이디(토픽)으로 키테이블 생성하는게 좋을 듯함. ListSet으로 그래야 메세지 히스토리를 사이즈 단위로 관리 할 수 있음.
        String orgPublishKey = makePublishKey(publishEvent.getPubClientID(), publishEvent.getMessageID());
        PubMsgBean pubMsgBean = new PubMsgBean();
        pubMsgBean.setPubClientID(publishEvent.getPubClientID());
        pubMsgBean.setSubClientID(publishEvent.getSubClientID());
        pubMsgBean.setTopic(publishEvent.getTopic());
        pubMsgBean.setMsgID(publishEvent.getMessageID() + "");
        pubMsgBean.setPub_message(publishEvent.getPub_message());
        pubMsgBean.setPub_qos(publishEvent.getPub_qos() + "");
        pubMsgBean.setRetainYN(publishEvent.getRetainYN());
        pubMsgBean.setSubscriberCnt(subscriberCnt + "");
        pubMsgBean.setSendate(System.currentTimeMillis()+"");

        //채팅방별로 설정한 갯수만큼 메세지를 보관한다.
        long listSize = masterRedisTemplate.opsForList().rightPush(REDIS_ROOMID_MSG + publishEvent.getTopic(), orgPublishKey + "|" + pubMsgBean.getPub_message() + "|" + pubMsgBean.getSendate());
        if(logger.isTraceEnabled()){
            logger.trace("## [RedisStorageService putPubMsg] room msg size==>rootID:{}   cnt:{}",publishEvent.getTopic(),listSize);
        }
        if(listSize>BrokerConfig.getIntProperty(BrokerConfig.ROOM_MSG_MAX_SAVE_CNT)){
            // 맨 처음거 하나를 지운다.
            masterRedisTemplate.opsForList().leftPop(REDIS_ROOMID_MSG + publishEvent.getTopic());
        }
        if(IS_SEND_RECEIVED_ACK) {
            masterRedisTemplate.opsForHash().put(REDIS_PUBMSG, orgPublishKey, gson.toJson(pubMsgBean));
            masterRedisTemplate.opsForHash().put(REDIS_PUBACK_CNT, orgPublishKey, "" + subscriberCnt);
            if (logger.isTraceEnabled()) {
                logger.trace("###[RedisStorageService putPubMsg] orgPublishKey : {} , subscriberCnt : {}", orgPublishKey, subscriberCnt);
            }
        }
    }

    /**
     * 고민 : 수신확인이 0이 되지 않는 경우(앱을 삭제하거나.. 앱을 구동하지 않을때) 축척되는 데이타 처리 방안.
     * 방안1 : Offmessage에서 삭제될때 지운다. 해결 완료
     * @param pubClientID : 메세지를 발행한 유저 즉, 이 유저에게 미수신메세지를 보내야 한다.
     * @param messageID
     */
    public void upPubMsgAckCnt(String pubClientID, int messageID){
        String orgPublishKey = makePublishKey(pubClientID, messageID);
        Long ackCnt = masterRedisTemplate.opsForHash().increment(REDIS_PUBACK_CNT, orgPublishKey, -1);
        if(logger.isDebugEnabled()){
            logger.debug("###[RedisStorageService upPubMsgAckCnt] ackCnt:{} , pubClientID:{}, messageID:{}", ackCnt, pubClientID, messageID);
        }
        // TODO : 고민1 : 마지막만 보낼까? 그냥 들어 올 때 마다 보낼까?
        // 발송자에게 수신ACK 정보를 통지해 주기 위해..
        Object stringObj = slaveRedisTemplate.opsForHash().get(REDIS_PUBMSG, orgPublishKey);
        if(ackCnt<=0) {
            masterRedisTemplate.opsForHash().delete(REDIS_PUBACK_CNT, orgPublishKey);
            masterRedisTemplate.opsForHash().delete(REDIS_PUBMSG, orgPublishKey);
        }

        Object brokerIdObj = getBrokerID(pubClientID);

        if(stringObj!=null && brokerIdObj!=null){
            String brokerID = brokerIdObj.toString();
            PubMsgBean pubMsgBean = gson.fromJson(stringObj.toString(), PubMsgBean.class);
            StringBuilder sb = new StringBuilder(BrokerConfig.SYS_MSG_SENT_COMPLETE);
            sb.append(pubClientID+"|");
            sb.append(pubMsgBean.getTopic()+"|");
            sb.append(messageID+"|");
            sb.append(ackCnt);
            if(logger.isTraceEnabled()){
                logger.trace("##[RedisStorageService upPubMsgAckCnt] SYS_MSG_SENT_COMPLETE:{}",sb.toString());
            }

            // 해당 클라이언트가 해당 브로커에 연결채널이 있으면 읽음확인 완료 카운트 0을보낸다.
            byte[] payLoadMsgBytes = sb.toString().getBytes();
            ByteBuffer payloadBuf = ByteBuffer.allocate(payLoadMsgBytes.length);
            payloadBuf.put(payLoadMsgBytes);
            payloadBuf.flip();

            PublishMessage publishMessage = new PublishMessage();
            publishMessage.setPayload(payloadBuf);
            publishMessage.setTopicName(pubMsgBean.getTopic());
            publishMessage.setMessageID(messageID);
            publishMessage.setQos(AbstractMessage.QOSType.MOST_ONE);
            publishMessage.setRetainFlag(false);

            // 발송대상자가 자기자신이 관리하는 대상자인지 확인 아닐 경우 다른 브로커에게 전달.
            if(brokerID.equals(BrokerConfig.getProperty(BrokerConfig.SERVER_ID))){

                //현재 MQTT client 발송자가 브로커서버에 접속되어 있는지 확인 후 있을 때에만 미수신카운트 정보 보내는 로직을 태운다.
                ConnectionDescriptor mqttChannel = ChannelQueue.getInstance().getChannel(pubClientID);
                if(mqttChannel!=null) {
                    MqttMsgWorkerManager.getInstance().handleProtocolMessage(mqttChannel.getSession(), publishMessage);
                }

                //PC Websocket에서 접속되어 있는지 확인 후 전송.
                ChannelHandlerContext channelHandlerContext= WebsocketClientIdCtxManager.getInstance().getChannel(pubClientID);
                NettyChannel webSocketNettyChannel = WebsocketCtxServerHandleManager.getInstance().getNettyChannel(channelHandlerContext);
                if(webSocketNettyChannel!=null){
                    // requestArr[0]:command, requestArr[1]:connectID, requestArr[2]:messageId, requestArr[3]:topic, requestArr[4]:message
                    String[] requestArr = new String[5];
                    requestArr[0]=BrokerConfig.SYS_MSG_SENT_COMPLETE;
                    requestArr[1]=pubClientID;
                    requestArr[2]=messageID+"";
                    requestArr[3]=pubMsgBean.getTopic();
                    requestArr[4]=ackCnt+"";
                    WebSocketMsgBean webSocketMsgBean = new WebSocketMsgBean(webSocketNettyChannel,BrokerConfig.SYS_MSG_SENT_COMPLETE,requestArr);
                    WebSocketMsgManager.getInstance().putWebSocketMsgBean(webSocketMsgBean);
                }
            }else{
                // 발송자가 접속해 있는 다른 브로커서버에 발송위임
                try {
                    BrokerClientManager.getInstance().sendSysMessage(brokerID,publishMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void putRetainMsg(String topic, PublishEvent evt){
        String pubEventJsonString = gson.toJson(evt);
        masterRedisTemplate.opsForHash().put(REDIS_RETAIN_MSG, topic, pubEventJsonString);
    }

    public void putOffMsg(String subClientID, PublishEvent publishEvent){
        if(OFFMSG_KEYTABLE==null) {
            String serverID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
            // 브로커들이 OFFMSG 키테이블은 공통으로 써야함. 이유는 다른 브로커로 할당받아 접속 할 수 있으므로...
            OFFMSG_KEYTABLE = REDIS_OFFLINE_MSG;
            CACHE_USER_MSG_COUNT = BrokerConfig.getIntProperty(BrokerConfig.CACHE_USER_MSG_COUNT);
            OFFMSG_EXPIRE_KEYTABLE = serverID + REDIS_OFFLINE_MSG_EXPIRE;
            EXPIRE_MILISECOND = BrokerConfig.getLongProperty(BrokerConfig.OFFMSG_EXPIRE_SECOND);
        }
        Object obj = slaveRedisTemplate.opsForHash().get(OFFMSG_KEYTABLE,subClientID);
        List<OffMsgBean> offMsgBeanList = null;
        String jsonString = "";
        if(obj!=null){
            jsonString = obj.toString();
            offMsgBeanList = gson.fromJson(jsonString,ArrayList.class);
            if(offMsgBeanList.size()>=CACHE_USER_MSG_COUNT){
                //설정한 갯수 이상을 담지 못하므로 가장 오래된 메세지를 지우면서 실패처리해야함. 실패처리 로직 구현한 큐에 넣음.
                offMsgBeanList.remove(0);
            }
        }else{
            long expireMiliTime = System.currentTimeMillis() + EXPIRE_MILISECOND;
            offMsgBeanList = new ArrayList<OffMsgBean>();
            // clientID를 키로 off메세지 만료시간 관리하는 키테이블. 최초 한번만 넣음
            masterRedisTemplate.opsForHash().put(OFFMSG_EXPIRE_KEYTABLE,subClientID,""+expireMiliTime);
            publishEvent.setExpire(expireMiliTime);
        }
        OffMsgBean offMsgBean = new OffMsgBean();
        offMsgBean.setMsgID(publishEvent.getMessageID()+"");
        offMsgBean.setPub_message(publishEvent.getPub_message());
        offMsgBean.setTopic(publishEvent.getTopic());
        offMsgBean.setPubClientID(publishEvent.getPubClientID());
        offMsgBean.setSubClientID(subClientID);
        offMsgBean.setQos(publishEvent.getPub_qos() + "");
        offMsgBean.setRetain(publishEvent.getRetainYN());
        offMsgBean.setExpire(System.currentTimeMillis());
        offMsgBeanList.add(offMsgBean);
        jsonString = gson.toJson(offMsgBeanList);
        if(logger.isDebugEnabled()) {
            logger.debug("###[RedisStorageService putOffMsg] jsonString:{}", jsonString);
        }
        masterRedisTemplate.opsForHash().put(OFFMSG_KEYTABLE, subClientID, jsonString);
    }

    public List<OffMsgBean> getSendOffMsgList(String subClientID){
        List<OffMsgBean> publishEventList = null;
        if(OFFMSG_KEYTABLE==null) {
            String serverID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
            OFFMSG_KEYTABLE = REDIS_OFFLINE_MSG;
            CACHE_USER_MSG_COUNT = BrokerConfig.getIntProperty(BrokerConfig.CACHE_USER_MSG_COUNT);
            OFFMSG_EXPIRE_KEYTABLE = serverID + REDIS_OFFLINE_MSG_EXPIRE;
            EXPIRE_MILISECOND = BrokerConfig.getLongProperty(BrokerConfig.OFFMSG_EXPIRE_SECOND);
        }
        Object publishEventJson = slaveRedisTemplate.opsForHash().get(OFFMSG_KEYTABLE,subClientID);
        if(publishEventJson!=null){
            Type listType = new TypeToken<ArrayList<OffMsgBean>>(){}.getType();
            publishEventList = gson.fromJson(publishEventJson.toString(), listType);
        }
        // 보내려고 가져온 off메세지와 off메세지 만료 키테이블에서 해당 아이디의 OFF메세지 정보를 삭제한다.
        masterRedisTemplate.opsForHash().delete(OFFMSG_KEYTABLE,subClientID);
        masterRedisTemplate.opsForHash().delete(OFFMSG_EXPIRE_KEYTABLE, subClientID);
        return publishEventList;
    }

    public long getOffMsgSize(){
        return slaveRedisTemplate.opsForHash().size(OFFMSG_KEYTABLE);
    }

    public String removeOffMsg(String subClientID, int messageId){
        String pubClientID = null;
        Object obj = slaveRedisTemplate.opsForHash().get(OFFMSG_KEYTABLE,subClientID);
        if(obj!=null){
            String jsonString = obj.toString();
            List<OffMsgBean> offMsgBeanList = gson.fromJson(jsonString, ArrayList.class);
            for(OffMsgBean offMsgBean : offMsgBeanList){
                if(offMsgBean.getMsgID().equals(""+messageId)){
                    pubClientID = offMsgBean.getPubClientID();
                    offMsgBeanList.remove(offMsgBean);
                    break;
                }
            }
            jsonString = gson.toJson(offMsgBeanList);
            masterRedisTemplate.opsForHash().put(OFFMSG_KEYTABLE, subClientID, jsonString);

            if(offMsgBeanList.size()==0){
                // OFFMSG_EXPIRE_KEYTABLE에서도 삭제
                masterRedisTemplate.opsForHash().delete(OFFMSG_EXPIRE_KEYTABLE);
            }
        }
        return pubClientID;
    }

    /**
     * 해당 메소드는 설정값에 저장되어 있는 오프메세지 체크주기에 쓰레드로 주기적으로 호출해야함.
     * 단, 성능에 영향을 줄수 있으므로 적어도 10분이상으로 설정하는 걸 권장함.
     */
    public void offMsgExpireCheck(){
        long startMilitime = System.currentTimeMillis();
        if(OFFMSG_KEYTABLE==null) {
            String serverID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
            OFFMSG_KEYTABLE = REDIS_OFFLINE_MSG;
            CACHE_USER_MSG_COUNT = BrokerConfig.getIntProperty(BrokerConfig.CACHE_USER_MSG_COUNT);
            OFFMSG_EXPIRE_KEYTABLE = serverID + REDIS_OFFLINE_MSG_EXPIRE;
            EXPIRE_MILISECOND = BrokerConfig.getLongProperty(BrokerConfig.OFFMSG_EXPIRE_SECOND);
        }
        List<String> expireClientIDs = new ArrayList<String>();
        Map<String,String> offmsgExpireMap = slaveRedisTemplate.opsForHash().entries(OFFMSG_EXPIRE_KEYTABLE);
        Set<Map.Entry<String,String>> offmsgExpireSet = offmsgExpireMap.entrySet();
        long nowMilSecond  = System.currentTimeMillis();
        for(Map.Entry<String,String> offmsgExpireEntry : offmsgExpireSet){
            String clientID = offmsgExpireEntry.getKey();
            Long expireMilSecond = Long.parseLong(offmsgExpireEntry.getValue());
            if(logger.isTraceEnabled()) {
                logger.trace("###[RedisStorageService offMsgExpireCheck] nowMilSecond: {}, expireMilSecond {}" ,nowMilSecond, expireMilSecond);
            }
            if(nowMilSecond>=expireMilSecond){
                expireClientIDs.add(clientID);
            }
        }
        // 최대 10,000개씩 처리하도록 구현.
        Set<String> multiKeySet = new HashSet<String>();
        for(int i=0; i<expireClientIDs.size(); i++){
            // OFFMSG에서 체크하여 지워야 할 Client ID
            // 해당 클라이언트아이디로 저장되어 있는 오프메세지를 지우고 실패 처리해야함.
            // 가장 적게 남아있는 시간으로 client Expire time을 세팅해야함.
            multiKeySet.add(expireClientIDs.get(i));
            if(multiKeySet.size()%10000==0){
                // 클라이언트에 오프메세지에 등록된 메세지 중 만료된 메세지 실패처리
                List<String> clientIdOffmsgList = slaveRedisTemplate.opsForHash().multiGet(OFFMSG_KEYTABLE,multiKeySet);
                expireOffMsgProcess(clientIdOffmsgList);
                //처리한 데이타는 초기화 시킴
                multiKeySet = new HashSet<String>();
            }
        }
        // 처리되지 않은 남은 멀티키 조회하여 처리
        if(multiKeySet.size()>0){
            List<String> clientIdOffmsgList = slaveRedisTemplate.opsForHash().multiGet(OFFMSG_KEYTABLE,multiKeySet);
            expireOffMsgProcess(clientIdOffmsgList);
        }

        long endMilitime = System.currentTimeMillis();
        long elapsedmiltime = endMilitime-startMilitime;
        if(logger.isInfoEnabled()) {
            logger.info("###[RedisStorageService offMsgExpireCheck] Off msg check Elapsed MiliTime : {} , offMsg User size : {}" ,elapsedmiltime , slaveRedisTemplate.opsForHash().size(OFFMSG_EXPIRE_KEYTABLE));
        }
    }

    private void expireOffMsgProcess(List<String> clientIdOffmsgList){
        Type listType = new TypeToken<ArrayList<OffMsgBean>>(){}.getType();
        List<OffMsgBean> newOffMsgBeanList = null;
        List<OffMsgBean> offMsgBeanList = null;
        try {
            for (String offMsg : clientIdOffmsgList) {
                if(offMsg!=null) {
                    String clientID = "";
                    long expireMinTime = 0;

                    newOffMsgBeanList = new ArrayList<OffMsgBean>();
                    offMsgBeanList = gson.fromJson(offMsg, listType);
                    for (int j=0; j<offMsgBeanList.size(); j++) {
                        OffMsgBean offMsgBean = offMsgBeanList.get(j);
                        if(j==0){
                            clientID = offMsgBean.getSubClientID();
                            expireMinTime = offMsgBean.getExpire();
                        }
                        // 만료된 메세지 검증
                        if(System.currentTimeMillis()>offMsgBean.getExpire()){
                            // TODO : PUB_ACK 키테이블에서 삭제 처리 하고.. RDB 어딘가에 저장하여 보존한다.
                            if(logger.isTraceEnabled()) {
                                logger.trace("###[RedisStorageService expireOffMsgProcess] pubclientID : {},  subclientID :{} , messageID : {}, offMsg User size : {}",offMsgBean.getPubClientID(), offMsgBean.getSubClientID(), offMsgBean.getMsgID());
                            }
                            // PUB_ACK 키테이블에서 삭제 처리한다.
                            upPubMsgAckCnt(offMsgBean.getPubClientID(), Integer.parseInt(offMsgBean.getMsgID()));
                        }else{
                            // 만료되지 않은 오프메세지 다시 담음.
                            newOffMsgBeanList.add(offMsgBean);
                            // 만료되지 않은 오프메세지 중에서 가장 만료시간이 적게남은 메세지 시간을 구함.
                            if(expireMinTime>offMsgBean.getExpire()){
                                clientID = offMsgBean.getSubClientID();
                                expireMinTime = offMsgBean.getExpire();
                            }
                        }
                    }

                    if(newOffMsgBeanList.size()==0){
                        // 만료된 오프메세지를 삭제 처리 후 Off메세지가 없으면 오프메세지에서 삭제 처리
                        masterRedisTemplate.opsForHash().delete(OFFMSG_KEYTABLE, clientID);
                        // 만료된 오프메세지를 삭제 처리 후 Off메세지가 없으면 오프메세지 expire 키테이블 삭제처리
                        masterRedisTemplate.opsForHash().delete(OFFMSG_EXPIRE_KEYTABLE, clientID);

                        if(logger.isTraceEnabled()){
                            logger.trace("###[RedisStorageService expireOffMsgProcess] OFFMSG_KEYTABLE size :" + slaveRedisTemplate.opsForHash().size(OFFMSG_KEYTABLE) + "    OFFMSG_EXPIRE_KEYTABLE size:" + slaveRedisTemplate.opsForHash().size(OFFMSG_EXPIRE_KEYTABLE));
                        }
                    }else {
                        // 만료된 오프메세지를 삭제 처리하고 만료되지 않은 메세지만 담은 오프메세지 다시 저장
                        masterRedisTemplate.opsForHash().put(OFFMSG_KEYTABLE, clientID, gson.toJson(newOffMsgBeanList));
                        // 만료시간 검증할 키테이블에 해당 클라이언트를 키로 가장 적게 남은 메세지의 말료시간을 저장함.
                        masterRedisTemplate.opsForHash().put(OFFMSG_EXPIRE_KEYTABLE, clientID, "" + expireMinTime);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 단말이 브로커에 발송 요청한 publish메세지는 구독자가 다 받거나 off메세지 저장시간이 만료되면 다 지워져야 하나..
     * 서버를 리부트 하면 off메세지에 저장이 다 날아가 오래된 메세지임에도 불구하고
     * 더미데이타로 계속 쌓여 메모리를 까먹으므로 설정된 새벽시간에 메세지 사이즈를 체크하여 정리해줄 필요가 있음.
     */
    public void cleanOrgPublishMsg(){
        long chkMilSecond = BrokerConfig.getIntProperty(BrokerConfig.OFFMSG_EXPIRE_SECOND)*1000;
        Calendar c = Calendar.getInstance(); //객체 생성 및 현재 일시분초...셋팅
        StringBuffer sb = new StringBuffer();
        sb.append(c.get(Calendar.YEAR));
        sb.append(c.get(Calendar.MONTH)+1);
        sb.append(c.get(Calendar.DATE));

        // 하루에 한번만 청소하기 위해..
        if(!sb.toString().equals(ORG_PUBLISH_LAST_CLEAN_DAY)){
            try {
                int MULTIKEY_SIZE = 5000;
                Set<Object> allPublishkeys = slaveRedisTemplate.opsForHash().keys(REDIS_PUBMSG);
                Collection<Object> multiPushKey = new ArrayList<Object>();
                int multiPushKeySize = 0;
                for (Object publishKey : allPublishkeys) {
                    multiPushKey.add(publishKey);
                    multiPushKeySize++;
                    if (multiPushKeySize % MULTIKEY_SIZE == 0) {
                        // 속도를 위해 모아서 multikey로 보내 리스트를 받는다.
                        List<Object> orgPublishList = slaveRedisTemplate.opsForHash().multiGet(REDIS_PUBMSG, multiPushKey);
                        for (Object publishMsgObj : orgPublishList) {
                            if (publishMsgObj != null) {
                                PubMsgBean pubMsgBean = gson.fromJson(publishMsgObj.toString(), PubMsgBean.class);
                                long sendDateMilSecond = Long.parseLong(pubMsgBean.getSendate());
                                if((System.currentTimeMillis()-chkMilSecond)>sendDateMilSecond){
                                    // PUBMSG, PUB_ACK에서 지워야함
                                    String cleanKey = makePublishKey2(pubMsgBean.getPubClientID(), pubMsgBean.getMsgID());
                                    masterRedisTemplate.opsForHash().delete(REDIS_PUBMSG,cleanKey);
                                    masterRedisTemplate.opsForHash().delete(REDIS_PUBACK_CNT,cleanKey);
                                    if(logger.isDebugEnabled()){
                                        logger.debug("##[RedisStorageService cleanOrgPublishMsg] Clean Key : {}",cleanKey);
                                    }
                                }
                            }
                        }
                        multiPushKey.clear();
                    }
                }
                // 나머지 처리
                if (multiPushKey.size() > 0) {
                    // 속도를 위해 모아서 multikey로 보내 푸쉬발송유저정보 리스트를 받는다.
                    List<Object> orgPublishList = masterRedisTemplate.opsForHash().multiGet(REDIS_PUBMSG, multiPushKey);
                    for (Object publishMsgObj : orgPublishList) {
                        if (publishMsgObj != null) {
                            PubMsgBean pubMsgBean = gson.fromJson(publishMsgObj.toString(), PubMsgBean.class);
                            long sendDateMilSecond = Long.parseLong(pubMsgBean.getSendate());
                            if((System.currentTimeMillis()-chkMilSecond)>sendDateMilSecond){
                                // PUBMSG, PUB_ACK에서 지워야함
                                String cleanKey = makePublishKey2(pubMsgBean.getPubClientID(), pubMsgBean.getMsgID());
                                masterRedisTemplate.opsForHash().delete(REDIS_PUBMSG,cleanKey);
                                masterRedisTemplate.opsForHash().delete(REDIS_PUBACK_CNT,cleanKey);
                                if(logger.isDebugEnabled()){
                                    logger.debug("##[RedisStorageService cleanOrgPublishMsg] Clean Key : {}",cleanKey);
                                }
                            }
                        }
                    }
                    multiPushKey.clear();
                }
                // 청소할 날짜 셋팅
                ORG_PUBLISH_LAST_CLEAN_DAY = sb.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String makePublishKey(String clientID, int messageID){
        return String.format("%s|%d", clientID, messageID);
    }

    private String makePublishKey2(String clientID, String messageID){
        return String.format("%s|%s", clientID, messageID);
    }
}
