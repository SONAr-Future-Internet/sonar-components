package br.ufu.facom.mehar.sonar.client.nem.exception;

public class SubscribeErrorException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public SubscribeErrorException() {
		super();
	}

	public SubscribeErrorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SubscribeErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public SubscribeErrorException(String message) {
		super(message);
	}

	public SubscribeErrorException(Throwable cause) {
		super(cause);
	}

}
