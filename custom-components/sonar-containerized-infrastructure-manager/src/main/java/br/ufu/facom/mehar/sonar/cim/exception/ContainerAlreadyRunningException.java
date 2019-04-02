package br.ufu.facom.mehar.sonar.cim.exception;

import br.ufu.facom.mehar.sonar.core.model.container.Container;

public class ContainerAlreadyRunningException extends GeneralContainerizedInfrastructureManagerException {
	
	private static final long serialVersionUID = 3161814041892507363L;

	public ContainerAlreadyRunningException() {
		super();
	}

	public ContainerAlreadyRunningException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerAlreadyRunningException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerAlreadyRunningException(String message) {
		super(message);
	}

	public ContainerAlreadyRunningException(Throwable cause) {
		super(cause);
	}

}
