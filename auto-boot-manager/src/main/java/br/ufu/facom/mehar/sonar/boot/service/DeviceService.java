package br.ufu.facom.mehar.sonar.boot.service;

import java.util.Vector;

import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import br.ufu.facom.mehar.sonar.boot.model.Device;

@Service
public class DeviceService {

	Logger logger = Logger.getLogger(DeviceService.class);

	Vector<Device> newDevices = new Vector<Device>();
	Vector<Device> configuredDevices = new Vector<Device>();

	public void register(Device device) {
		logger.info("Registering device" + device.getIpAddress() + ".");
		this.newDevices.add(device);
	}
}
