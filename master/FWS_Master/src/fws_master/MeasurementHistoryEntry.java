package fws_master;

import java.util.Date;

public class MeasurementHistoryEntry {
	private double value;
	private Date timestamp;
		
	public MeasurementHistoryEntry(double value, Date timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}


	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
}
