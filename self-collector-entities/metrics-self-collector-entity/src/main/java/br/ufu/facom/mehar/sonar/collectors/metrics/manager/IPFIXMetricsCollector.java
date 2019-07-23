package br.ufu.facom.mehar.sonar.collectors.metrics.manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import br.ufu.facom.mehar.sonar.collectors.metrics.manager.ipfix.MessageHeader;
import br.ufu.facom.mehar.sonar.collectors.metrics.manager.util.HeaderParseException;

public class IPFIXMetricsCollector {
	public void run() {
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(2055);
			while (true) {
				byte[] data = new byte[65536];
				DatagramPacket dp = new DatagramPacket(data, data.length);
				ds.receive(dp);
				MessageHeader mh = MessageHeader.parse(dp.getData());
				System.out.println(mh);
			}
		} catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (HeaderParseException hpe) {
			hpe.printStackTrace();
		} finally {
			if (ds != null) ds.close();
		}
	}
}
