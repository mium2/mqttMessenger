package com.msp.messenger.util.security.exception;

public class InvalidUserException extends Exception {

	private static final long serialVersionUID = -7550914801277291909L;

	public InvalidUserException(String message) {
		super(message);
	}

	public InvalidUserException(Throwable cause) {
		super(cause);
	}

	public InvalidUserException(String message, Throwable cause) {
		super(message, cause);
	}

}
