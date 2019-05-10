package br.ufu.facom.mehar.sonar.client.nim.element.exception;

import br.ufu.facom.mehar.sonar.client.nim.exception.GeneralNetworkInfrastructureManagerClientException;

public class MethodNotImplementedYetException extends GeneralNetworkInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public MethodNotImplementedYetException() {
		super();
	}

	public MethodNotImplementedYetException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MethodNotImplementedYetException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotImplementedYetException(String message) {
		super(message);
	}

	public MethodNotImplementedYetException(Throwable cause) {
		super(cause);
	}

}
