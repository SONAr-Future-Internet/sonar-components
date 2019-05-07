package br.ufu.facom.mehar.sonar.client.nim.element.exception;

import br.ufu.facom.mehar.sonar.client.nim.exception.GeneralNetworkInfrastructureManagerClientException;

public class DeviceConfigurationException extends GeneralNetworkInfrastructureManagerClientException {

	private static final long serialVersionUID = 6592793753488301126L;

	public DeviceConfigurationException() {
		super();
	}

	public DeviceConfigurationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DeviceConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceConfigurationException(String message) {
		super(message);
	}

	public DeviceConfigurationException(Throwable cause) {
		super(cause);
	}

}
