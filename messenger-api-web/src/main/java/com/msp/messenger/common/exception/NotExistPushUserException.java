package com.msp.messenger.common.exception;

import java.io.Serializable;

/**
 * Created by Y.B.H(mium2) on 16. 6. 2..
 */
public class NotExistPushUserException extends Exception implements Serializable {


    private static final long serialVersionUID = -8509316809193018509L;

    public NotExistPushUserException(){}
    
    public NotExistPushUserException(String errmsg) {
        super(errmsg);
    }

    public NotExistPushUserException (Throwable cause) {
        super (cause);
    }

    public NotExistPushUserException (String message, Throwable cause) {
        super (message, cause);
    }
}
