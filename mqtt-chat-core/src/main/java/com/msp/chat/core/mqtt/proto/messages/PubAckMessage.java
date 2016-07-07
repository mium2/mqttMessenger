package com.msp.chat.core.mqtt.proto.messages;

/**
 * Placeholder for PUBACK message.
 * 
 * @author mium2(Y.B.H)
 */
public class PubAckMessage extends MessageIDMessage {
    public PubAckMessage() {
        m_messageType = AbstractMessage.PUBACK;
    }
}
