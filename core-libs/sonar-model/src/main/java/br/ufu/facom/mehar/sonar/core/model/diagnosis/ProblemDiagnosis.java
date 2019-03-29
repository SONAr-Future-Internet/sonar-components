package br.ufu.facom.mehar.sonar.core.model.diagnosis;

import java.util.List;

import br.ufu.facom.mehar.sonar.core.model.alarm.Alarm;
import br.ufu.facom.mehar.sonar.core.model.log.Log;
import br.ufu.facom.mehar.sonar.core.model.metric.ElementMetric;
import br.ufu.facom.mehar.sonar.core.model.metric.PortMetric;

public class ProblemDiagnosis {
	private List<Alarm> correlatedAlarms;
	private List<Log> correlatedLogs;
	private List<ElementMetric> correlatedElementMetrics;
	private List<PortMetric> correlatedPortMetrics;

	public List<Alarm> getCorrelatedAlarms() {
		return correlatedAlarms;
	}

	public void setCorrelatedAlarms(List<Alarm> correlatedAlarms) {
		this.correlatedAlarms = correlatedAlarms;
	}

	public List<Log> getCorrelatedLogs() {
		return correlatedLogs;
	}

	public void setCorrelatedLogs(List<Log> correlatedLogs) {
		this.correlatedLogs = correlatedLogs;
	}

	public List<ElementMetric> getCorrelatedElementMetrics() {
		return correlatedElementMetrics;
	}

	public void setCorrelatedElementMetrics(List<ElementMetric> correlatedElementMetrics) {
		this.correlatedElementMetrics = correlatedElementMetrics;
	}

	public List<PortMetric> getCorrelatedPortMetrics() {
		return correlatedPortMetrics;
	}

	public void setCorrelatedPortMetrics(List<PortMetric> correlatedPortMetrics) {
		this.correlatedPortMetrics = correlatedPortMetrics;
	}

}
