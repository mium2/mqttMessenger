package com.msp.chat.client;

import com.msp.chat.core.mqtt.proto.messages.*;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */
public class ClientMQTTHandler extends ChannelHandlerAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(ClientMQTTHandler.class);
    
    Client m_callback;

    ClientMQTTHandler(Client callback)  {
        m_callback = callback;
    } 
    
    @Override
    public void channelRead(ChannelHandlerContext session, Object message) throws Exception {
        AbstractMessage msg = (AbstractMessage) message;
        System.out.println("Received a message of type " + msg.getMessageType());
        switch (msg.getMessageType()) {
            case AbstractMessage.CONNACK:
                handleConnectAck(session, (ConnAckMessage) msg);
                System.out.println("#### CONNACK MSG RECEIVED!");
                break;
            case AbstractMessage.SUBACK:
                handleSubscribeAck(session, (SubAckMessage) msg);
                System.out.println("#### SUBACK MSG RECEIVED!");
                break;
            case AbstractMessage.UNSUBACK:
                handleUnsubscribeAck(session, (UnsubAckMessage) msg);
                System.out.println("#### UNSUBACK MSG RECEIVED!");
                break;
//            case SUBSCRIBE:
//                handleSubscribe(session, (SubscribeMessage) msg);
//        break;
            case AbstractMessage.PUBLISH:
                handlePublish(session, (PublishMessage) msg);
                System.out.println("#### PUBLISH MSG RECEIVED!");
                break;
            case AbstractMessage.PUBACK:
                handlePublishAck(session, (PubAckMessage) msg);
                System.out.println("#### PUBACK MSG RECEIVED!");
                break;
            case AbstractMessage.PINGRESP:
                System.out.println("#### PINGRESP MSG RECEIVED!");
                break;
        }
    }

    private void handlePublishAck(ChannelHandlerContext session, PubAckMessage msg) {
        m_callback.publishAckCallback(msg.getMessageID());
    }

    private void handleConnectAck(ChannelHandlerContext session, ConnAckMessage connAckMessage) {
        m_callback.connectionAckCallback(connAckMessage.getReturnCode());
    }

    private void handleSubscribeAck(ChannelHandlerContext session, SubAckMessage subAckMessage) {
        m_callback.subscribeAckCallback(subAckMessage.getMessageID());
    }
    
    private void handlePublish(ChannelHandlerContext session, PublishMessage pubMessage) {
//        m_callback.publishCallback(pubMessage.getTopicName(), pubMessage.getPayload());

        m_callback.publishCallback(pubMessage);
    }

    private void handleUnsubscribeAck(ChannelHandlerContext session, UnsubAckMessage unsubAckMessage) {
        m_callback.unsubscribeAckCallback(unsubAckMessage.getMessageID());
    }
}
