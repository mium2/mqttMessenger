package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.proto.messages.MessageIDMessage;
import com.msp.chat.core.mqtt.proto.messages.UnsubAckMessage;

/**
 *
 * @author andrea
 */
class UnsubAckDecoder extends MessageIDDecoder {

    @Override
    protected MessageIDMessage createMessage() {
        return new UnsubAckMessage();
    }
}

