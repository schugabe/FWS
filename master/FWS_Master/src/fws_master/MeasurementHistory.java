package fws_master;


import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
/**
 * A History of Measurements. Without References to Stations or Parameters directly. So it can be serialized. 
 * @author Johannes Kasberger
 *
 */
public class MeasurementHistory {
	private String station;
	private String parameter;
	private Units unit;
	private LinkedList<MeasurementHistoryEntry> values;
		
	/**
	 * The station, parameter and unit must be provided at creation. So everything is know to Plot the Data without reference to the Station or Parameter itself.
	 * @param station
	 * @param parameter
	 * @param unit
	 */
	public MeasurementHistory(String station, String parameter, Units unit) {
		this.station = station;
		this.parameter = parameter;
		this.unit = unit;
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
	 * Get the Station of which the Measurements are
	 * @return Name of the Station
	 */
	public String getStation() {
		return this.station;
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
		this.values.remove(m);
		
	}
}
