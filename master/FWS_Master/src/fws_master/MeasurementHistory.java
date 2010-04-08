package fws_master;


import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class MeasurementHistory {
	
	private HashMap<String,LinkedList<Measurement>> lastHours;
	private HashMap<String,LinkedList<MeasurementHistoryEntry>> lastYear;
	
	
	public void addData(String station,String parameter,Vector<Measurement> data) {
		String key = generateKey(station,parameter);
		LinkedList<Measurement> tmp = lastHours.get(key);
		if (tmp == null) {
			tmp = new LinkedList<Measurement>(data);
			lastHours.put(key, tmp);
		}
	}
	
	public void changeDay(String station,String parameter,HistoryFunctions historyFunction, Date oldDate, Date newDate) {
		if (oldDate.after(newDate))
			return;
		
		String key = generateKey(station,parameter);
		LinkedList<MeasurementHistoryEntry> year = lastYear.get(key);
		if (year == null) {
			year = new LinkedList<MeasurementHistoryEntry>();
			lastYear.put(key, year);
		}
		
		LinkedList<Measurement> current = lastHours.get(key);
		if (current == null)
			return;
		double calc = 0;
		switch(historyFunction) {
		case AVG: calc = 0.0; break;
		case MIN: calc = Double.MAX_VALUE; break;
		case MAX: calc = Double.MIN_VALUE;break;
		}
		for(Measurement m:current) {
			Date mDate = new Date(m.getTimestamp());
			if (mDate.before(oldDate))
				lastHours.remove(m);
			else if (mDate.compareTo(oldDate) == 0) {
				switch(historyFunction) {
				case AVG: calc += m.getConvValue(); break;
				case MIN: if ( m.getConvValue() < calc) calc = m.getConvValue(); break;
				case MAX: if (m.getConvValue() > calc ) calc = m.getConvValue(); break;
				}
			}
		}
		
		if (historyFunction == HistoryFunctions.AVG) {
			calc /= current.size();
		}
		
		MeasurementHistoryEntry entry = new MeasurementHistoryEntry(station,parameter,current.getFirst().getParameter().getName(),calc,oldDate);
		year.add(entry);
		if (year.size() > 355) {
			year.removeFirst();
		}
	}
	
	public Vector<Measurement> getLastHistory(String station,String parameter,int hours) {
		if (hours > 24)
			return null;
		
		String key = generateKey(station,parameter);
		LinkedList<Measurement> tmp = lastHours.get(key);
		
		if (tmp == null)
			return null;
		
		Date now = new Date();
		Date border = new Date((now.getTime()-(long)hours*60*60*1000));
		
		Vector<Measurement> result = new Vector<Measurement>();
		
		for(Measurement m:tmp) {
			Date mDate = new Date(m.getTimestamp());
			if (mDate.before(border)) {
				result.add(m);
			}
		}
		
		return result;
	}
	
	public Vector<MeasurementHistoryEntry> getLastHistoryDays(String station,String parameter) {
		String key = generateKey(station,parameter);
		LinkedList<MeasurementHistoryEntry> year = lastYear.get(key);
		return new Vector<MeasurementHistoryEntry>(year);
	}
	
	private String generateKey(String station,String parameter) {
		return station+parameter;
	}
}
