package fws_master;

import java.util.Vector;

/**
 * Manages a List of all Stations
 * @author Johannes Kasberger
 *
 */
public class StationController {
	private Vector<Station> stations;
	private boolean running = false;
	/**
	 * Just creates a new List
	 */
	public StationController() {
		stations = new Vector<Station>();
	}

	/**
	 * @param station the stations to add
	 */
	public void addStation(Station station) {
		this.stations.add(station);;
	}
	
	/**
	 * Removes a Station
	 * @param station the station to be removed
	 * @return true if removing was possible
	 */
	public boolean removeStation(Station station) {
		if (this.stations.remove(station)) {
			station.deleteStation();
			return true;
		}
		return false;
	}

	/**
	 * Finds a Station 
	 * @param name Name of the station that is searched
	 * @return the station with the searched name
	 */
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

	/**
	 * Starts or Pauses all Stations of this controller
	 * @param start if true the threads a started or continued, when false the threads are paused
	 */
	public void startStations(boolean start) {
		
		for (Station s:this.stations) {
			if (start && !s.isAlive()) {
				s.start();
				running = true;
			}
			else if (start && s.isAlive()) {
				s.resumeStation();
				running = true;
			}
			else if (!start && s.isAlive()){
				s.pauseStation();
				s.interrupt();
				running = false;
			}
		}
	}
	
	public boolean isRunning() {
		return running;
	}
}
