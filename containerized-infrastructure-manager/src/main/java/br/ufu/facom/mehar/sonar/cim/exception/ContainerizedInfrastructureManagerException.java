package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerizedInfrastructureManagerException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public ContainerizedInfrastructureManagerException() {
		super();
	}

	public ContainerizedInfrastructureManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerizedInfrastructureManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerizedInfrastructureManagerException(String message) {
		super(message);
	}

	public ContainerizedInfrastructureManagerException(Throwable cause) {
		super(cause);
	}

	
}
