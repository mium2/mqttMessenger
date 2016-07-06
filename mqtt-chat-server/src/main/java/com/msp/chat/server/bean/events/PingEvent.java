package com.msp.chat.server.bean.events;

/**
 * Created by Y.B.H(mium2) on 16. 4. 11..
 */
public class PingEvent extends MessagingEvent {
    private String m_clientID;

    public PingEvent(String clienID) {
        m_clientID = clienID;
    }

    public String getClientID() {
        return m_clientID;
    }

}