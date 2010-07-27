package fws_master;

import java.util.Vector;

/**
 * This class handles a list of Parameters. 
 * @author Johannes Kasberger
 *
 */
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
	
	/**
	 * add a parameter to the list
	 * @param parameter
	 */
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}
	
	/**
	 * Remove a parameter from the list. Only possible if this parameter isn't in use of a slave
	 * @param parameter
	 * @return true if removing was possible
	 */
	public boolean removeParameter(Parameter parameter) {
		if (parameter.inUse())
			return false;
		
		return this.parameters.remove(parameter);
	}
	
	/**
	 * Return a Parameter with search name from list
	 * @param name
	 * @return Parameter with searched name
	 */
	public Parameter findParameter(String name) {
		for(Parameter param:this.parameters) {
			if (param.getName().equals(name))
				return param;
		}
		return null;
	}
}
