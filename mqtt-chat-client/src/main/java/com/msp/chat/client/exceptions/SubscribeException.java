package com.msp.chat.client.exceptions;
/**
 *
 * @author andrea
 */
public class SubscribeException extends ConnectionException {

    public SubscribeException(String msg) {
        super(msg);
    }

    public SubscribeException(Throwable e) {
        super(e);
    }
}
