package com.mium2.push.client.exceptions;

/**
 *
 * @author andrea
 */
public class MQTTException extends RuntimeException {

    public MQTTException() {
        super();
    }
    
    public MQTTException(String msg) {
        super(msg);
    }
    
    public MQTTException(Throwable cause) {
        super(cause);
    }

    public MQTTException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
