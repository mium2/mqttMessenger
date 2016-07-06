package com.msp.chat.server.worker;

import com.msp.chat.core.mqtt.proto.messages.*;
import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.bean.events.*;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.netty.ServerChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 4. 11..
 */
public class MqttMsgWorkerThread extends Thread{
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
    private String ThreadName;
    private final MqttMsgWorkerManager mqttMsgWorkerManager;
    private boolean isRun = true;
    private final MqttMsgProcessor mqttMsgProcessor;

    public MqttMsgWorkerThread(String name, MqttMsgWorkerManager _mqttMsgWorkerManager, IAuthenticator authenticator){
        super(name);
        this.ThreadName=getName();
        this.mqttMsgWorkerManager=_mqttMsgWorkerManager;
        this.mqttMsgProcessor = new MqttMsgProcessor();
        mqttMsgProcessor.init(authenticator);
    }
    public void run(){
        while(isRun){
            ///push 전송 로직
            ValueEvent valueEvent = null;
            try {
                valueEvent = mqttMsgWorkerManager.takeMqttMsg();
                MessagingEvent evt = valueEvent.getEvent();
                if (evt instanceof PublishEvent) {
                    mqttMsgProcessor.processPublish((PublishEvent) evt);
                } else if (evt instanceof StopEvent) {
                    mqttMsgWorkerManager.processStop();
                } else if (evt instanceof DisconnectEvent) {
                    LOGGER.debug("###[MqttMsgWorkerThread run] DisconnectEvent");
                    DisconnectEvent disEvt = (DisconnectEvent) evt;
                    String clientID = (String) disEvt.getSession().getAttribute(Constants.ATTR_CLIENTID);
                    mqttMsgProcessor.processDisconnect(disEvt.getSession(), clientID, false);
                } else if (evt instanceof ProtocolEvent) {
                    ServerChannel session = ((ProtocolEvent) evt).getSession();
                    AbstractMessage message = ((ProtocolEvent) evt).getMessage();
                    // 클라이언트에서 넘어온 메세지가 CONNECT 일경우
                    if (message instanceof ConnectMessage) {
                        mqttMsgProcessor.processConnect(session, (ConnectMessage) message);
                    } else if (message instanceof PublishMessage) {
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        PublishEvent pubEvt = new PublishEvent((PublishMessage) message, clientID, null, session);
                        // 다른 브로커서버에서 발송 위임한 Publish메세지인지 체크
                        if(clientID.startsWith(BrokerConfig.SYSTEM_BROKER_CLIENT_PRIFIX)){
                            // 다른 브로커에서 발송 위임한 메세지는 payload에 발송자아이디와 수신자아이디 정보가 있음.
                            mqttMsgProcessor.processSystemClientPublish(pubEvt);
                        }else {
                            mqttMsgProcessor.processPublish(pubEvt);
                        }
                    } else if (message instanceof DisconnectMessage) {
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        boolean cleanSession = (Boolean) session.getAttribute(Constants.CLEAN_SESSION);
                        mqttMsgProcessor.processDisconnect(session, clientID, cleanSession);
                    } else if (message instanceof UnsubscribeMessage) {
                        UnsubscribeMessage unsubMsg = (UnsubscribeMessage) message;
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        mqttMsgProcessor.processUnsubscribe(session, clientID, unsubMsg.topics(), unsubMsg.getMessageID());
                    } else if (message instanceof SubscribeMessage) {
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        boolean cleanSession = (Boolean) session.getAttribute(Constants.CLEAN_SESSION);
                        mqttMsgProcessor.processSubscribe(session, (SubscribeMessage) message, clientID, cleanSession);
                    }else if (message instanceof PubRelMessage) {
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        int messageID = ((PubRelMessage) message).getMessageID();
                        mqttMsgProcessor.processPubRel(clientID, messageID);
                    } else if (message instanceof PubAckMessage) {
                        String clientID = (String) session.getAttribute(Constants.ATTR_CLIENTID);
                        int messageID = ((PubAckMessage) message).getMessageID();
                        mqttMsgProcessor.processPubAck(clientID, messageID);
                    } else {
                        throw new RuntimeException("Illegal message received " + message);
                    }
                    //서버가 시작되면 InitEvent를 날려준다.
                }else if (evt instanceof LostConnectionEvent) {
                    LOGGER.debug("###[MqttMsgWorkerThread run] Lost Connection ~~!");
                    LostConnectionEvent lostEvt = (LostConnectionEvent) evt;
                    mqttMsgProcessor.proccessConnectionLost(lostEvt.getClientID());
                }
            }catch (InterruptedException ex){
                LOGGER.debug("###[MqttMsgWorkerThread run] "+ThreadName+" InterruptedException 발생");
                break;
            }catch (Exception e){
                LOGGER.debug("###[MqttMsgWorkerThread run]" + ThreadName + " End :" + e.getMessage());
            }finally {
                valueEvent = null;
            }
        }
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }
}
