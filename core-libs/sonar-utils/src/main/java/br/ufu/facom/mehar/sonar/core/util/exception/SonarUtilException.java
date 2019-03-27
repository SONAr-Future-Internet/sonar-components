package br.ufu.facom.mehar.sonar.core.util.exception;

public class SonarUtilException extends RuntimeException{

	private static final long serialVersionUID = 8643512004101751614L;

	public SonarUtilException() {
		super();
	}

	public SonarUtilException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SonarUtilException(String message, Throwable cause) {
		super(message, cause);
	}

	public SonarUtilException(String message) {
		super(message);
	}

	public SonarUtilException(Throwable cause) {
		super(cause);
	}
}
