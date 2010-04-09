package fws_master;


import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class MeasurementHistory {
	private String station;
	private String parameter;
	private Units unit;
	private LinkedList<MeasurementHistoryEntry> values;
		
	public MeasurementHistory(String station, String parameter, Units unit) {
		this.station = station;
		this.parameter = parameter;
		this.unit = unit;
		this.values = new LinkedList<MeasurementHistoryEntry>();
	}
	
	public void addMeasurements(Collection<Measurement> c) {
		for(Measurement m:c) {
			MeasurementHistoryEntry e = new MeasurementHistoryEntry(m.getConvValue(),new Date(m.getTimestamp()));
			this.values.add(e);
		}
	}
	public void addMeasurement(MeasurementHistoryEntry entry) {
		this.values.add(entry);
	}
	
	public String getStation() {
		return this.station;
	}
	
	public String getParameter() {
		return this.parameter;
	}
	
	public Units getUnit() {
		return this.unit;
	}
	
	public LinkedList<MeasurementHistoryEntry> getValues() {
		return this.values;
	}

	public void removeFirstEntry() {
		this.values.removeFirst();
	}

	public void removeEntry(MeasurementHistoryEntry m) {
		this.values.remove(m);
		
	}
}
