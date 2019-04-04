package br.ufu.facom.mehar.sonar.client.cim.exception;

public class UnableToStopComponentException extends GeneralContainerizedInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public UnableToStopComponentException() {
		super();
	}

	public UnableToStopComponentException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnableToStopComponentException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToStopComponentException(String message) {
		super(message);
	}

	public UnableToStopComponentException(Throwable cause) {
		super(cause);
	}

}
