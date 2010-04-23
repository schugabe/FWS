package fws_master;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class MeasurementHistoryController {
	private HashMap<String,MeasurementHistory> lastHours;
	private HashMap<String,MeasurementHistory> lastYear;
	
	public MeasurementHistoryController() {
		lastHours = new HashMap<String,MeasurementHistory>();
		lastYear = new HashMap<String,MeasurementHistory>();
	}
	
	
	public synchronized void addData(String station,String parameter,Vector<Measurement> data) {
		if (data.size() == 0)
			return;
		
		String key = generateKey(station,parameter);
		MeasurementHistory tmp = lastHours.get(key);
		if (tmp == null) {
			
			tmp = new MeasurementHistory(station,parameter, data.firstElement().getParameter().getUnit());
			
			lastHours.put(key, tmp);
		}
		tmp.addMeasurements(data);
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
			
			if (m.getTimestamp().after(border)) {
				result.addMeasurement(m);
			}
		}
		
		return result;
	}
	
	public MeasurementHistory getLastHistoryDays(String station,String parameter, int days) {
		String key = generateKey(station,parameter);
		MeasurementHistory tmp =  lastYear.get(key);
		if(tmp == null)
			return null;
		MeasurementHistory newHist = new MeasurementHistory(tmp.getStation(),tmp.getParameter(),tmp.getUnit());
		
		LinkedList<MeasurementHistoryEntry> list = tmp.getValues();
		
		
		
		if (days>list.size())
			return tmp;
		
		Iterator<MeasurementHistoryEntry> it = list.descendingIterator();
		int i = 0;
		while (it.hasNext() && i<days) {
			MeasurementHistoryEntry e = it.next();
			newHist.addMeasurement(e);
		}
		return newHist;
	}
	
	
	public void changeDay(String station,String parameter,HistoryFunctions historyFunction, Date oldDate) {
			
		String key = generateKey(station,parameter);
		
		Calendar tmpDay = Calendar.getInstance();
		tmpDay.setTime(oldDate);
		tmpDay.add(Calendar.HOUR_OF_DAY , -tmpDay.get(Calendar.HOUR_OF_DAY ));
		tmpDay.add(Calendar.MINUTE, -tmpDay.get(Calendar.MINUTE));
		tmpDay.add(Calendar.SECOND, -tmpDay.get(Calendar.SECOND));
		tmpDay.add(Calendar.MILLISECOND, -tmpDay.get(Calendar.MILLISECOND));
		
		Date beginDay,endDay;
		
		beginDay = tmpDay.getTime();
		
		tmpDay.add(Calendar.HOUR_OF_DAY , 23);
		tmpDay.add(Calendar.MINUTE, 59);
		tmpDay.add(Calendar.SECOND, 59);
		
		endDay = tmpDay.getTime();
		
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
		
		MeasurementHistory newCurrent = new MeasurementHistory(station,parameter,current.getUnit());
		
		for(MeasurementHistoryEntry m:current.getValues()) {
			
			if (m.getTimestamp().after(endDay))
				newCurrent.addMeasurement(m);
			
			else if (m.getTimestamp().after(beginDay) && m.getTimestamp().before(endDay)) {
				switch(historyFunction) {
				case AVG: calc += m.getValue(); break;
				case MIN: if ( m.getValue() < calc) calc = m.getValue(); break;
				case MAX: if (m.getValue() > calc ) calc = m.getValue(); break;
				}
			}
		}
		
		lastHours.put(key,newCurrent);
		
		if (historyFunction == HistoryFunctions.AVG) {
			calc /= current.getValues().size();
		}
		
		MeasurementHistoryEntry entry = new MeasurementHistoryEntry(calc,endDay);
		year.addMeasurement(entry);
		if (year.getValues().size() > 355) {
			year.removeFirstEntry();
		}
	}
	
	private String generateKey(String station,String parameter) {
		return station+parameter;
	}

}
