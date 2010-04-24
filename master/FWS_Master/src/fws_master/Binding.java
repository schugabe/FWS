package fws_master;
/**
 * Binding is the abstract base class for binding parameters to stations. 
 * 
 * @author Johannes Kasberger
 * @see StationConfigBinding 
 * @see StationInputBinding
 * @see Parameter
 * @see Station
 */
public abstract class Binding {
	/** 
	 * Address in the Memory of the Slave
	 */
	private int address;
	private Parameter parameter;
	private Station station;
	
	/**
	 * Default constructor
	 */
	public Binding() {
		this.address = -1;
		this.station = null;
		this.parameter = null;
	}
	
	/**
	 * Constructor for Binding a Parameter to a Station at a address.
	 * @param station
	 * @param parameter
	 * @param address the address of the memory on the station 
	 */
	public Binding(Station station, Parameter parameter, int address) {
		this.setParameter(parameter);
		this.setStation(station);
		this.address = address;
	}
	
	/**
	 * Removes the Binding from the Parameter
	 */
	public void releaseParameter() {
		this.parameter.removeBinding(this);
	}
	
	/**
	 * Removes the Binding from the Station
	 */
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
	 * Removes the binding from its old Station if there is an existing binding
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
