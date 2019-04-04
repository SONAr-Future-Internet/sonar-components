package br.ufu.facom.mehar.sonar.boot.server.exception;

public class InvalidEndpointException extends AutoBootManagerException {
	private static final long serialVersionUID = 4205746162035430500L;

	public InvalidEndpointException() {
		super();
	}

	public InvalidEndpointException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidEndpointException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidEndpointException(String message) {
		super(message);
	}

	public InvalidEndpointException(Throwable cause) {
		super(cause);
	}

}
