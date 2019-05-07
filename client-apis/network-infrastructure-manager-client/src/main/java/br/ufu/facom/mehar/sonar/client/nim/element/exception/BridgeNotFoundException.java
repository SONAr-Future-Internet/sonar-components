package br.ufu.facom.mehar.sonar.client.nim.element.exception;

import br.ufu.facom.mehar.sonar.client.nim.exception.GeneralNetworkInfrastructureManagerClientException;

public class BridgeNotFoundException extends GeneralNetworkInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public BridgeNotFoundException() {
		super();
	}

	public BridgeNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BridgeNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public BridgeNotFoundException(String message) {
		super(message);
	}

	public BridgeNotFoundException(Throwable cause) {
		super(cause);
	}

}
