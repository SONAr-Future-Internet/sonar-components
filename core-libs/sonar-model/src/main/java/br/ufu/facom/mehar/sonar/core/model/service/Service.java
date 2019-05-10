package br.ufu.facom.mehar.sonar.core.model.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Service {
	// Primary Key
	private UUID idService;
	
	// Service identification (from OSS/BSS)
	private Long serviceReference;
	
	// Filter with Logical Expression
	private Filter filter;
	
	// Function / communication intent / functional requirement
	private Function function;
	
	// Policies / communication intent / non-functional requirement
	private Set<Policy> policies;
	
	//Params to Policies, Functions and Filters processing
	private Map<String,Object> parameterMap;
	
	// Natural description of the service. Useful for Intent Translation
	private String desciption;

	public UUID getIdService() {
		return idService;
	}

	public void setIdService(UUID idService) {
		this.idService = idService;
	}

	public Long getServiceReference() {
		return serviceReference;
	}

	public void setServiceReference(Long serviceReference) {
		this.serviceReference = serviceReference;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Set<Policy> getPolicies() {
		return policies;
	}

	public void setPolicies(Set<Policy> policies) {
		this.policies = policies;
	}

	public Map<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getDesciption() {
		return desciption;
	}

	public void setDesciption(String desciption) {
		this.desciption = desciption;
	}
}
