package br.ufu.facom.mehar.sonar.client.nem.exception;

public class GenericNetworkElementManagerException extends RuntimeException{
	private static final long serialVersionUID = 133147478047662440L;

	public GenericNetworkElementManagerException() {
		super();
	}

	public GenericNetworkElementManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GenericNetworkElementManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericNetworkElementManagerException(String message) {
		super(message);
	}

	public GenericNetworkElementManagerException(Throwable cause) {
		super(cause);
	}
}
