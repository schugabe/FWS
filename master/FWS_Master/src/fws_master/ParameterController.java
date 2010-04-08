package fws_master;

import java.util.Vector;

public class ParameterController {
	private Vector<Parameter> parameters;
	
	public ParameterController() {
		parameters = new Vector<Parameter>();
	}

	/**
	 * @return the parameters
	 */
	public Vector<Parameter> getParameters() {
		return parameters;
	}
	
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}
	
	public boolean removeParameter(Parameter parameter) {
		if (parameter.inUse())
			return false;
		
		return this.parameters.remove(parameter);
	}
	
	public Parameter findParameter(String name) {
		for(Parameter param:this.parameters) {
			if (param.getName().equals(name))
				return param;
		}
		return null;
	}
}
