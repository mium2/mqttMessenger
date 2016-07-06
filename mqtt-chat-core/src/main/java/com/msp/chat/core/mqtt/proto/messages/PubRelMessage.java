package com.msp.chat.core.mqtt.proto.messages;

/**
 *
 * @author andrea
 */
public class PubRelMessage extends MessageIDMessage {
    
    public PubRelMessage() {
        m_messageType = AbstractMessage.PUBREL;
    }
}
