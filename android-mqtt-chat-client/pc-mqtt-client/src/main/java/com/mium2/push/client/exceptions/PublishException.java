package com.mium2.push.client.exceptions;

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
