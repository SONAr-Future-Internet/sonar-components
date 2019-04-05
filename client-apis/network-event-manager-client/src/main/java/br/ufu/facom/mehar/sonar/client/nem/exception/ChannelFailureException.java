package br.ufu.facom.mehar.sonar.client.nem.exception;

public class ChannelFailureException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public ChannelFailureException() {
		super();
	}

	public ChannelFailureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ChannelFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelFailureException(String message) {
		super(message);
	}

	public ChannelFailureException(Throwable cause) {
		super(cause);
	}

}
