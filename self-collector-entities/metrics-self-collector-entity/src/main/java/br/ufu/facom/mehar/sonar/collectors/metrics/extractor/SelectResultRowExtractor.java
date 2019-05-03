package br.ufu.facom.mehar.sonar.collectors.metrics.extractor;

import java.util.Map;

import com.vmware.ovsdb.protocol.operation.notation.Row;

import br.ufu.facom.mehar.sonar.collectors.metrics.model.Interface;
import br.ufu.facom.mehar.sonar.collectors.metrics.model.Statistics;

public class SelectResultRowExtractor {

	private Row row;

	public SelectResultRowExtractor(Row row) {
		this.row = row;
	}

	public Interface getInterface() {
		String name = row.getStringColumn("name");
		Map<String, Long> statisticsMap = row.getMapColumn("statistics");
		Long rx_packets = statisticsMap.get("rx_packets");
		Long rx_bytes = statisticsMap.get("rx_bytes");
		Long tx_packets = statisticsMap.get("tx_packets");
		Long tx_bytes = statisticsMap.get("tx_bytes");
		Long rx_dropped = statisticsMap.get("rx_dropped");
		Long rx_frame_err = statisticsMap.get("rx_frame_err");
		Long rx_over_err = statisticsMap.get("rx_over_err");
		Long rx_crc_err = statisticsMap.get("rx_crc_err");
		Long rx_errors = statisticsMap.get("rx_errors");
		Long tx_dropped = statisticsMap.get("tx_dropped");
		Long collisions = statisticsMap.get("collisions");
		Long tx_errors = statisticsMap.get("tx_errors");
		Statistics statistics = new Statistics(rx_packets, rx_bytes, tx_packets, tx_bytes, rx_dropped, rx_frame_err,
				rx_over_err, rx_crc_err, rx_errors, tx_dropped, collisions, tx_errors);
		Interface portInterface = new Interface(name, statistics);
		return portInterface;
	}

}
