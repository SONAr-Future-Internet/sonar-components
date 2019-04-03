package br.ufu.facom.mehar.sonar.client.cim.exception;

public class UnableToRunComponentException extends GeneralContainerizedInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public UnableToRunComponentException() {
		super();
	}

	public UnableToRunComponentException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnableToRunComponentException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToRunComponentException(String message) {
		super(message);
	}

	public UnableToRunComponentException(Throwable cause) {
		super(cause);
	}

}
