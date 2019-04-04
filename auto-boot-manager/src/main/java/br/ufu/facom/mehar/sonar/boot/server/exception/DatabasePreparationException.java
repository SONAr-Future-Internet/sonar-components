package br.ufu.facom.mehar.sonar.boot.server.exception;

public class DatabasePreparationException extends AutoBootManagerException {
	private static final long serialVersionUID = 4205746162035430500L;

	public DatabasePreparationException() {
		super();
	}

	public DatabasePreparationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DatabasePreparationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabasePreparationException(String message) {
		super(message);
	}

	public DatabasePreparationException(Throwable cause) {
		super(cause);
	}

}
