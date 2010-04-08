package fws_master;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.widgets.Label;

public class Station extends Thread{
	private Vector<Binding> parameters;
	private Vector<Measurement> measurements;
	private String ipAddress;
	private int polling_intervall;
	private String name;
	private Label statusLabel;
	private StationController controller;
	private volatile boolean suspended;
	private ModBusWrapper wrapper;
	//private int lastCollected;
	
	public Station(String name,StationController controller) {
		this.setStationName(name);
		this.controller = controller;
		this.polling_intervall = 60;
		this.ipAddress = "127.0.0.1";
		this.statusLabel = null;
		this.init();
	}
	
	public Station(String name,StationController controller,String ip,int polling_intervall) {
		this.setStationName(name);
		this.controller = controller;
		this.polling_intervall = polling_intervall;
		this.ipAddress = ip;
		this.statusLabel = null;
		this.init();
	}
	
	private void init() {
		this.parameters = new Vector<Binding>();
		this.measurements = new Vector<Measurement>();
		this.suspended = true;
		//this.lastCollected = 0;
		this.setName(this.name);
		
	}
	
	public void resumeStation() {
		this.suspended = false;
		synchronized(this) {notify();}
	}
	
	public void pauseStation() {
		this.suspended = true;
	}
	
	private void setLabel(final String msg) {
		this.statusLabel.getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			    String date =  sdf.format(cal.getTime());

				statusLabel.setText(""+msg+" um "+ date);
			}
		});
	}
	
	public void run() {
		this.suspended = false;
		wrapper = new ModBusWrapper(this.ipAddress);
		while (true) {
			try {
				if (this.suspended) {
					this.wrapper.releaseConnection();
					this.setLabel("Pause");
					synchronized(this) {
						while(this.suspended)
							wait();
					}
					this.wrapper = new ModBusWrapper(this.ipAddress);
					setLabel("Gestartet");
				}
				this.getMeasurements();
				Thread.sleep(this.polling_intervall*1000);

			} catch (InterruptedException e) {
				
			}
		}

	}
	
	private void getMeasurements() {
		
		/*if (!wrapper.hasConnection()) {
			this.setLabel("Keine Verbindung");
			return;
		}*/
		this.setLabel("Online");
		for(Binding b:this.parameters) {
			if (b instanceof StationInputBinding) {
				int result = wrapper.sendReadRequest(b.getAddress());
				Measurement m = new Measurement(this,(InputParameter) b.getParameter(),result);
				synchronized(this.measurements) {
					this.measurements.add(m);
					System.out.println(this.name+" "+m.getParameter().getName()+" "+m.getConvValue());
				}
			}
		}
	}
	
	public Vector<Binding> getParameters() {
		return this.parameters;
	}
	
	public boolean uploadDeviceConfig(String newIP) {
		
		if(!this.ipAddress.equals(newIP)) {
			ModBusWrapper config_wrapper = new ModBusWrapper(this.ipAddress);
			for (Station s:this.controller.getStations()) {
				if (s.getIpAddress().equals(newIP)) {
					return false;
				}
			}
			int []int_ip = this.convertIP(newIP);

			config_wrapper.sendWriteRequest(0, int_ip[0]);
			//config_wrapper.sendWriteRequest(1, int_ip[1]);
			config_wrapper.releaseConnection();
		}
		this.ipAddress = newIP;
		return true;
	}
	
	private int[] convertIP(String ip) {
		int [] conv = new int[2];
		int idx = ip.indexOf(':');
	    
	    if(idx > 0) {
	      ip = ip.substring(0,idx);
	    }
	    
	    int tmp = 0;
	    String sub;
	    StringTokenizer st = new StringTokenizer(ip,".");
	    while (st.hasMoreTokens()) {
	         sub = st.nextToken();
	         if (tmp == 0) {
	        	 conv[0] = Integer.parseInt(sub) << 8;
	         }
	         else if (tmp == 1) {
	        	 conv[0] |= Integer.parseInt(sub);
	        	 
	         }
	         else if (tmp == 2) {
	        	 conv[1] = Integer.parseInt(sub) << 8;
	         }
	         else if (tmp == 3) {
	        	 conv[1] |= Integer.parseInt(sub);
	         }
	         else
	        	 break;
	         tmp++;
	     }

		
		return conv;
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
	
	public int getInputParamsCount() {
		int tmp = 0;
		for(Binding b:this.parameters) {
			if (b instanceof StationInputBinding)
				tmp++;
		}
		return tmp;
	}
	
	public Vector<Measurement> getLastMeasurements() {
		Vector<Measurement> tmp;
		
		synchronized(this.measurements) {
			//int params_count = this.getInputParamsCount();

			int size = this.measurements.size(); //- this.lastCollected;

			tmp = new Vector<Measurement>(size);
			int i = 0;
			while (i < size) {
				tmp.add(this.measurements.get(i));
				i++;
			}
			this.measurements.clear();
			//this.lastCollected = size;
		}
		return tmp;
	}
}
