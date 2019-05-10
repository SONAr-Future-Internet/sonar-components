package br.ufu.facom.mehar.sonar.client.nim.element.exception;

import br.ufu.facom.mehar.sonar.client.nim.exception.GeneralNetworkInfrastructureManagerClientException;

public class MethodNotSupportedException extends GeneralNetworkInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public MethodNotSupportedException() {
		super();
	}

	public MethodNotSupportedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MethodNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotSupportedException(String message) {
		super(message);
	}

	public MethodNotSupportedException(Throwable cause) {
		super(cause);
	}

}
