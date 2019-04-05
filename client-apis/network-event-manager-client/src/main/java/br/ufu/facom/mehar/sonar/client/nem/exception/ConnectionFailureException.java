package br.ufu.facom.mehar.sonar.client.nem.exception;

public class ConnectionFailureException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public ConnectionFailureException() {
		super();
	}

	public ConnectionFailureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConnectionFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionFailureException(String message) {
		super(message);
	}

	public ConnectionFailureException(Throwable cause) {
		super(cause);
	}

}
