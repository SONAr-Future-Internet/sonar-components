package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerInvalidException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerInvalidException() {
		super();
	}

	public ContainerInvalidException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerInvalidException(String message) {
		super(message);
	}

	public ContainerInvalidException(Throwable cause) {
		super(cause);
	}

}
