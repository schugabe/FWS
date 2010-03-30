package fws_master;

import java.util.Vector;

public class Parameter {
	private String name;
	private Vector<Binding> stations;
	
	public Parameter(String name) {
		this.setName(name);
		stations = new Vector<Binding>();
	}
	
	/** 
	 * Liefert true wenn dieser Parameter gerade an eine Station gebunden ist
	 * @return true/false
	 */
	public boolean inUse() {
		if (stations.size() != 0)
			return true;
		return false;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
		
	public Vector<Binding> getStationsBindings() {
		return this.stations;
	}
	
	public void addBinding(Binding binding) {
		this.stations.add(binding);
	}
	
	public boolean removeBinding(Binding binding) {
		return this.stations.remove(binding);
	}
}
