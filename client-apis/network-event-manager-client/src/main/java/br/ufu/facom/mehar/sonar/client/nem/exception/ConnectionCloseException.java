package br.ufu.facom.mehar.sonar.client.nem.exception;

public class ConnectionCloseException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public ConnectionCloseException() {
		super();
	}

	public ConnectionCloseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConnectionCloseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionCloseException(String message) {
		super(message);
	}

	public ConnectionCloseException(Throwable cause) {
		super(cause);
	}

}
