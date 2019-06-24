package br.ufu.facom.mehar.sonar.core.model.event;

public class ControllerInterceptionEvent {
	private String ipSource;
	private String interceptorPath;
	
	public ControllerInterceptionEvent(String ipSource, String interceptorPath) {
		super();
		this.ipSource = ipSource;
		this.interceptorPath = interceptorPath;
	}
	public String getIpSource() {
		return ipSource;
	}
	public void setIpSource(String ipSource) {
		this.ipSource = ipSource;
	}
	public String getInterceptorPath() {
		return interceptorPath;
	}
	public void setInterceptorPath(String interceptorPath) {
		this.interceptorPath = interceptorPath;
	}
}
