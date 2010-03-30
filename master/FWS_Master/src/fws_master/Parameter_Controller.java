package fws_master;

import java.util.Vector;

public class Parameter_Controller {
	private Vector<Parameter> parameters;
	
	public Parameter_Controller() {
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
			if (param.getName()==name)
				return param;
		}
		return null;
	}
}
