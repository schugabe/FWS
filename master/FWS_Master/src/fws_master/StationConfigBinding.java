package fws_master;

/**
 * This class represents a Binding of a ConfigParameter to a Station. To Value that is set will be transfered to the slave in the configuration phase.
 * @author Johannes Kasberger
 *
 */
public class StationConfigBinding extends Binding {
	private int value;
	private boolean transfered;
	/**
	 * Creates a Binding 
	 * @param station
	 * @param parameter
	 * @param address
	 * @param active
	 */
	public StationConfigBinding(Station station,ConfigParameter parameter,int address,boolean active,boolean transfered) {
		super(station,parameter,address,active);
		this.value = 0;
		this.transfered = transfered;
	}
	
	/**
	 * Creates a Binding with a Value to be transfered.
	 * @param station
	 * @param parameter
	 * @param address
	 * @param value
	 * @param active
	 */
	public StationConfigBinding(Station station,ConfigParameter parameter,int address,int value,boolean active,boolean transfered) {
		super(station,parameter,address,active);
		this.value = value;
		this.transfered = transfered;
	}
	
	/**
	 * @param value the value that should be written to the slave
	 */
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * @return The currently set configuration value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param transfered Set if value has been transfered
	 */
	public void setTransfered(boolean transfered) {
		this.transfered = transfered;
	}

	/**
	 * @return If this value has been transfered to the slave.
	 */
	public boolean isTransfered() {
		return transfered;
	}
}
