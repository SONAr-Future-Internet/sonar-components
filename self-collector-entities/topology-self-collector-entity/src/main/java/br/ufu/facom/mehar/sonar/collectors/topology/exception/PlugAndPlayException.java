package br.ufu.facom.mehar.sonar.collectors.topology.exception;

public class PlugAndPlayException extends RuntimeException {
	private static final long serialVersionUID = 6536697503984369262L;

	public PlugAndPlayException() {
		super();
	}

	public PlugAndPlayException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PlugAndPlayException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlugAndPlayException(String message) {
		super(message);
	}

	public PlugAndPlayException(Throwable cause) {
		super(cause);
	}

}
