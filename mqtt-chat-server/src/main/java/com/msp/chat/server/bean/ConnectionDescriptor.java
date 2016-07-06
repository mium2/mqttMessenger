package com.msp.chat.server.bean;

import com.msp.chat.server.netty.ServerChannel;

/**
 * Created by Y.B.H(mium2) on 15. 8. 25..
 */
public class ConnectionDescriptor {
    
    private String m_clientID;
    private ServerChannel m_session;
    private boolean m_cleanSession;
    
    public ConnectionDescriptor(String clientID, ServerChannel session, boolean cleanSession) {
        this.m_clientID = clientID;
        this.m_session = session;
        this.m_cleanSession = cleanSession;
    }
    
    public boolean isCleanSession() {
        return m_cleanSession;
    }

    public String getClientID() {
        return m_clientID;
    }

    public ServerChannel getSession() {
        return m_session;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptor{" + "m_clientID=" + m_clientID + ", m_cleanSession=" + m_cleanSession + '}';
    }
}
