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
	/**
	 * Class constructor
	 * @param defaultPath Path for the plot files
	 */
	public PlotController(String defaultPath) {
		this.defaultPath = defaultPath;
		
		plots = new Vector<PlotBase>(3);
		plots.add(new PlotTime("Zeitverlauf",this.defaultPath));
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
}
