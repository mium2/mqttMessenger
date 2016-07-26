package com.msp.chat.server.worker;

import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.bean.ConnectionDescriptor;
import com.msp.chat.server.bean.WebSocketMsgBean;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.netty.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Y.B.H(mium2) on 16. 7. 19..
 */
public class WebSocketWorkerThread extends Thread{
    private Logger LOGGER = LoggerFactory.getLogger("server");
    private String ThreadName;
    private final WebSocketMsgManager webSocketMsgManager;
    private boolean isRun = true;
    private final MqttMsgProcessor mqttMsgProcessor;

    public WebSocketWorkerThread(String name, WebSocketMsgManager _webSocketMsgManager, IAuthenticator authenticator){
        super(name);
        this.ThreadName=getName();
        this.webSocketMsgManager=_webSocketMsgManager;
        this.mqttMsgProcessor = new MqttMsgProcessor();
        mqttMsgProcessor.init(authenticator);
    }
    public void run(){
        while(isRun){
            /// 전송 로직
            WebSocketMsgBean webSocketMsgBean = null;
            try {
                webSocketMsgBean = webSocketMsgManager.takeWebSocketMsgBean();
                if(webSocketMsgBean.getCommand().equals("CONNECT")){
                    processConnect(webSocketMsgBean);
                }else if(webSocketMsgBean.getCommand().equals("PUBACK")){
                    processPubAck(webSocketMsgBean);
                }else if(webSocketMsgBean.getCommand().equals("PUBLISH")){
                    processPublish(webSocketMsgBean);
                }else if(webSocketMsgBean.getCommand().equals(BrokerConfig.SYS_MSG_SENT_COMPLETE)){
                    processPubackComplete(webSocketMsgBean);
                }else if(webSocketMsgBean.getCommand().equals(BrokerConfig.SYS_REQ_MSG_FILE)){
                    processAttachFile(webSocketMsgBean);
                }
            }catch (Exception e){
                LOGGER.debug("###[WebSocketWorkerThread run]" + ThreadName + " End :" + e.getMessage());
            }finally {
//                webSocketMsgBean = null;
            }
        }
    }

    private void processConnect(WebSocketMsgBean webSocketMsgBean){
        String connectID = webSocketMsgBean.getRequestArr()[1];
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("###[WebSocketWorkerThread processConnect] connectID:{}",connectID);
        }
        if(!isConnectAllow(connectID)){
            webSocketMsgBean.getServerChannel().write(new TextWebSocketFrame("CONACK|401|등록 사용자가 아니거나 접속 가능서버가 아닙니다"));
            webSocketMsgBean.getServerChannel().close(false);
        }else{
            if(WebsocketClientIdCtxManager.getInstance().isContainsKey(connectID)){
                ChannelHandlerContext oldCtx = WebsocketClientIdCtxManager.getInstance().getChannel(connectID);
                NettyChannel oldNettyChannel = WebsocketCtxServerHandleManager.getInstance().getNettyChannel(oldCtx);
                oldNettyChannel.setAttribute(Constants.DOUBLE_LOGIN,true);
                oldCtx.writeAndFlush(new TextWebSocketFrame("CONACK|402|해당 아이디로 다른곳에서 접속하여 끊겼습니다."));
                oldCtx.close();
            }

            NettyChannel nettyChannel = (NettyChannel) webSocketMsgBean.getServerChannel();
            nettyChannel.setAttribute(Constants.ATTR_CLIENTID, connectID);
            WebsocketClientIdCtxManager.getInstance().putChannel(connectID, nettyChannel.getChannelHandlerContext());
            webSocketMsgBean.getServerChannel().write(new TextWebSocketFrame("CONACK|200|SUCCESS"));
        }
    }

    private void processPublish(WebSocketMsgBean webSocketMsgBean){
        String connectID = webSocketMsgBean.getRequestArr()[1];
        int messageId = Integer.parseInt(webSocketMsgBean.getRequestArr()[2]);
        String topic = webSocketMsgBean.getRequestArr()[3];
        String message = webSocketMsgBean.getRequestArr()[4];
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("###[WebSocketWorkerThread processPublish] connectID:{} messageId:{} topic:{} message:{}",connectID,messageId,topic,message);
        }
        // PubAck 전달해줌.
        webSocketMsgBean.getServerChannel().write(new TextWebSocketFrame("PUBACK|"+messageId));
        // 발송을위해 Mqtt브로커 서버 메세지큐에 PUBLISH메세지를 넣는다.
        PublishEvent publishEvent = new PublishEvent();
        publishEvent.setM_msgID(messageId);
        publishEvent.setPub_message(message);
        publishEvent.setRetainYN("N");
        publishEvent.setM_topic(topic);
        publishEvent.setPub_qos(1);
        publishEvent.setRetainYN("N");
        publishEvent.setPubClientID(connectID);
        mqttMsgProcessor.webSocketPublish2Subscribers(publishEvent);
    }

    // websocket 클라이언트로 publishAck를 처리한다. Redis에서 카운트 뺀 후 메세지 발송자에게 통지한다.
    private void processPubAck(WebSocketMsgBean webSocketMsgBean){
        String publisherid = webSocketMsgBean.getRequestArr()[1];
        String messageid = webSocketMsgBean.getRequestArr()[2];
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("###[WebSocketWorkerThread processPubAck] messageId:{} publisherid : {} ",messageid,publisherid);
        }
        mqttMsgProcessor.processWebsocketPubAck(publisherid, Integer.parseInt(messageid));
    }

    private  void processPubackComplete(WebSocketMsgBean webSocketMsgBean){
        String command = webSocketMsgBean.getRequestArr()[0];
        String connectID = webSocketMsgBean.getRequestArr()[1];
        String messageId = webSocketMsgBean.getRequestArr()[2];
        String topic = webSocketMsgBean.getRequestArr()[3];
        String message = webSocketMsgBean.getRequestArr()[4];
        StringBuilder sendSB = new StringBuilder(command);
        sendSB.append("|"+topic);
        sendSB.append("|"+messageId);
        sendSB.append("|"+message);
        webSocketMsgBean.getServerChannel().write(new TextWebSocketFrame(sendSB.toString()));
    }

    private boolean isConnectAllow(String clientID){
        boolean returnValue = false;
        try {
            Object obj = webSocketMsgManager.getRedisStorageService().getUserAsignBrokerID(clientID);
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("###[WebSocketWorkerThread isConnectAllow] redis allow borkerID:"+obj.toString()+"  this brokerID: "+webSocketMsgManager.getSERVER_ID());
            }
            if (obj != null && obj.toString().equals(webSocketMsgManager.getSERVER_ID())) {
                returnValue = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }

    private void processAttachFile(WebSocketMsgBean webSocketMsgBean){
        String connectID = webSocketMsgBean.getRequestArr()[1];
        String messageId = webSocketMsgBean.getRequestArr()[2];
        String topic = webSocketMsgBean.getRequestArr()[3];
        String message = webSocketMsgBean.getRequestArr()[4];
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("###[WebSocketWorkerThread processPublish] connectID:{} messageId:{} topic:{} message:{}",connectID,messageId,topic,message);
        }

        // 파일보내기 메세지 처리
        try {
            String rev_fileName = webSocketMsgBean.getFilename();
            int lastIndex = rev_fileName.lastIndexOf(".");
            String fileExtention = rev_fileName.substring(lastIndex+1).toLowerCase();
            String saveFileFullSrc;
            String downloadUrl;
            if(webSocketMsgManager.getCHKIMGSET().contains(fileExtention)){
                saveFileFullSrc = BrokerConfig.getProperty(BrokerConfig.FILE_SAVE_SRC)+"images/";
                downloadUrl = BrokerConfig.getProperty(BrokerConfig.DOWNLOAD_HOSTURL)+"images/";
            }else{
                saveFileFullSrc = BrokerConfig.getProperty(BrokerConfig.FILE_SAVE_SRC)+"etc/";
                downloadUrl = BrokerConfig.getProperty(BrokerConfig.DOWNLOAD_HOSTURL)+"etc/";
            }
            rev_fileName = System.currentTimeMillis()+"."+fileExtention;
            String orgFileFullSrc = saveFileFullSrc+rev_fileName;
            FileOutputStream fot = new FileOutputStream(orgFileFullSrc);
            fot.write(webSocketMsgBean.getAttachFile());
            fot.close();

            File thumbFile = null;
            // 이미지 파일 일때만 썸네일 이미지 만들기
            if(webSocketMsgManager.getCHKIMGSET().contains(fileExtention)) {
                thumbFile = new File(saveFileFullSrc+"thumb/"+rev_fileName);
                BufferedImage buffer_original_image = ImageIO.read(new File(orgFileFullSrc));
                int orgImgHeight = buffer_original_image.getHeight();
                int orgImgWidth = buffer_original_image.getWidth();
                int THUMBNAIL_HEIGHT = Integer.parseInt(BrokerConfig.getProperty(BrokerConfig.THUMBNAIL_HEIGHT));
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

            StringBuilder sendSB = new StringBuilder();
            sendSB.append(BrokerConfig.SYS_RES_MSG_FILE);
            sendSB.append("|");
            sendSB.append(connectID);
            sendSB.append("|");
            sendSB.append(messageId);
            sendSB.append("|");
            sendSB.append(fileExtention);
            sendSB.append("|");
            sendSB.append(downloadUrl+rev_fileName);
            if(thumbFile!=null){
                sendSB.append("|");
                sendSB.append(downloadUrl+"thumb/"+rev_fileName);
            }

            // PubAck 전달해줌.
            webSocketMsgBean.getServerChannel().write(new TextWebSocketFrame("PUBACK|"+messageId));
            // 발송을위해 Mqtt브로커 서버 메세지큐에 PUBLISH메세지를 넣는다.
            PublishEvent publishEvent = new PublishEvent();
            publishEvent.setM_msgID(Integer.parseInt(messageId));
            publishEvent.setPub_message(sendSB.toString());
            publishEvent.setRetainYN("N");
            publishEvent.setM_topic(topic);
            publishEvent.setPub_qos(1);
            publishEvent.setRetainYN("N");
            publishEvent.setPubClientID(connectID);
            mqttMsgProcessor.webSocketPublish2Subscribers(publishEvent);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }
}
