package fws_master;

public class StationConfigBinding extends Binding {
	private int value;
	
	public StationConfigBinding(Station station,ConfigParameter parameter,int address) {
		super(station,parameter,address);
		this.value = 0;
	}
	
	public StationConfigBinding(Station station,ConfigParameter parameter,int address,int value) {
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
