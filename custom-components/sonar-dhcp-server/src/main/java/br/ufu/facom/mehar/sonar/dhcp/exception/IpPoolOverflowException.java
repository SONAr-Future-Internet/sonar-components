package br.ufu.facom.mehar.sonar.dhcp.exception;

public class IpPoolOverflowException extends DHCPServerException {

	private static final long serialVersionUID = -5343057765582573369L;

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
