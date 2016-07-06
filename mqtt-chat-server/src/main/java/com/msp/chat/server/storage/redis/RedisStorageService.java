package com.msp.chat.server.storage.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.msp.chat.client.BrokerClientManager;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import com.msp.chat.core.mqtt.proto.messages.chat.MakeChatRoom;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.storage.redis.bean.OffMsgBean;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.redis.bean.PubMsgBean;
import com.msp.chat.server.storage.redis.bean.RedisChatRoomBean;
import com.msp.chat.server.worker.ChannelQueue;
import com.msp.chat.server.worker.MqttMsgWorkerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 4. 20..
 */
@Service
public class RedisStorageService {
    private Logger logger = LoggerFactory.getLogger("server");
    //key : roomID, value : RedisChatRoomBean class ==> JsonString
    public final static String REDIS_CHATROOM = "CHATROOM";
    //key : userid , Value : HashSet<String> ==> roomID
    public final static String REDIS_USER_CHATROOM = "USER_CHATROOM";
    public final static String REDIS_RETAIN_MSG = "RETAIN_MSG";
    public final static String REDIS_OFFLINE_MSG = "OFFMSG";
    public final static String REDIS_OFFLINE_MSG_EXPIRE = ":OFFMSG_EXPIRE";
    public final static String REDIS_USER_BROKERID = ":USER_BROKERID";
    public final static String REDIS_PUBMSG = "PUBMSG";
    public final static String REDIS_PUBACK_CNT = "PUBACK_CNT";
    public String OFFMSG_KEYTABLE = null;
    public String OFFMSG_EXPIRE_KEYTABLE = null;
    public int CACHE_USER_MSG_COUNT = 0;
    public long EXPIRE_MILISECOND = 0;

    @Autowired(required = true)
    private RedisTemplate masterRedisTemplate;

    @Autowired(required = true)
    private RedisTemplate slaveRedisTemplate;

    private Gson gson = new Gson();

    public RedisStorageService(){}

    @Deprecated
    // 메신저 WEB-API서버에서 대화방 생성처리함.
    public boolean putChatRoomMake(MakeChatRoom makeChatRoom){
        boolean returnVal = false;
        RedisChatRoomBean redisChatRoomBean = new RedisChatRoomBean(makeChatRoom.getAppID(),makeChatRoom.getRoomID(), makeChatRoom.getAlias(), makeChatRoom.getUserids());
        String jsonChatRoomString = gson.toJson(redisChatRoomBean);
        try {
            if(masterRedisTemplate!=null) {
                System.out.println("getAppID:" + makeChatRoom.getAppID() + "   getRoomID:" + makeChatRoom.getRoomID());

                // 채팅방 정보를 저장한다.
                masterRedisTemplate.opsForHash().put(makeChatRoom.getAppID() + REDIS_CHATROOM, makeChatRoom.getRoomID(), jsonChatRoomString);
                // TODO : 사용자별로 가입된 채팅방Collection 정보를 만들어 넣는다.
                HashSet<String> useridMultiKey = new HashSet<String>();
                StringTokenizer st = new StringTokenizer(makeChatRoom.getUserids());
                while(st.hasMoreTokens()){
                    String userID = st.nextToken();
                    useridMultiKey.add(userID);
                }
                List<Object> userRoomList = slaveRedisTemplate.opsForHash().multiGet(makeChatRoom.getAppID() + REDIS_USER_CHATROOM, useridMultiKey);
                for(Object roomIdSetJson : userRoomList){
                    if(roomIdSetJson==null){

                    }else{
                        HashSet<String> roomSet = gson.fromJson(roomIdSetJson.toString(),HashSet.class);
                    }
                }
            }else{
                logger.error("###[RedisStorageService putChatRoomMake] redisTemplate not connected~~!");
            }

            // Test
            Map<Object,Object> chatRoomMap = slaveRedisTemplate.opsForHash().entries(makeChatRoom.getAppID() + REDIS_CHATROOM);
            Set<Map.Entry<Object,Object>> chatRoomMapSet = chatRoomMap.entrySet();
            int i=0;
            for(Map.Entry<Object,Object> chatRoomEntry : chatRoomMapSet){
                i++;
                String chatRoomID = (String)chatRoomEntry.getKey();
                String chatRoomInfo = (String)chatRoomEntry.getValue();
                RedisChatRoomBean redisChatRoomBean1 = gson.fromJson(chatRoomInfo,RedisChatRoomBean.class);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnVal;
    }

    public String getChatRoomList(ByteBuffer byteByffer){
        String returnVal = null;
        return returnVal;
    }

    public boolean rmChatRoomUser(ByteBuffer byteByffer){
        boolean returnVal = false;
        return returnVal;
    }

    public void putUserIDBrokerID(String appid,String clientID, String brokerID){
        masterRedisTemplate.opsForHash().put(appid+REDIS_USER_BROKERID,clientID,brokerID);
    }

    public Object getUserAsignBrokerID(String appid,String clientID){
        return slaveRedisTemplate.opsForHash().get(appid+REDIS_USER_BROKERID,clientID);
    }

    public Object getBrokerID(String appid,String clientID){
        return slaveRedisTemplate.opsForHash().get(appid+REDIS_USER_BROKERID,clientID);
    }
    public void putPubMsg(PublishEvent publishEvent, int subscriberCnt){
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

        masterRedisTemplate.opsForHash().put(REDIS_PUBMSG, orgPublishKey, gson.toJson(pubMsgBean));
        masterRedisTemplate.opsForHash().put(REDIS_PUBACK_CNT, orgPublishKey, ""+subscriberCnt);
        if(logger.isTraceEnabled()){
            logger.trace("###[RedisStorageService putPubMsg] orgPublishKey : {} , subscriberCnt : {}", orgPublishKey, subscriberCnt);
        }
    }

    public void upPubMsgAckCnt(String appid, String pubClientID, int messageID, String thisServerID){
        String orgPublishKey = makePublishKey(pubClientID, messageID);
        Long ackCnt = masterRedisTemplate.opsForHash().increment(REDIS_PUBACK_CNT, orgPublishKey, -1);
        if(logger.isDebugEnabled()){
            logger.debug("###[RedisStorageService upPubMsgAckCnt] ackCnt:{} , pubClientID:{}, messageID:{}", ackCnt, pubClientID, messageID);
        }
        if(ackCnt<=0){
            Object stringObj = slaveRedisTemplate.opsForHash().get(REDIS_PUBMSG,orgPublishKey);
            masterRedisTemplate.opsForHash().delete(REDIS_PUBACK_CNT, orgPublishKey);
            masterRedisTemplate.opsForHash().delete(REDIS_PUBMSG, orgPublishKey);
            // TODO : 여기서 클라이언트에 발송메세지 카운트 전송해 주는 로직을 넣는게 좋을 듯함.
            // TODO : 그룹 채팅일 경우는 읽음확인이 안된 메세지는 요청하는 API를 만들어서 주는게 좋을 듯함. 왜냐하면 클라이언트가 받지 못할 수도 있고 관련 정보는 클라이언트가 더 정확히 알기 때문.
            Object brokerIdObj = getBrokerID(appid,pubClientID);
            if(stringObj!=null && brokerIdObj!=null){
                String brokerID = brokerIdObj.toString();
                PubMsgBean pubMsgBean = gson.fromJson(stringObj.toString(), PubMsgBean.class);
                StringBuilder sb = new StringBuilder(BrokerConfig.SYS_MSG_SENT_COMPLETE);
                sb.append(pubClientID+"|");
                sb.append(messageID);
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
                if(brokerID.equals(thisServerID)){
                    MqttMsgWorkerManager.getInstance().handleProtocolMessage(ChannelQueue.getInstance().getChannel(pubClientID).getSession(), publishMessage);
                }else{
                    try {
                        BrokerClientManager.getInstance().sendSysMessage(brokerID,publishMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                            // TODO : 실패처리 로직 구현한 큐에 넣음.
                            if(logger.isTraceEnabled()) {
                                logger.trace("###[RedisStorageService expireOffMsgProcess] clientID : {}, messageID : {}, offMsg User size : {}", offMsgBean.getSubClientID(), offMsgBean.getMsgID());
                            }
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

    private String makePublishKey(String clientID, int messageID){
        return String.format("%s|%d", clientID, messageID);
    }
}
