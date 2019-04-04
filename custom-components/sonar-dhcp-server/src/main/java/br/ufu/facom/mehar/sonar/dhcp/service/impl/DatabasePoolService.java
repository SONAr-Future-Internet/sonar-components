package br.ufu.facom.mehar.sonar.dhcp.service.impl;

import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.client.dndb.repository.PropertyRepository;
import br.ufu.facom.mehar.sonar.dhcp.service.PoolService;

@Service
public class DatabasePoolService implements PoolService{
	@Autowired
	private PropertyRepository propertyRepository;

	@Override
	public InetAddress getIP(String macAddress) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
