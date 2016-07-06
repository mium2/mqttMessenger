package com.msp.chat.server.bean.events;

import com.msp.chat.server.netty.ServerChannel;

/**
 *
 * @author andrea
 */
public class DisconnectEvent extends MessagingEvent {
    
    ServerChannel m_session;
    
    public DisconnectEvent(ServerChannel session) {
        m_session = session;
    }

    public ServerChannel getSession() {
        return m_session;
    }
    
    
}
