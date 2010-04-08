package fws_master;

public class InputParameter extends Parameter{
	private Units unit;
	private OutputFormats format;
	private HistoryFunctions history_function;
	
	public InputParameter(String name,ParameterController controller) {
		super(name,controller);
		
	}
	
	public InputParameter(String name,ParameterController controller, Units unit,OutputFormats format, HistoryFunctions history_function) {
		super(name,controller);
		this.format = format;
		this.unit = unit;
		this.history_function = history_function;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(Units unit) {
		this.unit = unit;
	}

	/**
	 * @return the unit
	 */
	public Units getUnit() {
		return unit;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(OutputFormats format) {
		this.format = format;
	}

	/**
	 * @return the format
	 */
	public OutputFormats getFormat() {
		return format;
	}

	/**
	 * @param history_function the history_function to set
	 */
	public void setHistory_function(HistoryFunctions history_function) {
		this.history_function = history_function;
	}

	/**
	 * @return the history_function
	 */
	public HistoryFunctions getHistory_function() {
		return history_function;
	}

}
