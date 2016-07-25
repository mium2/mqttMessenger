package com.msp.chat.server.worker;

import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.bean.WebSocketMsgBean;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.netty.NettyChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            NettyChannel nettyChannel = (NettyChannel)webSocketMsgBean.getServerChannel();
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

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }
}
