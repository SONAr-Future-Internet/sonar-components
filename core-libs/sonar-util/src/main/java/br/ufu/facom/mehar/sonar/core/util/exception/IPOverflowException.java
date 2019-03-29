package br.ufu.facom.mehar.sonar.core.util.exception;

public class IPOverflowException extends SonarUtilException{
	private static final long serialVersionUID = 8083882467592290363L;

	public IPOverflowException() {
		super();
	}

	public IPOverflowException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IPOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public IPOverflowException(String message) {
		super(message);
	}

	public IPOverflowException(Throwable cause) {
		super(cause);
	}
}
