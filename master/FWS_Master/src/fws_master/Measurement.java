package fws_master;
/**
 * A Measurement is a value received of a slave. When created the current time is saved. This is not the timestamp when the value was measured but it shouldn't be to old so this will be precise enough. 
 * @author Johannes Kasberger
 *
 */
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
	
	/**
	 * Returns the Converted Value. The value is a short value. But the slave can embed a floating point value in a short by multiplying the value. So if the slave measures a temperature value with
	 * a precision of 20.2 it saves the value 202. This functions converts it back to a double value. 
	 * @return the converted value
	 */
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
