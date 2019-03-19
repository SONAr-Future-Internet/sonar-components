package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerInstantiationException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerInstantiationException() {
		super();
	}

	public ContainerInstantiationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerInstantiationException(String message) {
		super(message);
	}

	public ContainerInstantiationException(Throwable cause) {
		super(cause);
	}

}
