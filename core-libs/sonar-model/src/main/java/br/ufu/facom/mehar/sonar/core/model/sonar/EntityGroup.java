package br.ufu.facom.mehar.sonar.core.model.sonar;

public enum EntityGroup {
	// Basic Data and Communication COmponentes
	CollectorEntities("CoE", "collector-entities"), 
	SelfOrganizingEntities("SOE", "self-organizing-entities"),
	SelfLearningEntities("SLE", "self-learning-entities");

	private String acronym;
	private String key;

	private EntityGroup(String acronym, String key) {
		this.acronym = acronym;
		this.key = key;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static EntityGroup getByKey(String key) {
		for (EntityGroup component : EntityGroup.values()) {
			if (component.getKey().equals(key)) {
				return component;
			}
		}
		return null;
	}
}
