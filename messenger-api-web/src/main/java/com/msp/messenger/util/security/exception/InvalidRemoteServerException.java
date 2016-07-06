package com.msp.messenger.util.security.exception;

public class InvalidRemoteServerException extends Exception {

	private static final long serialVersionUID = -6024235355618683040L;

	public InvalidRemoteServerException(String message) {
		super(message);
	}

	public InvalidRemoteServerException(Throwable cause) {
		super(cause);
	}

	public InvalidRemoteServerException(String message, Throwable cause) {
		super(message, cause);
	}

}
