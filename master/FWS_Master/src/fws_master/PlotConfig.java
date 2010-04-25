package fws_master;

/**
 * A PlotConfig is created for each plot that a user configures for a input binding. In this configuration the data range is saved that is used 
 * for the creation of the plot.
 * @author Johannes Kasberger
 *
 */
public class PlotConfig {
	private int id;
	private int count;
	private char timeBase;
	
	/**
	 * Create a Plot Configuration
	 * @param id with the same id the data of this bindings are saved to the same plot
	 * @param count amount of timeBase used in the plot
	 * @param timeBase daily or hourly 
	 */
	public PlotConfig(int id,int count, char timeBase) {
		this.id = id;
		this.count = count;
		this.timeBase = timeBase;
	}
	
	/**
	 * Without a id only one kind of data will be shown in the plot
	 * @param count amount of timeBase used in the plot
	 * @param timeBase daily or hourly 
	 */
	public PlotConfig(int count, char timeBase) {
		this.id = -1;
		this.count = count;
		this.timeBase = timeBase;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param timeBase the timeBase to set
	 */
	public void setTimeBase(char timeBase) {
		this.timeBase = timeBase;
	}
	/**
	 * @return the timeBase
	 */
	public char getTimeBase() {
		return timeBase;
	}
}
