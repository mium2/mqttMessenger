package com.msp.messenger.util.security.exception;

public class SecurityAuthorizationException extends Exception {

	private static final long serialVersionUID = -8539984849191980307L;

	public SecurityAuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityAuthorizationException(String message) {
		super(message);
	}

	public SecurityAuthorizationException(Throwable cause) {
		super(cause);
	}

}
