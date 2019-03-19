package br.ufu.facom.mehar.sonar.cim.exception;

public class GeneralContainerizedInfrastructureManagerException extends RuntimeException{

	private static final long serialVersionUID = 3590553886140911603L;

	public GeneralContainerizedInfrastructureManagerException() {
		super();
	}

	public GeneralContainerizedInfrastructureManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GeneralContainerizedInfrastructureManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneralContainerizedInfrastructureManagerException(String message) {
		super(message);
	}

	public GeneralContainerizedInfrastructureManagerException(Throwable cause) {
		super(cause);
	}

	
}
