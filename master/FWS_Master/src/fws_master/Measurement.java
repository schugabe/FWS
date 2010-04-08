package fws_master;

public class Measurement {
	private InputParameter parameter;
	private Station station;
	private long timestamp;
	private int value;

	
	public Measurement(Station station,InputParameter parameter,int value) {
		this.parameter = parameter;
		this.station = station;
		this.value = value;
		this.timestamp = System.currentTimeMillis();
	}
	




	/**
	 * @return the parameter
	 */
	public InputParameter getParameter() {
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
	
	public double getConvValue() {
		double tmp = value;
		switch (this.parameter.getFormat()) {
		case NK0: break;
		case NK1: tmp /= 10; break;
		case NK2: tmp /= 100; break;
		case NK3: tmp /= 1000; break;
		case NK4: tmp /= 10000; break;
		case NK5: tmp /= 100000; break;
		
		}
		return tmp;
	}
}
