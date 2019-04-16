package br.ufu.facom.mehar.sonar.client.ndb.exception;

public class DataValidationException extends GenericDistributedNetworkDatabaseClientException {

	private static final long serialVersionUID = -2601039257523497614L;

	public DataValidationException() {
		super();
	}

	public DataValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataValidationException(String message) {
		super(message);
	}

	public DataValidationException(Throwable cause) {
		super(cause);
	}

}
