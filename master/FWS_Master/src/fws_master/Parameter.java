package fws_master;

import java.util.Vector;

/**
 * A Parameter is the base class for any kind of values that are sent to slaves or received by the master.
 * @author Johannes Kasberger
 *
 */
public class Parameter {
	private String name;
	private Vector<Binding> slaves;
	private ParameterController controller;
	
	/**
	 * A Parameter must have Name and a controller
	 * @param name
	 * @param controller
	 * @throws Exception 
	 */
	public Parameter(String name,ParameterController controller) throws Exception {
		this.controller = controller;
		if (!this.setName(name))
			throw new Exception("Invalid Parameter Name");
		slaves = new Vector<Binding>();
	}
	
	/** 
	 * True if this parameter is currently used by a slave
	 * @return true/false
	 */
	public boolean inUse() {
		if (slaves.size() != 0)
			return true;
		return false;
	}
	
	/**
	 * @param name the name to set. the name must be unique
	 */
	public boolean setName(String name) {
		String tmp = name.trim();
		if (!tmp.matches("(\\w| )+"))
			return false;
		
		if (this.controller != null) {
			for(Parameter p:this.controller.getParameters()) {
				if (p != this && p.getName().equals(tmp))
					return false;
			}
		}
		this.name = tmp;
		return true;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
		
	/**
	 * Slaves where this parameter is in use
	 * @return Vector of Bindings
	 */
	public Vector<Binding> getSlavesBindings() {
		return this.slaves;
	}
	
	/**
	 * Add this Parameter to a slave
	 * @param binding
	 */
	public void addBinding(Binding binding) {
		this.slaves.add(binding);
	}
	
	/**
	 * Remove this Parameter from a slave
	 * @param binding
	 * @return true if is removed
	 */
	public boolean removeBinding(Binding binding) {
		return this.slaves.remove(binding);
	}
}
