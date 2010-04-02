package fws_master;

public class Station_Config_Binding extends Binding {
	private int value;
	
	public Station_Config_Binding(Station station,Config_Parameter parameter,int address) {
		super(station,parameter,address);
		this.value = 0;
	}
	
	public Station_Config_Binding(Station station,Config_Parameter parameter,int address,int value) {
		super(station,parameter,address);
		this.value = value;
		
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
}
