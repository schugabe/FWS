package fws_master;
/**
 * Binding is the abstract base class for binding parameters to slaves. 
 * 
 * @author Johannes Kasberger
 * @see SlaveConfigBinding 
 * @see SlaveInputBinding
 * @see Parameter
 * @see Slave
 */
public abstract class Binding {
	/** 
	 * Address in the Memory of the Slave
	 */
	private int address;
	private Parameter parameter;
	private Slave slave;
	private boolean active;
	
	/**
	 * Default constructor
	 */
	public Binding() {
		this.address = -1;
		this.slave = null;
		this.parameter = null;
		this.active = false;
	}
	
	/**
	 * Constructor for Binding a Parameter to a Slave at a address.
	 * @param slave
	 * @param parameter
	 * @param address the address of the memory on the slave 
	 */
	public Binding(Slave slave, Parameter parameter, int address, boolean active) {
		this.setParameter(parameter);
		this.setSlave(slave);
		this.address = address;
		this.active = active;
	}
	
	/**
	 * Removes the Binding from the Parameter
	 */
	public void releaseParameter() {
		this.parameter.removeBinding(this);
	}
	
	/**
	 * Removes the Binding from the Slave
	 */
	public void releaseSlave() {
		this.slave.removeBinding(this);
	}
	
	/**
	 * @param address the address to set
	 */
	public void setAddress(int address) {
		this.address = address;
	}
	/**
	 * @return the address
	 */
	public int getAddress() {
		return address;
	}
	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(Parameter parameter) {
		if (this.parameter != null)
			this.parameter.removeBinding(this);
		this.parameter = parameter;
		this.parameter.addBinding(this);
	}
	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}
	/**
	 * Removes the binding from its old Slave if there is an existing binding
	 * @param slave the slave to set
	 */
	public void setSlave(Slave slave) {
		if(this.slave != null)
			this.slave.removeBinding(this);
		this.slave = slave;
		this.slave.addBinding(this);
		
	}
	/**
	 * @return the slave
	 */
	public Slave getSlave() {
		return slave;
	}
	
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}
}
