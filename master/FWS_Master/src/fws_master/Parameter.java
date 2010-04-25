package fws_master;

import java.util.Vector;

/**
 * A Parameter is the base class for any kind of values that are sent to slaves or received by the master.
 * @author Johannes Kasberger
 *
 */
public class Parameter {
	private String name;
	private Vector<Binding> stations;
	private ParameterController controller;
	
	/**
	 * A Parameter must have Name and a controller
	 * @param name
	 * @param controller
	 */
	public Parameter(String name,ParameterController controller) {
		this.controller = controller;
		this.setName(name);
		stations = new Vector<Binding>();
	}
	
	/** 
	 * True if this parameter is currently used by a station
	 * @return true/false
	 */
	public boolean inUse() {
		if (stations.size() != 0)
			return true;
		return false;
	}
	
	/**
	 * @param name the name to set. the name must be unique
	 */
	public boolean setName(String name) {
		if (this.controller != null) {
			for(Parameter p:this.controller.getParameters()) {
				if (p != this && p.getName().equals(name))
					return false;
			}
		}
		this.name = name;
		return true;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
		
	/**
	 * Stations where this parameter is in use
	 * @return Vector of Bindings
	 */
	public Vector<Binding> getStationsBindings() {
		return this.stations;
	}
	
	/**
	 * Add this Parameter to a station
	 * @param binding
	 */
	public void addBinding(Binding binding) {
		this.stations.add(binding);
	}
	
	/**
	 * Remove this Parameter from a station
	 * @param binding
	 * @return true if is removed
	 */
	public boolean removeBinding(Binding binding) {
		return this.stations.remove(binding);
	}
}
