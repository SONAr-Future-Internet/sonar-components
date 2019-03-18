package br.ufu.facom.mehar.sonar.cim.exception;

public class ContainerCreationException extends ContainerizedInfrastructureManagerException {
	private static final long serialVersionUID = 1L;

	public ContainerCreationException() {
		super();
	}

	public ContainerCreationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContainerCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContainerCreationException(String message) {
		super(message);
	}

	public ContainerCreationException(Throwable cause) {
		super(cause);
	}

}
