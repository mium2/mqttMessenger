package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.proto.messages.MessageIDMessage;
import com.msp.chat.core.mqtt.proto.messages.PubAckMessage;

/**
 *
 * @author andrea
 */
class PubAckDecoder extends MessageIDDecoder {

    @Override
    protected MessageIDMessage createMessage() {
        return new PubAckMessage();
    }
    
}
