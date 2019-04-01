package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerSearchException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerSearchException() {
		super();
	}

	public ContainerSearchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerSearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerSearchException(String message) {
		super(message);
	}

	public ContainerSearchException(Throwable cause) {
		super(cause);
	}

}
