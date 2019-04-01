package br.ufu.facom.mehar.sonar.cim.exception;

public class PortPoolManagerException extends GeneralContainerizedInfrastructureManagerException {

	private static final long serialVersionUID = 3161814041892507363L;

	public PortPoolManagerException() {
		super();
	}

	public PortPoolManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PortPoolManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public PortPoolManagerException(String message) {
		super(message);
	}

	public PortPoolManagerException(Throwable cause) {
		super(cause);
	}

}
