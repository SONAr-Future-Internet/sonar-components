package br.ufu.facom.mehar.sonar.core.database.client.exception;

public class GenericDistributedNetworkDatabaseClientException extends RuntimeException{
	private static final long serialVersionUID = 133147478047662440L;

	public GenericDistributedNetworkDatabaseClientException() {
		super();
	}

	public GenericDistributedNetworkDatabaseClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GenericDistributedNetworkDatabaseClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenericDistributedNetworkDatabaseClientException(String message) {
		super(message);
	}

	public GenericDistributedNetworkDatabaseClientException(Throwable cause) {
		super(cause);
	}
}
