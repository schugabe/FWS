package fws_master;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class MeasurementHistoryController {
	private HashMap<String,MeasurementHistory> lastHours;
	private HashMap<String,MeasurementHistory> lastYear;
	
	public void addData(String station,String parameter,Vector<Measurement> data) {
		if (data.size() == 0)
			return;
		
		String key = generateKey(station,parameter);
		MeasurementHistory tmp = lastHours.get(key);
		if (tmp == null) {
			tmp = new MeasurementHistory(station,parameter, data.get(0).getParameter().getUnit());
			tmp.addMeasurements(data);
			lastHours.put(key, tmp);
		}
	}
	
	
	
	public MeasurementHistory getLastHistory(String station,String parameter,int hours) {
		if (hours > 24)
			return null;
		
		String key = generateKey(station,parameter);
		MeasurementHistory tmp = lastHours.get(key);
		
		if (tmp == null)
			return null;
		
		MeasurementHistory result = new MeasurementHistory(tmp.getStation(),tmp.getParameter(),tmp.getUnit());
		
		Date now = new Date();
		Date border = new Date((now.getTime()-(long)hours*60*60*1000));
		
		for(MeasurementHistoryEntry m:tmp.getValues()) {
			
			if (m.getTimestamp().before(border)) {
				result.addMeasurement(m);
			}
		}
		
		return result;
	}
	
	public MeasurementHistory getLastHistoryDays(String station,String parameter) {
		String key = generateKey(station,parameter);
		return lastYear.get(key);
	}
	
	
	public void changeDay(String station,String parameter,HistoryFunctions historyFunction, Date oldDate, Date newDate) {
		if (oldDate.after(newDate))
			return;
		
		String key = generateKey(station,parameter);
		
		MeasurementHistory current = lastHours.get(key);
		if (current == null)
			return;
		
		MeasurementHistory year = lastYear.get(key);
		
		if (year == null) {
			year = new MeasurementHistory(current.getStation(),current.getParameter(), current.getUnit());
			lastYear.put(key, year);
		}
		
		
		double calc = 0;
		switch(historyFunction) {
		case AVG: calc = 0.0; break;
		case MIN: calc = Double.MAX_VALUE; break;
		case MAX: calc = Double.MIN_VALUE;break;
		}
		
		for(MeasurementHistoryEntry m:current.getValues()) {
			
			if (m.getTimestamp().before(oldDate))
				current.removeEntry(m);
			else if (m.getTimestamp().compareTo(oldDate) == 0) {
				switch(historyFunction) {
				case AVG: calc += m.getValue(); break;
				case MIN: if ( m.getValue() < calc) calc = m.getValue(); break;
				case MAX: if (m.getValue() > calc ) calc = m.getValue(); break;
				}
			}
		}
		
		if (historyFunction == HistoryFunctions.AVG) {
			calc /= current.getValues().size();
		}
		
		MeasurementHistoryEntry entry = new MeasurementHistoryEntry(calc,oldDate);
		year.addMeasurement(entry);
		if (year.getValues().size() > 355) {
			year.removeFirstEntry();
		}
	}
	
	private String generateKey(String station,String parameter) {
		return station+parameter;
	}

}
