package com.msp.chat.core.mqtt.proto.messages;

/**
 * Doesn't care DUP, QOS and RETAIN flags.
 * 
 * @author andrea
 */
public class DisconnectMessage extends ZeroLengthMessage {
    
    public DisconnectMessage() {
        m_messageType = AbstractMessage.DISCONNECT;
    }
}
