package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerMismatchException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerMismatchException() {
		super();
	}

	public ContainerMismatchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerMismatchException(String message) {
		super(message);
	}

	public ContainerMismatchException(Throwable cause) {
		super(cause);
	}

}
