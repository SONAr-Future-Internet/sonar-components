package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerServerConnectionException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerServerConnectionException() {
		super();
	}

	public ContainerServerConnectionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerServerConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerServerConnectionException(String message) {
		super(message);
	}

	public ContainerServerConnectionException(Throwable cause) {
		super(cause);
	}

}
