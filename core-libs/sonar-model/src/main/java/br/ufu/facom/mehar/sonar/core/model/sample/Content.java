package br.ufu.facom.mehar.sonar.core.model.sample;

import br.ufu.facom.mehar.sonar.core.model.service.Service;

public class Content {
	private Service service;
	private Sample sample;
	private ContentType type;

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}

	public ContentType getType() {
		return type;
	}

	public void setType(ContentType type) {
		this.type = type;
	}

}
