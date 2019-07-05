package br.ufu.facom.mehar.sonar.interceptor.openflow;

import org.projectfloodlight.openflow.protocol.OFMessage;

public class OFMessageHolder {
    private OFMessageHeader header;
    private OFMessagePayload payload;
    private OFMessageType messageType;
    private OFMessage message;

    public OFMessageHolder(OFMessageHeader header, OFMessagePayload payload, OFMessageType messageType, OFMessage packet) {
        this.header = header;
        this.payload = payload;
        this.messageType = messageType;
        this.message = packet;
    }

	public OFMessageHeader getHeader() {
		return header;
	}

	public void setHeader(OFMessageHeader header) {
		this.header = header;
	}

	public OFMessagePayload getPayload() {
		return payload;
	}

	public void setPayload(OFMessagePayload payload) {
		this.payload = payload;
	}

	public OFMessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(OFMessageType messageType) {
		this.messageType = messageType;
	}

	public OFMessage getMessage() {
		return message;
	}

	public void setMessage(OFMessage message) {
		this.message = message;
	}
}