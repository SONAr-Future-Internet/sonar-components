package br.ufu.facom.mehar.sonar.client.nim.element.exception;

import br.ufu.facom.mehar.sonar.client.nim.exception.GeneralNetworkInfrastructureManagerClientException;

public class UnknownElementManagerException extends GeneralNetworkInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public UnknownElementManagerException() {
		super();
	}

	public UnknownElementManagerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownElementManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownElementManagerException(String message) {
		super(message);
	}

	public UnknownElementManagerException(Throwable cause) {
		super(cause);
	}

}
