package fws_master;

import java.util.Vector;

public class Station_Controller {
	private Vector<Station> stations;
	
	public Station_Controller() {
		stations = new Vector<Station>();
	}

	/**
	 * @param stations the stations to set
	 */
	public void addStation(Station station) {
		this.stations.add(station);;
	}
	
	public boolean removeStation(Station station) {
		if (this.stations.remove(station)) {
			station.deleteStation();
			return true;
		}
		return false;
	}

	public Station findStation(String name) {
		for(Station s:this.stations) {
			if(s.getStationName().equals(name))
				return s;
		}
		return null;
	}
	/**
	 * @return the stations
	 */
	public Vector<Station> getStations() {
		return stations;
	}

	public void startStation(boolean start) {
		
		for (Station s:this.stations) {
			if (start && !s.isAlive())
				s.start();
			else if (start && s.isAlive()) {
				s.resumeStation();
			}
			else if (!start && s.isAlive()){
				s.pauseStation();
				s.interrupt();
			}
				
		}
		
	}
	
	
}
