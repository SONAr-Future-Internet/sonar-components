package br.ufu.facom.mehar.sonar.core.model.topology.type;

public enum ElementState {
	//Initial States
	IP_ASSIGNED_BY_DHCP(0), //Referred by DHCP
	REFERRED_BY_CONTROLLER(1), //Found in controller discovery
	CONNECTED_TO_TOPOLOGY(2), //Refered by Discovery (e.g. LLDP)
	
	//Intermediate / Waiting States
	WAITING_ROUTES(3), //COnnected But not accessible... waiting routes
	WAITING_DISCOVERY(4), //Neighbors configured with routes to access the device (may be avoided with NORMAL routes, learning switch or fowarding app)
	WAITING_CONTROLLER_ASSIGNMENT(6), //Device configured with one or more controllers
	WAITING_CONTROLLER_CONNECTION(7), //Device configured with one or more controllers
	WAITING_CONFIGURATION(8), //Device connected to the controller and waiting configuration
	
	//Final States
	DISCOVERED(5), //Device successfully discovered
	CONFIGURED(9), //Device reconfigured using controller
	DISCONNECTED(-1); //Device not accessible
	
	private Integer order;
	
	private ElementState(Integer order) {
		this.order = order;
	}

	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	
	public Boolean before(ElementState state){
		return this.order < state.getOrder();
	}
	
	public Boolean after(ElementState state){
		return this.order > state.getOrder();
	}

	public boolean beforeOrEqual(ElementState state) {
		return this.before(state) || this.equals(state);
	}
	
	public boolean afterOrEqual(ElementState state) {
		return this.after(state) || this.equals(state);
	}
	
	
	public Boolean isInitialState() {
		return this.equals(IP_ASSIGNED_BY_DHCP) || this.equals(REFERRED_BY_CONTROLLER) || this.equals(CONNECTED_TO_TOPOLOGY);
	}
	
	public Boolean isFinalState(ElementType type) {
		if(this.equals(ElementType.DEVICE)) {
			return this.equals(CONFIGURED) || this.equals(DISCONNECTED);
		}else {
			return this.equals(DISCOVERED) || this.equals(DISCONNECTED);
		}
		
	}
	
	public boolean isFinalState() {
		return this.equals(CONFIGURED) || this.equals(DISCONNECTED) || this.equals(DISCOVERED);
	}
	
	public Boolean isIntermediateState() {
		return this.equals(WAITING_ROUTES) || this.equals(WAITING_DISCOVERY) || this.equals(WAITING_CONTROLLER_CONNECTION) || this.equals(WAITING_CONFIGURATION) || this.equals(WAITING_CONTROLLER_ASSIGNMENT);
	}

	public Boolean equalsAny(ElementState...states) {
		for(ElementState state : states) {
			if(this.equals(state)) {
				return true;
			}
		}
		return false;
	}
}
