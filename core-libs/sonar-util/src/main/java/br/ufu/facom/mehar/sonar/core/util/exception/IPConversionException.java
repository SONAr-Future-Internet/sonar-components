package br.ufu.facom.mehar.sonar.core.util.exception;

public class IPConversionException extends SonarUtilException{
	private static final long serialVersionUID = 8083882467592290363L;

	public IPConversionException() {
		super();
	}

	public IPConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IPConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public IPConversionException(String message) {
		super(message);
	}

	public IPConversionException(Throwable cause) {
		super(cause);
	}
}
