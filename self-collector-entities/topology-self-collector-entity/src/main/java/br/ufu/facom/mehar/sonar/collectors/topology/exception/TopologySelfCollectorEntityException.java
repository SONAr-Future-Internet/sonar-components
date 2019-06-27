package br.ufu.facom.mehar.sonar.collectors.topology.exception;

public class TopologySelfCollectorEntityException extends RuntimeException {
	private static final long serialVersionUID = 6536697503984369262L;

	public TopologySelfCollectorEntityException() {
		super();
	}

	public TopologySelfCollectorEntityException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TopologySelfCollectorEntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public TopologySelfCollectorEntityException(String message) {
		super(message);
	}

	public TopologySelfCollectorEntityException(Throwable cause) {
		super(cause);
	}

}
