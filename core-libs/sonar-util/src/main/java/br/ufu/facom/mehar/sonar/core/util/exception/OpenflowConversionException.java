package br.ufu.facom.mehar.sonar.core.util.exception;

public class OpenflowConversionException extends SonarUtilException{
	private static final long serialVersionUID = 8083882467592290363L;

	public OpenflowConversionException() {
		super();
	}

	public OpenflowConversionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public OpenflowConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	public OpenflowConversionException(String message) {
		super(message);
	}

	public OpenflowConversionException(Throwable cause) {
		super(cause);
	}
}
