package fws_master;

/**
 * Abstract base class for plots
 * @author Johannes Kasberger
 *
 */
public abstract class PlotBase {
	private String name;
	private String path;
	
	/**
	 * Each Plot has a path where it saves the plot files
	 * @param name
	 * @param path
	 */
	public PlotBase(String name,String path) {
		this.setName(name);
		this.setPath(path);
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

	/**
	 * @param fileName the fileName to set
	 */
	public void setPath(String fileName) {
		this.path = fileName;
	}

	/**
	 * @return the fileName
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * This method creates the Plot using the data in the Vector data,
	 * @param data measurements
	 * @param preFix is added to the filename to avoid file name conflicts
	 */
	public abstract void createPlot(PlotData data,String preFix);
}
