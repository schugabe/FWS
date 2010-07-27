package fws_master;

import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Allows binding a InputParameter to a Slave. This Parameter will be received from the slave. A InputBinding can have multiple plots configured. 
 * @author Johannes Kasberger
 *
 */
public class SlaveInputBinding extends Binding {
	private Vector<PlotConfig> plotConfig;
	private String plotConfigString;
	
	/**
	 * Class Constructor
	 * @param slave
	 * @param parameter
	 * @param address
	 */
	public SlaveInputBinding(Slave slave, InputParameter parameter, int address) {
		super(slave,parameter,address,false);
		this.setPlotConfig("h24;");	
	}
	
	/**
	 * Class Constructor with more options
	 * @param slave
	 * @param parameter
	 * @param address
	 * @param plotConfig
	 * @param active
	 */
	public SlaveInputBinding(Slave slave, InputParameter parameter, int address,String plotConfig, boolean active) {
		super(slave,parameter,address,active);
		this.setPlotConfig(plotConfig);
	}
	
	/**
	 * This config is checked for valid syntax. Then it's split into its separate plots and saved in a collection.
	 * @param plotConfig the plotConfig to set
	 */
	public boolean setPlotConfig(String plotConfig) {
		if (!plotConfig.matches("(([0-9]*)([h|d|c]{1})([0-9]+)(;))+"))
			return false;
		
		this.plotConfigString = plotConfig;
		StringTokenizer str = new StringTokenizer(plotConfig,";");
		this.plotConfig = new Vector<PlotConfig>();
		
		while (str.hasMoreTokens()) {
			String tmp = str.nextToken();
			
			if(!tmp.matches("([0-9]*)([h|d|c]{1})([0-9]+)"))
				continue;
			
			String [] config = tmp.split("([h|d|c]{1})");
			
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
		
		Collections.sort(this.plotConfig);
		return true;
	}
	
	/**
	 * Get a List of all plots that have to be generated
	 * @return plots
	 */
	public Vector<PlotConfig> getPlots() {
		return this.plotConfig;
	}
	/**
	 * @return the plotConfig
	 */
	public String getPlotConfig() {
		return plotConfigString;
	}
}

