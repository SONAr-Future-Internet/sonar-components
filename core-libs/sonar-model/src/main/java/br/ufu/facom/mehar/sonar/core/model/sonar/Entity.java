package br.ufu.facom.mehar.sonar.core.model.sonar;

public class Entity {

	private String name;
	private EntityGroup group;
	// TODO: think about other relevant attributes to an Entity

	public String getName() {
		return name;
	}

	public Entity() {
		super();
	}

	public Entity(String name, EntityGroup group) {
		super();
		this.name = name;
		this.group = group;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntityGroup getGroup() {
		return group;
	}

	public void setGroup(EntityGroup group) {
		this.group = group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (group != other.group)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Entity [name=" + name + ", group=" + group + "]";
	}

}
