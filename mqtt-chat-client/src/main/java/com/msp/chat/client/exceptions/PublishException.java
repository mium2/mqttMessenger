package com.msp.chat.client.exceptions;

/**
 *
 * @author andrea
 */
public class PublishException extends ConnectionException {

    public PublishException(String msg) {
        super(msg);
    }

    public PublishException(Throwable e) {
        super(e);
    }
}
