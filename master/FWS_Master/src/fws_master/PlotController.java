package fws_master;

import java.util.Vector;

public class PlotController {
	private Vector<PlotBase> plots;
	private String defaultPath;
	
	public PlotController(String defaultPath) {
		this.defaultPath = defaultPath;
		
		plots = new Vector<PlotBase>(3);
		plots.add(new PlotTime("Zeitverlauf",defaultPath));
	}
	
	public PlotBase getPlot(String name) {
		for (PlotBase b:this.plots) {
			if (b.getName().equals(name))
				return b;
		}
		return null;
	}
}
