package com.msp.messenger.auth;

import java.io.Serializable;

/**
 * Created by Y.B.H(mium2) on 16. 6. 20..
 */
public class UnauthorizedException extends Exception implements Serializable {

    private static final long serialVersionUID = -8210688170159553566L;

    public UnauthorizedException(){}

    public UnauthorizedException(String errmsg) {
        super(errmsg);
    }

    public UnauthorizedException (Throwable cause) {
        super (cause);
    }

    public UnauthorizedException (String message, Throwable cause) {
        super (message, cause);
    }

}
