package br.ufu.facom.mehar.sonar.boot.server.exception;

public class IpPoolConversionException extends DHCPServerException{
	private static final long serialVersionUID = -8331165439771546093L;

	public IpPoolConversionException() {
		super();
	}

	public IpPoolConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IpPoolConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public IpPoolConversionException(String message) {
		super(message);
	}

	public IpPoolConversionException(Throwable cause) {
		super(cause);
	}

	
}
