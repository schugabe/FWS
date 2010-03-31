package fws_master;

public class Measurement {
	private Input_Parameter parameter;
	private Station station;
	private long timestamp;
	private int value;
	
	public Measurement(Station station,Input_Parameter parameter,int value) {
		this.parameter = parameter;
		this.station = station;
		this.value = value;
		this.timestamp = System.currentTimeMillis();
	}


	/**
	 * @return the parameter
	 */
	public Input_Parameter getParameter() {
		return parameter;
	}

	/**
	 * @return the station
	 */
	public Station getStation() {
		return station;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
}
