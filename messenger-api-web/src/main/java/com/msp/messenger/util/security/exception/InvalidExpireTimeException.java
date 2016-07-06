package com.msp.messenger.util.security.exception;

public class InvalidExpireTimeException extends Exception {

	private static final long serialVersionUID = 8877261630901367282L;

	public InvalidExpireTimeException(String message) {
		super(message);
	}

	public InvalidExpireTimeException(Throwable cause) {
		super(cause);
	}

	public InvalidExpireTimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
