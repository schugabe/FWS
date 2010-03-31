package fws_master;

import java.util.Vector;

import org.eclipse.swt.widgets.Label;

public class Station {
	private Vector<Binding> parameters;
	private String ipAddress;
	private int polling_intervall;
	private String name;
	@SuppressWarnings("unused")
	private Label statusLabel;
	
	public Station(String name) {
		this.setName(name);
		this.polling_intervall = 60;
		this.ipAddress = "";
		this.statusLabel = null;
		this.parameters = new Vector<Binding>();
	}
	
	
	
	public Vector<Binding> getParameters() {
		return this.parameters;
	}
	
	public void deleteStation(){
		for(Binding binding:this.parameters) {
			binding.releaseParameter();
		}
	}
	
	public void addBinding(Binding binding) {
		this.parameters.add(binding);
	}
	
	public boolean removeBinding(Binding binding) {
		if (this.parameters.remove(binding)) {
			binding.releaseParameter();
			return true;
		}
		return false;
	}

	public void setStatusLabel(Label l) {
		this.statusLabel = l;
	}


	/**
	 * @param ip_address the ip_address to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the ip_address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param polling_intervall the polling_intervall to set
	 */
	public void setPollingIntervall(int polling_intervall) {
		this.polling_intervall = polling_intervall;
	}

	/**
	 * @return the polling_intervall
	 */
	public int getPollingIntervall() {
		return polling_intervall;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
