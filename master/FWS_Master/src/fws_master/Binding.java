package fws_master;

public class Binding {
	private int address;
	private Parameter parameter;
	private Station station;
	
	public Binding() {
		this.address = -1;
		this.station = null;
		this.parameter = null;
	}
	
	public Binding(Station station, Parameter parameter, int address) {
		this.setParameter(parameter);
		this.setStation(station);
		this.parameter = parameter;
	}
	
	public void releaseParameter() {
		this.parameter.removeBinding(this);
	}
	
	public void releaseStation() {
		this.station.removeBinding(this);
	}
	
	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		this.address = address;
	}
	/**
	 * @return the address
	 */
	public int getAddress() {
		return address;
	}
	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(Parameter parameter) {
		if (this.parameter != null)
			this.parameter.removeBinding(this);
		this.parameter = parameter;
		this.parameter.addBinding(this);
	}
	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}
	/**
	 * @param station the station to set
	 */
	public void setStation(Station station) {
		if(this.station != null)
			this.station.removeBinding(this);
		this.station = station;
		this.station.addBinding(this);
		
	}
	/**
	 * @return the station
	 */
	public Station getStation() {
		return station;
	}
	
	
}
