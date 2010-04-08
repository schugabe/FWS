package fws_master;

import java.util.Date;

public class MeasurementHistoryEntry {
	private double value;
	private Date timestamp;
	private String unit;
	private String parameter;
	private String station;
	
	public MeasurementHistoryEntry(String station, String parameter, String unit, double value, Date timestamp) {
		this.setStation(station);
		this.setParameter(parameter);
		this.setUnit(unit);
		this.setValue(value);
		this.setTimestamp(timestamp);
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return the parameter
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * @param station the station to set
	 */
	public void setStation(String station) {
		this.station = station;
	}

	/**
	 * @return the station
	 */
	public String getStation() {
		return station;
	}
}
