package br.ufu.facom.mehar.sonar.client.nim.component.exception;

public class ComponentNotFoundException extends GeneralContainerizedInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public ComponentNotFoundException() {
		super();
	}

	public ComponentNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComponentNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComponentNotFoundException(String message) {
		super(message);
	}

	public ComponentNotFoundException(Throwable cause) {
		super(cause);
	}

}
