package br.ufu.facom.mehar.sonar.boot.server.exception;

public class IpPoolOverflowException extends DHCPServerException{
	private static final long serialVersionUID = -8331165439771546093L;

	public IpPoolOverflowException() {
		super();
	}

	public IpPoolOverflowException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IpPoolOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public IpPoolOverflowException(String message) {
		super(message);
	}

	public IpPoolOverflowException(Throwable cause) {
		super(cause);
	}

	
}
