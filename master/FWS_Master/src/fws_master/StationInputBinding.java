package fws_master;

import java.util.StringTokenizer;
import java.util.Vector;

public class StationInputBinding extends Binding {

	private boolean active;
	private Vector<PlotConfig> plotConfig;
	private String plotConfigString;
	
	public StationInputBinding(Station station, InputParameter parameter, int address) {
		super(station,parameter,address);
		this.setPlotConfig("h24;");
		this.active = false;
	}
	public StationInputBinding(Station station, InputParameter parameter, int address,String plotConfig, boolean active) {
		super(station,parameter,address);
		this.setPlotConfig(plotConfig);
		this.active = active;
	}
	/**
	 * @param plotConfig the plotConfig to set
	 */
	public boolean setPlotConfig(String plotConfig) {
		if (!plotConfig.matches("(([0-9]*)([h|d]{1})([0-9]+)(;))+"))
			return false;
		
		this.plotConfigString = plotConfig;
		StringTokenizer str = new StringTokenizer(plotConfig,";");
		this.plotConfig = new Vector<PlotConfig>();
		
		while (str.hasMoreTokens()) {
			String tmp = str.nextToken();
			
			if(!tmp.matches("([0-9]*)([h|d]{1})([0-9]+)"))
				continue;
			
			String [] config = tmp.split("([h|d]{1})");
			
			int length = -1;
			int id = -1;
			
			try {
				length = Integer.parseInt(config[1]);
			} catch (Exception ex) {
			}
				
			try {
				id = Integer.parseInt(config[0]);
			} catch (Exception ex) {
			}

			
			config = tmp.split("([0-9]+)");
			char timeBase = config[config.length-1].charAt(0);
			
			this.plotConfig.add(new PlotConfig(id,length,timeBase));
		}
		
		
		return true;
	}
	
	public Vector<PlotConfig> getPlots() {
		return this.plotConfig;
	}
	/**
	 * @return the plotConfig
	 */
	public String getPlotConfig() {
		return plotConfigString;
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

