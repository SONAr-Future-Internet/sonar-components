package br.ufu.facom.mehar.sonar.client.nim.exception;

public class DeviceConfigurationTimeoutException extends DeviceConfigurationException {
	private static final long serialVersionUID = -5674805610624044596L;

	public DeviceConfigurationTimeoutException() {
		super();
	}

	public DeviceConfigurationTimeoutException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DeviceConfigurationTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceConfigurationTimeoutException(String message) {
		super(message);
	}

	public DeviceConfigurationTimeoutException(Throwable cause) {
		super(cause);
	}
}
