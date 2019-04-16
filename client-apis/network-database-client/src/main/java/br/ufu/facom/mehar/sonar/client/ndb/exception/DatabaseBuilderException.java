package br.ufu.facom.mehar.sonar.client.ndb.exception;

public class DatabaseBuilderException extends GenericDistributedNetworkDatabaseClientException {

	private static final long serialVersionUID = -2601039257523497614L;

	public DatabaseBuilderException() {
		super();
	}

	public DatabaseBuilderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DatabaseBuilderException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatabaseBuilderException(String message) {
		super(message);
	}

	public DatabaseBuilderException(Throwable cause) {
		super(cause);
	}

}
