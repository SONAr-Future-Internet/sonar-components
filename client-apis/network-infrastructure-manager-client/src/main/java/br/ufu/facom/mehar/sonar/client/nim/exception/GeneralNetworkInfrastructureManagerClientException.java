package br.ufu.facom.mehar.sonar.client.nim.exception;

public class GeneralNetworkInfrastructureManagerClientException extends RuntimeException{

	private static final long serialVersionUID = 3590553886140911603L;

	public GeneralNetworkInfrastructureManagerClientException() {
		super();
	}

	public GeneralNetworkInfrastructureManagerClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GeneralNetworkInfrastructureManagerClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeneralNetworkInfrastructureManagerClientException(String message) {
		super(message);
	}

	public GeneralNetworkInfrastructureManagerClientException(Throwable cause) {
		super(cause);
	}

	
}
