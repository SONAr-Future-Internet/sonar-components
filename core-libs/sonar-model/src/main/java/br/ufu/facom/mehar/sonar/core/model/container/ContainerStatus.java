package br.ufu.facom.mehar.sonar.core.model.container;

public enum ContainerStatus {
	RUNNING_STATE("running"),
	STOPPED_STATE("stopped"),
	CREATED_STATE("created"),
	RESTARTING_STATE("restarting"),
	PAUSED_STATE("paused"),
	EXITED_STATE("exited"),
	DEAD_STATE("dead"), // API 1.24
	REMOVED_STATE("removed"); // CUSTOM
	
	private String dockerStatus;
	
	ContainerStatus(String dockerStatus) {
		this.dockerStatus = dockerStatus;
	}
	
	public static ContainerStatus getByDockerStatus(String dockerStatus) {
		for(ContainerStatus status : ContainerStatus.values()) {
			if(status.getDockerStatus().equals(dockerStatus)) {
				return status;
			}
		}
		return null;
	}
	
	
	public String getDockerStatus() {
		return dockerStatus;
	}

	public void setDockerStatus(String dockerStatus) {
		this.dockerStatus = dockerStatus;
	}
}