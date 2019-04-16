package br.ufu.facom.mehar.sonar.client.ndb.exception;

public class GenericNetworkDatabaseClientException extends RuntimeException{
	private static final long serialVersionUID = 133147478047662440L;

	public GenericNetworkDatabaseClientException() {
		super();
	}

	public GenericNetworkDatabaseClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GenericNetworkDatabaseClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericNetworkDatabaseClientException(String message) {
		super(message);
	}

	public GenericNetworkDatabaseClientException(Throwable cause) {
		super(cause);
	}
}
