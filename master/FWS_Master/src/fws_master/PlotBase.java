package fws_master;

import java.util.Vector;

public abstract class PlotBase {
	private String name;
	private String path;
	
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
	
	public abstract void createPlot(Vector<Measurement> data);
}
