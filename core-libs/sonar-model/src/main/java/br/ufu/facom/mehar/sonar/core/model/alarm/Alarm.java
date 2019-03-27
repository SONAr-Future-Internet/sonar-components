package br.ufu.facom.mehar.sonar.core.model.alarm;

import br.ufu.facom.mehar.sonar.core.model.topology.Element;

public class Alarm {
	private Long idAlarm;
	private String description;
	private Element element;
	private AlarmSeverity severity;
}
