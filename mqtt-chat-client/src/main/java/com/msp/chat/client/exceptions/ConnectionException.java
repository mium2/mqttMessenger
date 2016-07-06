package com.msp.chat.client.exceptions;

import com.msp.chat.core.mqtt.MQTTException;

/**
 *
 * @author andrea
 */
public class ConnectionException extends MQTTException {

    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException(Throwable e) {
        super(e);
    }
}
