package fws_master;

import java.io.Serializable;
import java.util.Date;

/**
 * A Timestamp + Value. Are set on creation. Not changeable.
 * @author Johannes Kasberger
 *
 */
public class MeasurementHistoryEntry  
implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7010608165381950190L;
	private double value;
	private Date timestamp;
		
	/**
	 * Class constructor
	 * @param value
	 * @param timestamp
	 */
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