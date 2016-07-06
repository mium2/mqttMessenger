package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.proto.messages.MessageIDMessage;
import com.msp.chat.core.mqtt.proto.messages.PubRelMessage;

/**
 *
 * @author andrea
 */
class PubRelDecoder extends MessageIDDecoder {

    @Override
    protected MessageIDMessage createMessage() {
        return new PubRelMessage();
    }

}

