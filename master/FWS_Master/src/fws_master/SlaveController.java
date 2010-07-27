package fws_master;

import java.util.Vector;

/**
 * Manages a List of all Slaves
 * @author Johannes Kasberger
 *
 */
public class SlaveController {
	private Vector<Slave> slaves;
	private boolean running = false;
	/**
	 * Just creates a new List
	 */
	public SlaveController() {
		slaves = new Vector<Slave>();
	}

	/**
	 * @param slave the slaves to add
	 * @throws Exception 
	 */
	public void addSlave(Slave slave) throws Exception {
		for (Slave s:slaves) {
			if (s.getSlaveName().equals(slave.getSlaveName()) || s.getIpAddress().equals(slave.getIpAddress()))
				throw new Exception("Slave with same Name or IP exists");
		}
		this.slaves.add(slave);;
	}
	
	/**
	 * Removes a Slave
	 * @param slave the slave to be removed
	 * @return true if removing was possible
	 */
	public boolean removeSlave(Slave slave) {
		if (this.slaves.remove(slave)) {
			slave.deleteSlave();
			return true;
		}
		return false;
	}

	/**
	 * Finds a Slave 
	 * @param name Name of the slave that is searched
	 * @return the slave with the searched name
	 */
	public Slave findSlave(String name) {
		for(Slave s:this.slaves) {
			if(s.getSlaveName().equals(name))
				return s;
		}
		return null;
	}
	
	/**
	 * @return the slaves
	 */
	public Vector<Slave> getSlaves() {
		return slaves;
	}

	/**
	 * Starts or Pauses all Slaves of this controller
	 * @param start if true the threads a started or continued, when false the threads are paused
	 */
	public void startSlaves(boolean start) {
		
		for (Slave s:this.slaves) {
			if (start && !s.isAlive()) {
				s.start();
				running = true;
			}
			else if (start && s.isAlive()) {
				s.resumeSlave();
				running = true;
			}
			else if (!start && s.isAlive()){
				s.pauseSlave();
				s.interrupt();
				running = false;
			}
		}
	}
	
	public boolean isRunning() {
		return running;
	}
}
