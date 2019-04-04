package br.ufu.facom.mehar.sonar.core.util.exception;

public class ObjectCloneException extends SonarUtilException{
	private static final long serialVersionUID = 8083882467592290363L;

	public ObjectCloneException() {
		super();
	}

	public ObjectCloneException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ObjectCloneException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectCloneException(String message) {
		super(message);
	}

	public ObjectCloneException(Throwable cause) {
		super(cause);
	}
}
