package fws_master;

import java.util.Vector;

public class Parameter {
	private String name;
	private Vector<Binding> stations;
	private Parameter_Controller controller;
	
	public Parameter(String name,Parameter_Controller controller) {
		this.controller = controller;
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
