package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerNotFoundException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerNotFoundException() {
		super();
	}

	public ContainerNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerNotFoundException(String message) {
		super(message);
	}

	public ContainerNotFoundException(Throwable cause) {
		super(cause);
	}

}
