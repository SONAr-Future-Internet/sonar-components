package br.ufu.facom.mehar.sonar.cim.exception;

public class UnsupportedMethodException extends GeneralContainerizedInfrastructureManagerException {
	private static final long serialVersionUID = -2331311188206689353L;

	public UnsupportedMethodException() {
		super();
	}

	public UnsupportedMethodException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedMethodException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedMethodException(String message) {
		super(message);
	}

	public UnsupportedMethodException(Throwable cause) {
		super(cause);
	}

}
