package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.proto.messages.MessageIDMessage;
import com.msp.chat.core.mqtt.proto.messages.PubCompMessage;


/**
 *
 * @author andrea
 */
class PubCompDecoder extends MessageIDDecoder {

    @Override
    protected MessageIDMessage createMessage() {
        return new PubCompMessage();
    }
}
