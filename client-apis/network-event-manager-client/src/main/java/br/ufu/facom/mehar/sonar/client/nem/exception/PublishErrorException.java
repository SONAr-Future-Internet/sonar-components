package br.ufu.facom.mehar.sonar.client.nem.exception;

public class PublishErrorException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public PublishErrorException() {
		super();
	}

	public PublishErrorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PublishErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public PublishErrorException(String message) {
		super(message);
	}

	public PublishErrorException(Throwable cause) {
		super(cause);
	}

}
