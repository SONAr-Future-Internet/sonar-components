package br.ufu.facom.mehar.sonar.boot.server.exception;

public class AutoBootManagerException extends RuntimeException{
	private static final long serialVersionUID = -8331165439771546093L;

	public AutoBootManagerException() {
		super();
	}

	public AutoBootManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AutoBootManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public AutoBootManagerException(String message) {
		super(message);
	}

	public AutoBootManagerException(Throwable cause) {
		super(cause);
	}

	
}
