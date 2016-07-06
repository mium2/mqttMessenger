package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.proto.messages.MessageIDMessage;
import com.msp.chat.core.mqtt.proto.messages.PubRecMessage;

/**
 *
 * @author andrea
 */
class PubRecDecoder extends MessageIDDecoder {

    @Override
    protected MessageIDMessage createMessage() {
        return new PubRecMessage();
    }
}
