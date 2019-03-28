package br.ufu.facom.mehar.sonar.core.database.client.exception;

public class JsonConversionException extends GenericDistributedNetworkDatabaseClientException {

	private static final long serialVersionUID = -2601039257523497614L;

	public JsonConversionException() {
		super();
	}

	public JsonConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsonConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonConversionException(String message) {
		super(message);
	}

	public JsonConversionException(Throwable cause) {
		super(cause);
	}

}
