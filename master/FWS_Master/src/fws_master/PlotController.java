package fws_master;

import java.util.Vector;
/**
 * List of all Classes that implement the PlotBase
 * @author Johannes Kasberger
 *
 */
public class PlotController {
	private Vector<PlotBase> plots;
	private String defaultPath;
	private int width, height;
	/**
	 * Class constructor
	 * @param defaultPath Path for the plot files
	 */
	public PlotController(String defaultPath, int width, int height) {
		this.defaultPath = defaultPath;
		this.width = width;
		this.height = height;
		plots = new Vector<PlotBase>(3);
		plots.add(new PlotTime("time",this.defaultPath));
		plots.add(new PlotCurrent("current", this.defaultPath));
	}
	
	/**
	 * Get PlotFile for Name
	 * @param name requested name of plots
	 * @return the plotclass
	 */
	public PlotBase getPlot(String name) {
		for (PlotBase b:this.plots) {
			if (b.getName().equals(name))
				return b;
		}
		return null;
	}

	/**
	 * @param heigth the heigth to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the heigth
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
}
