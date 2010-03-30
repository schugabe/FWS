package fws_master;

import java.util.Vector;

public class Station {
	private Vector<Binding> parameters;
	
	
	public Vector<Binding> getParameters() {
		return this.parameters;
	}
	
	public void addBinding(Binding binding) {
		this.parameters.add(binding);
	}
	
	public boolean removeBinding(Binding binding) {
		return this.parameters.remove(binding);
	}
}
