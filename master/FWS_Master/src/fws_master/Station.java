package fws_master;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.eclipse.swt.widgets.Label;

public class Station extends Thread{
	private Vector<Binding> parameters;
	private String ipAddress;
	private int polling_intervall;
	private String name;
	private Label statusLabel;
	private Station_Controller controller;
	private volatile boolean suspended;
	
	public Station(String name,Station_Controller controller) {
		this.setStationName(name);
		this.polling_intervall = 60;
		this.ipAddress = "127.0.0.1";
		this.statusLabel = null;
		this.parameters = new Vector<Binding>();
		this.controller = controller;
		this.suspended = true;
	}
	
	public void resumeStation() {
		this.suspended = false;
		synchronized(this) {notify();}
	}
	
	public void pauseStation() {
		this.suspended = true;
	}
	
	public void run() {
		this.suspended = false;
		
		while (true) {
			this.statusLabel.getDisplay().asyncExec(new Runnable() {
				public void run()
				{
					GregorianCalendar cal = new GregorianCalendar();
					statusLabel.setText("Running"+cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" +cal.get(Calendar.SECOND));
				}
			});


			this.getMeasurements();

			try {
				if (this.suspended) {
					synchronized(this) {
						while(this.suspended)
							wait();
					}
					// reinit der parameter
				}

				Thread.sleep(this.polling_intervall*1000);

			} catch (InterruptedException e) {
				this.interrupt();
			}
		}

	}
	
	private void getMeasurements() {
		for(Binding b:this.parameters) {
			if (b instanceof Station_Input_Binding) {
				Station_Input_Binding sb = (Station_Input_Binding)b;
				
			}
		}
	}
	
	public Vector<Binding> getParameters() {
		return this.parameters;
	}
	
	public boolean uploadDeviceConfig(String newIP) {
		// TODO upload config to device
		for (Station s:this.controller.getStations()) {
			if (s.getIpAddress().equals(newIP)) {
				return false;
			}
		}
		this.ipAddress = newIP;
		return true;
	}
	
	public boolean uploadParamsConfig() {
		return true;
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
	public void setStationName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getStationName() {
		return name;
	}
}
