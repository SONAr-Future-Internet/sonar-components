package br.ufu.facom.mehar.sonar.client.cim.exception;

public class ComponentsNotLoadedException extends GeneralContainerizedInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public ComponentsNotLoadedException() {
		super();
	}

	public ComponentsNotLoadedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComponentsNotLoadedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComponentsNotLoadedException(String message) {
		super(message);
	}

	public ComponentsNotLoadedException(Throwable cause) {
		super(cause);
	}

}
