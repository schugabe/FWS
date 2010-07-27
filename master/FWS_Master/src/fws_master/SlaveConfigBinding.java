package fws_master;

/**
 * This class represents a Binding of a ConfigParameter to a Slave. To Value that is set will be transfered to the slave in the configuration phase.
 * @author Johannes Kasberger
 *
 */
public class SlaveConfigBinding extends Binding {
	private int value;
	private boolean transfered;
	/**
	 * Creates a Binding 
	 * @param slave
	 * @param parameter
	 * @param address
	 * @param active
	 */
	public SlaveConfigBinding(Slave slave,ConfigParameter parameter,int address,boolean active,boolean transfered) {
		super(slave,parameter,address,active);
		this.value = 0;
		this.transfered = transfered;
	}
	
	/**
	 * Creates a Binding with a Value to be transfered.
	 * @param slave
	 * @param parameter
	 * @param address
	 * @param value
	 * @param active
	 */
	public SlaveConfigBinding(Slave slave,ConfigParameter parameter,int address,int value,boolean active,boolean transfered) {
		super(slave,parameter,address,active);
		this.value = value;
		this.transfered = transfered;
	}
	
	public void setAddress(int address) {
		if (this.getAddress() != address)
			this.setTransfered(false);
		super.setAddress(address);
	}
	
	/**
	 * @param value the value that should be written to the slave
	 */
	public void setValue(int value) {
		if (this.value != value)
			this.setTransfered(false);
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
