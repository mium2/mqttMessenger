package com.msp.chat.server.bean.events;

import com.msp.chat.server.netty.ServerChannel;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;

/**
 *
 * @author andrea
 */
public class OutputMessagingEvent extends MessagingEvent {
    private ServerChannel m_channel;
    private AbstractMessage m_message;

    public OutputMessagingEvent(ServerChannel channel, AbstractMessage message) {
        m_channel = channel;
        m_message = message;
    }

    public ServerChannel getChannel() {
        return m_channel;
    }

    public AbstractMessage getMessage() {
        return m_message;
    }
    
    
}
