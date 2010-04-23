package fws_master;

public class PlotConfig {
	private int id;
	private int count;
	private char timeBase;
	
	public PlotConfig(int id,int count, char timeBase) {
		this.id = id;
		this.count = count;
		this.timeBase = timeBase;
	}
	
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
