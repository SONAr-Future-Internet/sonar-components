package br.ufu.facom.mehar.sonar.client.nem.exception;

public class ChannelCloseException extends GenericNetworkElementManagerException {

	private static final long serialVersionUID = -2601039257523497614L;

	public ChannelCloseException() {
		super();
	}

	public ChannelCloseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ChannelCloseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelCloseException(String message) {
		super(message);
	}

	public ChannelCloseException(Throwable cause) {
		super(cause);
	}

}
