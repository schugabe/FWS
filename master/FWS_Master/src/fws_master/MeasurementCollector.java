package fws_master;

import java.util.Hashtable;
import java.util.Vector;

public class MeasurementCollector extends Thread {
	private Station_Controller controller;
	private int intervall;
	public MeasurementCollector(Station_Controller controller, int intervall) {
		this.intervall = intervall;
		this.controller = controller;
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000*this.intervall);
				
				for(Station s:this.controller.getStations()) {
					Vector<Measurement> measurements = s.getNMeasurements(this.intervall);
					Hashtable<String,Vector<Measurement>> params = new Hashtable<String,Vector<Measurement>>();
					
					for(Measurement m:measurements) {
						Vector<Measurement> tmp = params.get(m.getParameter().getName());
						if (tmp == null) {
							tmp = new Vector<Measurement>(measurements.size()/s.getInputParamsCount()+1);
							params.put(m.getParameter().getName(), tmp);
						} 
						tmp.add(m);
					}
					
					// TODO output erstellen...
				}
				
			} catch (Exception ex) {

			}
		}
	}
}
