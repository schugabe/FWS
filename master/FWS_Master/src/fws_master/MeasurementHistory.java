package fws_master;


import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
/**
 * A History of Measurements. Without References to Slaves or Parameters directly. So it can be serialized. 
 * @author Johannes Kasberger
 *
 */
public class MeasurementHistory  
implements Serializable  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3023342558174695841L;
	private String slave;
	private String parameter;
	private Units unit;
	private float filter;
	private LinkedList<MeasurementHistoryEntry> values;
		
	/**
	 * The slave, parameter and unit must be provided at creation. So everything is know to Plot the Data without reference to the Slave or Parameter itself.
	 * @param slave
	 * @param parameter
	 * @param unit
	 */
	public MeasurementHistory(String slave, String parameter, Units unit, float filter) {
		this.slave = slave;
		this.parameter = parameter;
		this.unit = unit;
		this.filter = filter;
		this.values = new LinkedList<MeasurementHistoryEntry>();
	}
	
	/**
	 * Add the Measurements to the intern collection
	 * @param c
	 */
	public void addMeasurements(Collection<Measurement> c) {
		for(Measurement m:c) {
			MeasurementHistoryEntry e = new MeasurementHistoryEntry(m.getConvValue(),new Date(m.getTimestamp()));
			this.values.add(e);
		}
	}
	
	/**
	 * Add a single MeasurementHistoryEntry to the collection
	 * @param entry
	 */
	public void addMeasurement(MeasurementHistoryEntry entry) {
		this.values.add(entry);
	}
	
	/**
	 * Get the Slave of which the Measurements are
	 * @return Name of the Slave
	 */
	public String getSlave() {
		return this.slave;
	}
	
	/**
	 * Get the Filterparameter
	 * @return filter 
	 */
	public float getFilter() {
		return this.filter;
	}
	
	/**
	 * Get the Parameter of which the Measurements are
	 * @return Name of the Parameter
	 */
	public String getParameter() {
		return this.parameter;
	}
	
	/**
	 * Get the Output Unit
	 * @return the unit
	 */
	public Units getUnit() {
		return this.unit;
	}
	
	/**
	 * All Saved Values
	 * @return values
	 */
	public LinkedList<MeasurementHistoryEntry> getValues() {
		return this.values;
	}

	/**
	 * Remove the newest entry
	 */
	public void removeFirstEntry() {
		this.values.removeFirst();
	}

	/**
	 * Remove a Entry
	 * @param m Entry to be removed.
	 */
	public void removeEntry(MeasurementHistoryEntry m) {
		Vector<MeasurementHistoryEntry> del = new Vector<MeasurementHistoryEntry>();
		for (MeasurementHistoryEntry e:this.values) {
			if (e.getTimestamp().equals(m.getTimestamp()) && e.getValue() == m.getValue()) {
				del.add(e);
			}
		}
		this.values.removeAll(del);
		
	}
	
	public void removeAll() {
		this.values = new LinkedList<MeasurementHistoryEntry>();
	}

	public void sort() {
		Collections.sort(this.values);
	}
}
