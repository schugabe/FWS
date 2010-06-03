package fws_master;
/**
 * A ConfigParameter is a Parameter that will be submitted to the slave. Can be used to configure the behaviour of the slave. There must be an StationConfigBinding to bind a ConfigParameter to a Station.
 * @author Johannes Kasberger
 *
 */
public class ConfigParameter extends Parameter {
	/**
	 * Class constructor
	 * @param name Name of the Parameter
	 * @param controller Controller of the Parameter
	 * @throws Exception 
	 */
	public ConfigParameter(String name,ParameterController controller) throws Exception {
		super(name,controller);
	}

}
