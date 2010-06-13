package fws_master;

/**
 * Abstract base class for plots
 * @author Johannes Kasberger
 *
 */
public abstract class PlotBase {
	private PlotController controller;
	private String name;
	
	/**
	 * Each Plot has a path where it saves the plot files
	 * @param name
	 * @param path
	 */
	public PlotBase(String name,PlotController controller) {
		this.setName(name);
		this.setName(name);
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
	 * @return the fileName
	 */
	public String getPath() {
		return controller.getPath();
	}
	
	/**
	 * This method creates the Plot using the data in the Vector data,
	 * @param data measurements
	 * @param preFix is added to the filename to avoid file name conflicts
	 */
	public abstract void createPlot(PlotData data,String preFix);

	/**
	 * @param controller the controller to set
	 */
	public void setController(PlotController controller) {
		this.controller = controller;
	}

	/**
	 * @return the controller
	 */
	public PlotController getController() {
		return controller;
	}
}
