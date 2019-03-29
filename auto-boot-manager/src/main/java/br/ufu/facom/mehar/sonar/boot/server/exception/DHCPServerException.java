package br.ufu.facom.mehar.sonar.boot.server.exception;

public class DHCPServerException extends AutoBootManagerException{
	private static final long serialVersionUID = -8331165439771546093L;

	public DHCPServerException() {
		super();
	}

	public DHCPServerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DHCPServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DHCPServerException(String message) {
		super(message);
	}

	public DHCPServerException(Throwable cause) {
		super(cause);
	}

	
}
