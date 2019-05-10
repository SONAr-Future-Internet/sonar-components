package br.ufu.facom.mehar.sonar.core.model.context;

import java.util.Date;

/*
 * Temporal representation of a 'moment'
 */
public class Moment {
	protected Date intervalStart;
	protected Date intervalEnd;
	public Date getIntervalStart() {
		return intervalStart;
	}
	public void setIntervalStart(Date intervalStart) {
		this.intervalStart = intervalStart;
	}
	public Date getIntervalEnd() {
		return intervalEnd;
	}
	public void setIntervalEnd(Date intervalEnd) {
		this.intervalEnd = intervalEnd;
	}
}
