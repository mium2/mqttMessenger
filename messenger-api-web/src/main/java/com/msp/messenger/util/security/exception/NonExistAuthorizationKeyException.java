package com.msp.messenger.util.security.exception;

public class NonExistAuthorizationKeyException extends Exception {

	private static final long serialVersionUID = 673731580113835349L;

	public NonExistAuthorizationKeyException(String message) {
		super(message);
	}

	public NonExistAuthorizationKeyException(Throwable cause) {
		super(cause);
	}

	public NonExistAuthorizationKeyException(String message, Throwable cause) {
		super(message, cause);
	}

}
