package br.ufu.facom.mehar.sonar.client.nim.component.exception;

public class GeneralContainerizedInfrastructureManagerClientException extends RuntimeException{

	private static final long serialVersionUID = 3590553886140911603L;

	public GeneralContainerizedInfrastructureManagerClientException() {
		super();
	}

	public GeneralContainerizedInfrastructureManagerClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GeneralContainerizedInfrastructureManagerClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneralContainerizedInfrastructureManagerClientException(String message) {
		super(message);
	}

	public GeneralContainerizedInfrastructureManagerClientException(Throwable cause) {
		super(cause);
	}

	
}
