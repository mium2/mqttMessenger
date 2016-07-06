package com.msp.chat.server.bean.events;

import com.msp.chat.server.bean.Subscription;

/**
 *
 * @author andrea
 */
public class SubscribeEvent extends MessagingEvent {

    Subscription m_subscription;

    int m_messageID;
    
    public SubscribeEvent(Subscription subscription, int messageID) {
        m_subscription = subscription;
        m_messageID = messageID;
    }

    public Subscription getSubscription() {
        return m_subscription;
    }

    public int getMessageID() {
        return m_messageID;
    }
}
