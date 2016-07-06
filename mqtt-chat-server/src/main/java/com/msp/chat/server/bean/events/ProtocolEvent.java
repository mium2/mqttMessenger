package com.msp.chat.server.bean.events;

import com.msp.chat.server.netty.ServerChannel;
import com.msp.chat.core.mqtt.proto.Utils;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;

/**
 * Event used to carry ProtocolMessages from front handler to event processor
 */
public class ProtocolEvent extends MessagingEvent {
    ServerChannel m_session;
    AbstractMessage message;

    public ProtocolEvent(ServerChannel session, AbstractMessage message) {
        this.m_session = session;
        this.message = message;
    }

    public ServerChannel getSession() {
        return m_session;
    }

    public AbstractMessage getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "ProtocolEvent wrapping " + Utils.msgType2String(message.getMessageType());
    }
}
