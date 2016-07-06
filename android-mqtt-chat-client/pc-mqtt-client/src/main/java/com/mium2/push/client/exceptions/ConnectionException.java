package com.mium2.push.client.exceptions;


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
