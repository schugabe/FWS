package fws_master;

public class Input_Parameter extends Parameter{
	private Units unit;
	private Output_Formats format;
	private History_Functions history_function;
	
	public Input_Parameter(String name) {
		super(name);
		
	}
	
	public Input_Parameter(String name,Units unit,Output_Formats format, History_Functions history_function) {
		super(name);
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
	public void setFormat(Output_Formats format) {
		this.format = format;
	}

	/**
	 * @return the format
	 */
	public Output_Formats getFormat() {
		return format;
	}

	/**
	 * @param history_function the history_function to set
	 */
	public void setHistory_function(History_Functions history_function) {
		this.history_function = history_function;
	}

	/**
	 * @return the history_function
	 */
	public History_Functions getHistory_function() {
		return history_function;
	}

}
