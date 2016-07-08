package com.msp.chat.server.worker;

import com.msp.chat.client.BrokerClientManager;
import com.msp.chat.core.mqtt.proto.messages.*;
import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.Server;
import com.msp.chat.server.bean.ConnectionDescriptor;
import com.msp.chat.server.bean.Subscription;
import com.msp.chat.server.bean.events.*;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.commons.utill.DebugUtils;
import com.msp.chat.server.netty.ServerChannel;
import com.msp.chat.server.storage.ISubscribeStore;
import com.msp.chat.server.storage.ehcache.*;
import com.msp.chat.server.storage.memory.MemorySubscribeStore;
import com.msp.chat.server.storage.redis.RedisStorageService;
import com.msp.chat.server.storage.redis.RedisSubscribeStore;
import com.msp.chat.server.storage.redis.bean.OffMsgBean;
import com.msp.chat.server.storage.sqlite.SqliteSubscribeStore;
import net.sf.ehcache.Element;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 4. 11..
 */
public class MqttMsgProcessor {
    private final Logger LOGGER = LoggerFactory.getLogger("server");
    private final Logger LOGGER_PUBLISH = LoggerFactory.getLogger("publish");
    private final Logger LOGGER_PUBACK = LoggerFactory.getLogger("puback");
    private IAuthenticator m_authenticator;
    private final RedisStorageService redisStorageService;
    private final ISubscribeStore subscribeStore;
    private final String SERVER_ID;
    private final String APPID;
    private final String OFFMSG_STORAGE_KIND; // 0 : ehcache, 1 : reids
    private final String FILE_SAVE_ROOT_SRC;
    private final int THUMBNAIL_WIDTH;
    private final int THUMBNAIL_HEIGHT;
    private final String IMG_FOLDER = "images";
    private final String ETC_FOLDER = "etc";
    private final String THUMB_FOLDER = "thumb";
    private Set<String> CHKIMGSET;

    public MqttMsgProcessor(){
        SERVER_ID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
        APPID = BrokerConfig.getProperty(BrokerConfig.APPID);
        OFFMSG_STORAGE_KIND = BrokerConfig.getProperty(BrokerConfig.OFFMSG_STORE_KIND);
        FILE_SAVE_ROOT_SRC = BrokerConfig.getProperty(BrokerConfig.FILE_SAVE_SRC);
        THUMBNAIL_WIDTH = BrokerConfig.getIntProperty(BrokerConfig.THUMBNAIL_WIDTH);
        THUMBNAIL_HEIGHT = BrokerConfig.getIntProperty(BrokerConfig.THUMBNAIL_HEIGHT);
        CHKIMGSET = new HashSet<String>();
        CHKIMGSET.add("jpg");
        CHKIMGSET.add("jpeg");
        CHKIMGSET.add("gif");
        CHKIMGSET.add("bmp");
        CHKIMGSET.add("png");
        CHKIMGSET.add("tif");
        try {
            FileUtils.forceMkdir(new File(FILE_SAVE_ROOT_SRC + IMG_FOLDER + "/" + THUMB_FOLDER));
            FileUtils.forceMkdir(new File(FILE_SAVE_ROOT_SRC + ETC_FOLDER));
        }catch (Exception e){
            e.printStackTrace();
        }
        redisStorageService = (RedisStorageService)Server.ctx.getBean("redisStorageService");
        if(BrokerConfig.getProperty(BrokerConfig.SUBSCRIBE_STORE_KIND).equals("memory")){
            subscribeStore = new MemorySubscribeStore();
        }else if(BrokerConfig.getProperty(BrokerConfig.SUBSCRIBE_STORE_KIND).equals("sqlite")){
            subscribeStore = new SqliteSubscribeStore(BrokerConfig.getProperty(BrokerConfig.SQLITE_SRC));
        }else{
            subscribeStore = (RedisSubscribeStore)Server.ctx.getBean("redisSubscribeStore");
        }
    }

    protected void init(IAuthenticator authenticator) {
        m_authenticator = authenticator;
    }

    //클라이언트가 접속 했을 때 처리
    protected void processConnect(ServerChannel session, ConnectMessage msg) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[MqttMsgProcessor processConnect] Request Connect Client ID : {}", msg.getClientID());
        }
        //클라이언트 버전 체크
        if (msg.getProcotolVersion() != 0x03) {
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.UNNACEPTABLE_PROTOCOL_VERSION);
            LOGGER.warn("###[MqttMsgProcessor processConnect] Connect Error : sent bad proto ConnAck");
            session.write(badProto);
            session.close(false);
            return;
        }

        //접속 클라이언트 아이디 체크
        if (msg.getClientID() == null || msg.getClientID().length() > 50) {
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.IDENTIFIER_REJECTED);
            session.write(badProto);
            return;
        }
        if(!msg.getClientID().startsWith(BrokerConfig.SYSTEM_BROKER_CLIENT_PRIFIX)) {
            Object obj = redisStorageService.getUserAsignBrokerID(APPID, msg.getClientID());
            if (obj == null || !obj.toString().equals(SERVER_ID)) {
                ConnAckMessage badProto = new ConnAckMessage();
                badProto.setReturnCode(ConnAckMessage.NOT_AUTHORIZED);
                LOGGER.error("###[MqttMsgProcessor processConnect] not registed clientID or Not allocated broker serverID.");
                session.write(badProto);
                return;
            }
        }

        //접속 클라이언트 아이디 존재 여부
        if (ChannelQueue.getInstance().isContainsKey(msg.getClientID())) {
            LOGGER.info("###[MqttMsgProcessor processConnect] !!!!!!! Double LOGIN !!!!!!");
            ServerChannel oldSession = ChannelQueue.getInstance().getChannel(msg.getClientID()).getSession();
            //접속자 배열에서 이전 클라이언트 세션아이디를 닫는다.
            if(oldSession!=null) {
                oldSession.setAttribute(Constants.DOUBLE_LOGIN,true);
                oldSession.close(false);
            }
        }

        int keepAlive = msg.getKeepAlive();
        session.setAttribute(Constants.KEEP_ALIVE, keepAlive);
        session.setAttribute(Constants.CLEAN_SESSION, msg.isCleanSession());
        session.setAttribute(Constants.ATTR_CLIENTID, msg.getClientID());
        try{
            session.setIdleTime(Math.round(keepAlive * 1.5f));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            //새로 접속한 클라이언트 아이디를 접속자 배열에 넣는다.
            ConnectionDescriptor connDescr = new ConnectionDescriptor(msg.getClientID(), session, msg.isCleanSession());
            ChannelQueue.getInstance().putChannel(msg.getClientID(), connDescr);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("###[MqttMsgProcessor processConnect] msg.getClientID(): {}, clientIDSessionMap.size: {}",msg.getClientID(),ChannelQueue.getInstance().getSize());
            }

            // 접속시 공통으로 보낼 메세지가 있다면 isWillFlag 플레그를 이용 하여 publish를 한다. 예)해당 그룹에 접속 정보 메세지
            // 쳇팅시 사용 유용
            if (msg.isWillFlag()) {
                AbstractMessage.QOSType willQos = AbstractMessage.QOSType.values()[msg.getWillQos()];
                byte[] willPayload = msg.getWillMessage().getBytes();
                ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(willPayload.length).put(willPayload).flip();
                PublishEvent pubEvt = new PublishEvent(msg.getWillTopic(), willQos, bb, msg.isWillRetain(), msg.getClientID(), null, session);
                processPublish(pubEvt);
            }

            // 접속시 접속메세지에 정보에 패스워드 체크여부 넣었을 경우 패스워드 체크
            if (msg.isUserFlag()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("###[MqttMsgProcessor processConnect] PASSWORD CHECK");
                }
                String pwd = null;
                if (msg.isPasswordFlag()) {
                    pwd = msg.getPassword();
                }
                if (!m_authenticator.checkValid(msg.getUsername(), pwd)) {
                    ConnAckMessage okResp = new ConnAckMessage();
                    okResp.setReturnCode(ConnAckMessage.BAD_USERNAME_OR_PASSWORD);
                    session.write(okResp);
                    return;
                }
            }

            ConnAckMessage okResp = new ConnAckMessage();
            okResp.setReturnCode(ConnAckMessage.CONNECTION_ACCEPTED);
            session.write(okResp);
            // 현재 클라이언트아이디에 전달되지 못한 OFFMESSAGE 전달.
            if (msg.isCleanSession()) {
//                CacheOfflineMsgStore.getInstance().remove(msg.getClientID());
            } else {
                offMsgRepublish(msg.getClientID());
            }
        }catch (Exception e){
            ConnAckMessage badProto = new ConnAckMessage();
            badProto.setReturnCode(ConnAckMessage.SERVER_UNAVAILABLE);
            session.write(badProto);
            return;
        }
    }

    /**
     * 메세지 Publish이벤트(발행)가 발생했을때...
     * @param evt : PublishEvent
     */
    protected void processPublish(PublishEvent evt) throws Exception{
        final String topic = evt.getTopic();
        final AbstractMessage.QOSType qos = evt.getQos();
        final ByteBuffer message = evt.getMessage();
        boolean retain = evt.isRetain(); //Offline일 경우 메세지 저장여부
        if(message.remaining()>10){
            byte[] chkSysMsgBytes = new byte[10];
            message.mark();
            message.get(chkSysMsgBytes);
            String chkSysMsg = new String(chkSysMsgBytes,"utf-8");
            if(chkSysMsg.equals(BrokerConfig.SYS_MSG_SENT_COMPLETE)){
                byte[] revMsgBytes = new byte[message.remaining()];
                message.get(revMsgBytes);
                String revMsg = new String(revMsgBytes,"utf-8");
                String[] messageArr = revMsg.split("\\|");
                String pubClientID = SERVER_ID;
                String subClientID = messageArr[0];
                String sendMsg = messageArr[1];
                sendMsg=BrokerConfig.SYS_MSG_SENT_COMPLETE+sendMsg;
                byte[] sendMsgbytes = sendMsg.getBytes();
                ByteBuffer sendMsgByteBuffer = ByteBuffer.allocate(sendMsgbytes.length);
                sendMsgByteBuffer.put(sendMsgbytes);
                sendMsgByteBuffer.flip();
                sendPublish(pubClientID, subClientID, topic, qos, sendMsgByteBuffer, retain, evt.getMessageID());
                return;
            }else if(chkSysMsg.equals(BrokerConfig.SYS_REQ_MSG_SENT_INFO)){
                //TODO : 클라이언트가 메세지아이디 배열을 보내면 해당 메세지가 발송상태 카운트를 보내준다.
                return;
            }else if(chkSysMsg.equals(BrokerConfig.SYS_REQ_MSG_FILE)){
                // 파일보내기 메세지 처리
                try {
                    int rev_fileNameSize = message.getInt();
                    byte[] rev_fileNameBytes = new byte[rev_fileNameSize];
                    message.get(rev_fileNameBytes);
                    String rev_fileName = new String(rev_fileNameBytes,"utf-8");
                    int lastIndex = rev_fileName.lastIndexOf(".");
                    String fileExtention = rev_fileName.substring(lastIndex+1).toLowerCase();
                    String saveFileFullSrc;
                    if(CHKIMGSET.contains(fileExtention)){
                        saveFileFullSrc = FILE_SAVE_ROOT_SRC +"images/";
                    }else{
                        saveFileFullSrc = FILE_SAVE_ROOT_SRC +"etc/";
                    }
                    rev_fileName = System.currentTimeMillis()+"."+fileExtention;
                    String orgFileFullSrc = saveFileFullSrc+rev_fileName;
                    FileOutputStream fot = new FileOutputStream(orgFileFullSrc);
                    byte[] fileBytes = new byte[message.remaining()];
                    message.get(fileBytes);
                    fot.write(fileBytes);
                    fot.close();

                    File thumbFile = null;
                    // 이미지 파일 일때만 썸네일 이미지 만들기
                    if(CHKIMGSET.contains(fileExtention)) {
                        thumbFile = new File(saveFileFullSrc+"thumb/"+rev_fileName);
                        BufferedImage buffer_original_image = ImageIO.read(new File(orgFileFullSrc));
                        int orgImgHeight = buffer_original_image.getHeight();
                        int orgImgWidth = buffer_original_image.getWidth();

                        int autoResizeWidth = (orgImgWidth * THUMBNAIL_HEIGHT) / orgImgHeight;
                        BufferedImage buffer_thumbnail_image;
                        if (fileExtention.equals("png")) {
                            buffer_thumbnail_image = new BufferedImage(autoResizeWidth, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                        } else {
                            buffer_thumbnail_image = new BufferedImage(autoResizeWidth, THUMBNAIL_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
                        }
                        Graphics2D graphic = buffer_thumbnail_image.createGraphics();
                        graphic.drawImage(buffer_original_image, 0, 0, autoResizeWidth, THUMBNAIL_HEIGHT, null);
                        ImageIO.write(buffer_thumbnail_image, fileExtention, thumbFile);
                    }

                    //TODO : 시스템메세지 파일 전송일 경우 : 바로 리턴하지 않고 다른 클라이언트에 이미지일경우 썸네일이미지 말들고 다운받을 주소를 보낸다. 이미지가 아닐 경우는 확장자 정보와 다운받을 주소를 보낸다.
                    byte[] sysMsgKindBytes = BrokerConfig.SYS_RES_MSG_FILE.getBytes();
                    byte[] fileExtBytes = fileExtention.getBytes();
                    byte[] imgDownSrcBytes= rev_fileName.getBytes();

                    // sysMsgKindBytes.length + 확장자길이 + 확장자 + 다운로드URL주소길이 + 다운로드URL + 썸네일이미지;
                    int allcateSize = sysMsgKindBytes.length + 4 + fileExtBytes.length + 4 + imgDownSrcBytes.length;
                    if(thumbFile!=null){
                        allcateSize = allcateSize+(int)thumbFile.length();
                    }
                    ByteBuffer sysImgMsgByteBuffer = ByteBuffer.allocate(allcateSize);
                    sysImgMsgByteBuffer.put(sysMsgKindBytes);
                    sysImgMsgByteBuffer.putInt(fileExtBytes.length);
                    sysImgMsgByteBuffer.put(fileExtBytes);
                    sysImgMsgByteBuffer.putInt(imgDownSrcBytes.length);
                    sysImgMsgByteBuffer.put(imgDownSrcBytes);
                    if(thumbFile!=null){
                        FileInputStream thumbFileInStream = new FileInputStream(thumbFile);
                        FileChannel cin = thumbFileInStream.getChannel();
                        cin.read(sysImgMsgByteBuffer);
                        cin.close();
                    }
                    sysImgMsgByteBuffer.flip();
                    evt.setM_message(sysImgMsgByteBuffer);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            message.reset();
        }

        //브로커는 QOS: 1로 publish 메세지의 대해 발송처리시 PubAckEvent를 날려야함.
        if (qos == AbstractMessage.QOSType.LEAST_ONE) {
            try {
                sendPubAck(new PubAckEvent(evt.getMessageID(), evt.getPubClientID()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (qos == AbstractMessage.QOSType.EXACTLY_ONCE) {
            //qos2 메세지 스토어에 저장
            CacheQos2Store.getInstance().put(evt);
            sendPubRec(evt.getPubClientID(), evt.getMessageID());
        }

        //QOS:2일 경우는 발송을 하지 않고 스토리지에 발송메세지만 저장하고 SendPubRec메세지를 날리기 때문 구독자에게 발송해서는 안된다.
        if (qos != AbstractMessage.QOSType.EXACTLY_ONCE) {
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("###[MqttMsgProcessor processPublish] PUB_CLIENTID:{}, TOPIC:{}, MSGID:{}, MSG:{} ", evt.getPubClientID(), evt.getTopic(), evt.getMessageID(), DebugUtils.payload2Str(message));
            }
            publish2Subscribers(evt);
        }

        if (retain) {
            redisStorageService.putRetainMsg(topic, evt);
        }
    }

    /**
     * 발행자가 발행한 토픽을 브로커가 구독자에게 발송한다.
     * @param evt
     */
    private void publish2Subscribers(PublishEvent evt) {
        //해당 Topic를 구독 신청한 구독자들에게 메세지 전송. broker ==> client 전달
        try {
            HashSet<String> clientIdSet = subscribeStore.getSubscribeClientID(evt.getTopic());
            if(clientIdSet!=null){
                // TODO : 해당 Publish 원본 메세지를 얼마나 오랫동안 보관하여야 하는가? 삭제해야 한다면 어떤 방식으로 삭제해야 하는가? 고민...
                // TODO : [처리방안 1] 수신확인 ACK가 다 들어온 메세지는 삭제 처리.
                // 메세지 발행자의 원본메세지 정보를 로그파일로 저장.
                if(evt.getQos()!= AbstractMessage.QOSType.MOST_ONE) {
                    if (LOGGER_PUBLISH.isInfoEnabled()) {
                        LOGGER_PUBLISH.info("PUBCLIENTID:{}, TOPIC:{}, MSGID:{}, MSG:{} ", evt.getPubClientID(), evt.getTopic(), evt.getMessageID(), DebugUtils.payload2Str(evt.getMessage()));
                    }
                    // REDIS에 발송메세지 원장 저장. 자신은 보내지 않으므로 하나 뺌.
                    redisStorageService.putPubMsg(evt, clientIdSet.size()-1);
                }

                for(String revClientID : clientIdSet){
                    String[] subscriberInfoArr = revClientID.split("\\|");
                    revClientID = subscriberInfoArr[0];
                    String allocateBrokerID = subscriberInfoArr[1];
                    // 메세지 발송자에게는 메세지를 발송하지 않는다.
                    if(evt.getPubClientID().equals(revClientID)){
                        continue;
                    }
                    evt.setSubClientID(revClientID);

                    // 구독자들에게 발송후 ACK를 받기 위해 고유한 MessageID를 만들어야 한다.
                    ByteBuffer message = evt.getMessage().duplicate();

                    // 자신에게 할당된 사용자인지 확인하여 자신에게 할당된 유저아이디는 보내고 아닐경우는 할당된 메신저브로커에 메세지를 보내 위임한다.
                    if(SERVER_ID.equals(allocateBrokerID)) {
                        //구독자에게  발송처리
                        sendPublish(evt.getPubClientID(), revClientID, evt.getTopic(), AbstractMessage.QOSType.LEAST_ONE, message, false, evt.getMessageID());
                    }else{
                        //TODO : 다른 브로커서버에 메세지발송 위임처리
                        try {
                            //TODO : 메세지에 발송자아이디, 수신자아이디를 넣어서 메세지를 만든 후 발송한다.
                            String pubClientID = evt.getPubClientID();
                            String subClientID = revClientID;
                            String clientIdInfo = pubClientID+"|"+subClientID;
                            // 원본 보낼메세지 byte배열로 추출
                            byte[] messageBytes = new byte[message.remaining()];
                            message.get(messageBytes);
                            if(LOGGER.isDebugEnabled()){
                                LOGGER.debug("###[MqttMsgProcessor publish2Subscribers] pubClientID:{} subClientID:{} orgMessage:{}",pubClientID,subClientID,new String(messageBytes,"utf-8"));
                            }
                            // 발송자아이디와 수신자아이디를 넣은 메세지 Bytebuffer를 만듬.
                            int allocateSize = 4+clientIdInfo.getBytes().length + messageBytes.length;
                            ByteBuffer sysPublishBuf = ByteBuffer.allocate(allocateSize);
                            sysPublishBuf.putInt(clientIdInfo.length());
                            sysPublishBuf.put(clientIdInfo.getBytes());
                            sysPublishBuf.put(messageBytes);
                            sysPublishBuf.flip();

                            BrokerClientManager.getInstance().sendSysMessage(allocateBrokerID,sysPublishBuf,evt.getTopic(),evt.getMessageID());
                        }catch (Exception e){
                            LOGGER.error("###[MqttMsgProcessor publish2Subscribers]"+e.getMessage());
                            PublishEvent newPublishEvt = new PublishEvent(evt.getTopic(), AbstractMessage.QOSType.LEAST_ONE, message, false, evt.getPubClientID(), evt.getSubClientID(), evt.getMessageID(), null);
                            redisStorageService.putOffMsg(evt.getSubClientID(), newPublishEvt);
                        }
                    }
                }
            }else{
                // TODO : 해당 토픽을 구독신청자가 하나도 없는경우. 처리방안 고민?(아무일도 안해도 될것 같음.)
                LOGGER.error("###[MqttMsgProcessor processPublish] not exist subscriber.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Topic를 구독 신청한 해당 클라이언트 찾아 메세지 전송
    private void sendPublish(String pubClientID, String subClientID, String topic, AbstractMessage.QOSType qos, ByteBuffer message, boolean retained, int messageID) {
        //TODO: 파일 전송일 경우 처리 방안 구현
        // 예제로 아래는 클라이언트가 파일을 보냈을 경우 원본이미지,썸네일이미지 생성 후 서버에 저장함. 발송대상자에게는 썸네일 발송.

        PublishMessage pubMessage = new PublishMessage();
        pubMessage.setRetainFlag(retained);
        pubMessage.setTopicName(topic);
        pubMessage.setQos(qos);
        pubMessage.setPayload(message.duplicate());
        pubMessage.setMessageID(messageID);

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("###[MqttMsgProcessor sendPublish] send userID : {}, messageID : {}, qos : {}",subClientID,messageID,qos);
        }
        // 해당아이디가 접속된 클라이언트 아이디에 존재 할 경우 발송
        if (ChannelQueue.getInstance().isContainsKey(subClientID)) {
            ServerChannel channel = ChannelQueue.getInstance().getChannel(subClientID).getSession();
            channel.write(pubMessage);
            if(qos!= AbstractMessage.QOSType.MOST_ONE) {
                // 발송한 메세지는 임시 발송정보 캐시에 등록한다(nio이기 때문 컨넥션맵에 있다고 해서 반드시 세션이 유지되고 있다고 볼수 없다.)
                // 따라서 ack가 왔을 때에만 발송완료로 판단하여 삭제한다. 만약 삭제가 되지 않은 메세지는 offlineMessage에 담아 재접속시 재전송하기 위해
                PublishEvent newPublishEvt = new PublishEvent(topic, qos, message, retained, pubClientID, subClientID, messageID, channel);
                //Broker=>client로 발송은 수신자아이디를 넣어야 함.
                CachePublishStore.getInstance().put(subClientID, messageID, newPublishEvt);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("###[MqttMsgProcessor sendPublish] MSG SEND to Subscriber CLIENTID:{}, TOPIC:{}, MSGID:{}, MSG:{} ", subClientID, topic, messageID, DebugUtils.payload2Str(message));
            }
        }else{
            //접속되어 있지 않았으므로 offlinemessage에 저장
            PublishEvent newPublishEvt = new PublishEvent(topic, qos, message, retained, pubClientID, subClientID, messageID, null);
            if(OFFMSG_STORAGE_KIND.equals("1")) {
                redisStorageService.putOffMsg(subClientID, newPublishEvt);
            }else {
                CacheOfflineMsgStore.getInstance().put(subClientID,newPublishEvt);
            }
            //PUSH를 이용하여 메세지를 보내야 한다.
            PushSendWork pushSendWork = new PushSendWork();
            pushSendWork.setAPP_ID(APPID);
            pushSendWork.setMESSAGE(DebugUtils.payload2Str(message));
            pushSendWork.setCUID(subClientID);
            PushSendManager.getInstance().putWork(pushSendWork);
        }
    }

    /**
     * 다른 브로커서버가 위임 전달한 메세지 처리
     * @param evt
     */
    protected void processSystemClientPublish(PublishEvent evt) throws Exception{
        final String topic = evt.getTopic();
        final AbstractMessage.QOSType qos = evt.getQos();
        final ByteBuffer message = evt.getMessage();
        boolean retain = evt.isRetain(); //Offline일 경우 메세지 저장여부

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[MqttMsgProcessor processSystemClientPublish] revMsg : " + DebugUtils.payload2Str(message));
        }
        // 이곳은 다른브로커서버 ==> 해당브로커서버 ==> Client에 전달할 메세지
        if(message.remaining()>10){
            byte[] chkSysMsgBytes = new byte[10];
            message.mark();
            message.get(chkSysMsgBytes);
            String chkSysMsg = new String(chkSysMsgBytes,"utf-8");
            if(chkSysMsg.equals(BrokerConfig.SYS_MSG_SENT_COMPLETE)){
                byte[] revMsgBytes = new byte[message.remaining()];
                message.get(revMsgBytes);
                String revMsg = new String(revMsgBytes,"utf-8");
                String[] messageArr = revMsg.split("\\|");
                String pubClientID = SERVER_ID;
                String subClientID = messageArr[0];
                String sendMsg = messageArr[1];
                sendMsg=BrokerConfig.SYS_MSG_PRIFIX+sendMsg;
                byte[] sendMsgbytes = sendMsg.getBytes();
                ByteBuffer sendMsgByteBuffer = ByteBuffer.allocate(sendMsgbytes.length);
                sendMsgByteBuffer.put(sendMsgbytes);
                sendMsgByteBuffer.flip();
                sendPublish(pubClientID, subClientID, topic, qos, sendMsgByteBuffer, retain, evt.getMessageID());
                return;
            }else if(chkSysMsgBytes.equals(BrokerConfig.SYS_REQ_MSG_SENT_INFO)){
                return;

            }
            message.reset();
        }

        //브로커는 QOS: 1로 publish 메세지의 대해 발송처리시 PubAckEvent를 날려야함.
        if (qos == AbstractMessage.QOSType.LEAST_ONE) {
            try {
                sendPubAck(new PubAckEvent(evt.getMessageID(), evt.getPubClientID()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //QOS:2일 경우는 발송을 하지 않고 스토리지에 발송메세지만 저장하고 SendPubRec메세지를 날리기 때문 구독자에게 발송해서는 안된다.
        if (qos != AbstractMessage.QOSType.EXACTLY_ONCE) {
            try {
                //다른브로커서버로 부터 위임받은 publish메세지 발송이므로 받은메세지에서 받을 클라이언트아이디와 보내는 클라이언트아이디를 메세지정보 구해서 셋팅.
                int clientIDInfoLen = message.getInt();
                byte[] clientIDInfoByte = new byte[clientIDInfoLen];
                message.get(clientIDInfoByte);
                String clientIDInfo = new String(clientIDInfoByte, "utf-8");
                String[] clientIDInfoArr = clientIDInfo.split("\\|");
                String pubClientID = clientIDInfoArr[0];
                String subClientID = clientIDInfoArr[1];

                // 발송원본메세지 추출
                byte[] orgMsgBytes = new byte[message.remaining()];
                message.get(orgMsgBytes);
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("###[MqttMsgProcessor processSystemClientPublish] PUB_CLIENTID:{}, TOPIC:{}, MSGID:{}, MSG:{} ", evt.getPubClientID(), evt.getTopic(), evt.getMessageID(), new String(orgMsgBytes,"utf-8"));
                }
                // 웝본메세지 Bytebuffer로 만듬
                ByteBuffer orgMsgBuffer = ByteBuffer.allocate(orgMsgBytes.length);
                orgMsgBuffer.put(orgMsgBytes);
                orgMsgBuffer.flip();

                sendPublish(pubClientID, subClientID, evt.getTopic(), AbstractMessage.QOSType.LEAST_ONE, orgMsgBuffer, false, evt.getMessageID());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    //구독신청자가 브로커서버에 Ack전송. Qos 1 or 2일때  해당 메세지 전송 완료후 호출되는 ack를 통해 임시발송저장 메세지 삭제처리 하고 수신확인 정보 보낸다.
    protected void processPubAck(String subClientID, int messageID) {
        if(LOGGER_PUBACK.isDebugEnabled()) {
            LOGGER_PUBACK.debug("Client ID : {}, messageID : {}",subClientID,messageID);
        }
        try {
            String pubClientID = null;
            // 발송자 메세지 임시보관함에 발송확인 카운트 올리기 위해 임시발송메모리캐쉬(EHCache)에 수신자아이디로 발송메세지를 가져온다.
            Element element = CachePublishStore.getInstance().get(subClientID, messageID);
            if(element==null){
                // 시발송메모리캐쉬(EHCache)에 없으므로 OFF메세지로 이동하여 없을수 있으므로 OFF메세지에서 삭제 하도록 처리.
                if(OFFMSG_STORAGE_KIND.equals("1")){
                    // redis에 있는 offmessage 삭제
                    pubClientID = redisStorageService.removeOffMsg(subClientID, messageID);

                }else {
                    // ehcache에 있는 offmessage 삭제
                    pubClientID = CacheOfflineMsgStore.getInstance().remove(subClientID, messageID);
                }
            }else{
                PublishEvent publishEvent = (PublishEvent)element.getObjectValue();
                pubClientID = publishEvent.getPubClientID();
                //저장된 SUB_clientID+messageID로 저장된 발송성공확인용 임시발송메모리캐쉬(EHCache)에서 삭제하여 OFF메세지로 가지 않게 삭제처리.
                CachePublishStore.getInstance().remove(subClientID, messageID);
            }
            //TODO : 메세지 발송 사용자에게 발송완료카운트를 알려주고 싶다. 어떻게 하는것이 가장 합리적인가?
            // 방안 1. ACK가 들어올때 마다 해당 pubClientID+messageID를 키로 ACK카운트를 빼고 0이 되었을시 발행자에 통지한다. 그 전에는 클라이요청시에 응답한다.

            if(pubClientID!=null) {
                redisStorageService.upPubMsgAckCnt(APPID,pubClientID,messageID,SERVER_ID);
            }else{
                if(LOGGER.isErrorEnabled()){
                    LOGGER.error("###[MqttMsgProcessor processPubAck] CachePublishStore not exist and offMessage Store not exist. subClientID :{} , messageID:{}",subClientID,messageID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OffMessage에 저장된 못 보낸 메세지 즉시 발송한다.
     * @param clientID
     */
    private void offMsgRepublish(String clientID) {
        // OFFMSG ==> REDIS 사용시
        if(OFFMSG_STORAGE_KIND.equals("1")){
            List<OffMsgBean> offMsgBeans = redisStorageService.getSendOffMsgList(clientID);
            if (offMsgBeans == null) {
                return;
            }
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("###[MqttMsgProcessor offMsgRepublish] redis offmessage => clientID : {} offMsgBeans size : {}",clientID,offMsgBeans.size());
            }
            for (OffMsgBean offMsgBean : offMsgBeans) {
                //qos를 1로 발송한다.
                try {
                    byte[] offmsgPayload = offMsgBean.getPub_message().getBytes("utf-8");
                    ByteBuffer offmsgBuffer = (ByteBuffer) ByteBuffer.allocate(offmsgPayload.length).put(offmsgPayload).flip();
                    sendPublish(offMsgBean.getPubClientID(), clientID, offMsgBean.getTopic(), AbstractMessage.QOSType.LEAST_ONE, offmsgBuffer, false, Integer.parseInt(offMsgBean.getMsgID()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }else{
            // OFFMSG ==> ehcache
            List<PublishEvent> publishEventList = CacheOfflineMsgStore.getInstance().get(clientID);
            if (publishEventList == null) {
                return;
            }
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("###[MqttMsgProcessor offMsgRepublish] ehcache offmessage => clientID : {} offMsgBeans size : {}",clientID,publishEventList.size());
            }
            for (PublishEvent publishEvent : publishEventList) {
                //qos를 1로 발송한다.
                byte[] offmsgPayload = publishEvent.getPub_message().getBytes();
                ByteBuffer offmsgBuffer = (ByteBuffer) ByteBuffer.allocate(offmsgPayload.length).put(offmsgPayload).flip();
                sendPublish(publishEvent.getPubClientID(), clientID, publishEvent.getTopic(), AbstractMessage.QOSType.LEAST_ONE, offmsgBuffer, false, publishEvent.getMessageID());
            }
            //재접속시 보내지 못한 저장 메세지 보내고 지움
            CacheOfflineMsgStore.getInstance().remove(clientID);
        }
    }

    /**
     * 클라이언트에서 발송한 Publish Msg에 대해 Broker가 클라이언트에 응답ACK를 보냄.
     * @param evt
     * @throws Exception
     */
    private void sendPubAck(PubAckEvent evt) throws Exception{
        String clientId = evt.getClientID();

        PubAckMessage pubAckMessage = new PubAckMessage();
        pubAckMessage.setMessageID(evt.getMessageId());

        try {
            if (!ChannelQueue.getInstance().isContainsKey(clientId)) {
                throw new Exception(String.format("Can't find a ConnectionID for client %s in clientIDSessionMap", clientId));
            }
            ChannelQueue.getInstance().getChannel(clientId).getSession().write(pubAckMessage);
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("###[MqttMsgProcessor sendPubAck] cileitID : {} PUB ACK Send!",clientId);
            }
        }catch(Throwable t) {
            LOGGER.error(null, t);
        }
    }

    protected void processPubRel(String pubClientID, int messageID) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[MqttMsgProcessor processPubRel] PUB --PUBREL-> SRV processPubRel invoked for clientID {} ad messageID {}", pubClientID, messageID);
        }
        Element element = CacheQos2Store.getInstance().get(pubClientID, messageID);
        if(element!=null){
            PublishEvent evt = (PublishEvent)element.getObjectValue();
            final String topic = evt.getTopic();
            final AbstractMessage.QOSType qos = evt.getQos();
            publish2Subscribers(evt);

            try {
                CacheQos2Store.getInstance().remove(pubClientID, messageID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (evt.isRetain()) {
                redisStorageService.putRetainMsg(topic, evt);
            }

            sendPubComp(pubClientID, messageID);
        }
    }

    private void sendPubComp(String clientID, int messageID) {
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[MqttMsgProcessor sendPubComp] PUB <-PUBCOMP-- SRV sendPubComp invoked for clientID {} ad messageID {}", clientID, messageID);
        }
        PubCompMessage pubCompMessage = new PubCompMessage();
        pubCompMessage.setMessageID(messageID);

        ChannelQueue.getInstance().getChannel(clientID).getSession().write(pubCompMessage);
    }

    /**
     * QOS 2 Publish일 경우 스토리자에 저장 후 저장 완료 했다는 메세지를 클라이언트에 전송
     * @param pubClientID
     * @param messageID
     */
    private void sendPubRec(String pubClientID, int messageID) {
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[MqttMsgProcessor sendPubRec] PUB <--PUBREC-- SRV sendPubRec invoked for clientID {} with messageID {}", pubClientID, messageID);
        }
        PubRecMessage pubRecMessage = new PubRecMessage();
        pubRecMessage.setMessageID(messageID);
        ChannelQueue.getInstance().getChannel(pubClientID).getSession().write(pubRecMessage);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UnSubscribe 관련 함수 모음
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void processUnsubscribe(ServerChannel session, String clientID, List<String> topics, int messageID) {
        //해당아이디가 구독신청한 토픽 구독정보 삭제
        for (String topic : topics) {
            //캐시에서 삭제
            subscribeStore.removeSubscription(topic, clientID);
            //Offline 메세지들도 삭제
            CacheOfflineMsgStore.getInstance().remove(topic);
        }
        //ack the client
        UnsubAckMessage ackMessage = new UnsubAckMessage();
        ackMessage.setMessageID(messageID);
        session.write(ackMessage);
    }


    //구독 메세지 호출시 호출됨
    protected void processSubscribe(ServerChannel session, SubscribeMessage msg, String clientID, boolean cleanSession) {
        LOGGER.debug("###[MqttMsgProcessor processSubscribe] SubscribeInfo :" + clientID + " MSG ID:" + msg.getMessageID());
        for (SubscribeMessage.Couple req : msg.subscriptions()) { //요청한 구독리스트 정보를 저장한다.
            AbstractMessage.QOSType qos = AbstractMessage.QOSType.values()[req.getQos()];
            Subscription newSubscription = new Subscription(clientID, req.getTopic(), qos, cleanSession);
            //요청한 Topic의 구독정보를 하나씩 등록한다.
            subscribeSingleTopic(newSubscription, req.getTopic());
        }

        //ack the client
        SubAckMessage ackMessage = new SubAckMessage();
        ackMessage.setMessageID(msg.getMessageID());

        //reply with requested qos
        for(SubscribeMessage.Couple req : msg.subscriptions()) {
            AbstractMessage.QOSType qos = AbstractMessage.QOSType.values()[req.getQos()];
            ackMessage.addType(qos);
        }
        session.write(ackMessage);
    }

    // 구독시청한 토픽을  구독(키:토픽)에 해당 사용자를 등록한다.
    private void subscribeSingleTopic(Subscription newSubscription, final String topic) {
        // 캐시 메모리에 저장
        subscribeStore.addNewSubscription(newSubscription);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void processDisconnect(ServerChannel session, String clientID, boolean cleanSession) throws InterruptedException {
        ChannelQueue.getInstance().removeChannel(clientID);
        session.close(true);
    }

    // ping이 해당 시간 동안 오지 않을 경우. 클라이언트가 체널 close()를 한 경우. 메세지를 발송할 때 클라이언트 채널이 연결되어 있지 않을때
    protected void proccessConnectionLost(String clientID) {
        if (ChannelQueue.getInstance().removeChannel(clientID) != null) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("###[MqttMsgProcessor proccessConnectionLost] Lost connection with client <{}>", clientID);
            }
        }
    }

    public void processPing(String clientID) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[MqttMsgProcessor processPing] ping : {}", clientID);
        }
    }

    public void onEvent(ValueEvent t, long l, boolean bln) throws Exception {
        MessagingEvent evt = t.getEvent();
        OutputMessagingEvent outEvent = (OutputMessagingEvent) evt;
        outEvent.getChannel().write(outEvent.getMessage());
    }

    protected int getConnetedUserCnt(){
        return ChannelQueue.getInstance().getSize();
    }

    protected boolean isExistConnectionMap(String clientID){
        boolean retrunValue = false;
        if(ChannelQueue.getInstance().isContainsKey(clientID)){
            retrunValue = true;
        }
        return retrunValue;
    }
}
