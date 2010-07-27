package fws_master;

import java.util.Vector;
/**
 * This Class is used to build the needed information for the plot. It contains the configuration for this plot. 
 * All the data that should be in the plot is added to it. The PlotBase class extracts the information and plots the data.
 * @author Johannes Kasberger
 *
 */
public class PlotData {
	private PlotConfig configuration;
	private Vector<MeasurementHistory> data;
	
	/**
	 * Generate a new PlotData object
	 * @param configuration the configuration for the plot
	 * @param data the data for the plot
	 */
	public PlotData(PlotConfig configuration, Vector<MeasurementHistory> data) {
		this.setConfiguration(configuration);
		this.setData(data);
	}
	
	/**
	 * Add Data to this Plot
	 * @param newData the data to be added
	 */
	public void addData(MeasurementHistory newData) {
		this.data.add(newData);
	}
	

	/**
	 * @param data the data to set
	 */
	public void setData(Vector<MeasurementHistory> data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public Vector<MeasurementHistory> getData() {
		return data;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(PlotConfig configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the configuration
	 */
	public PlotConfig getConfiguration() {
		return configuration;
	}
	
	public boolean checkData() {
		for (MeasurementHistory h: data) {
			if (h == null || h.getParameter() == null || h.getSlave() == null || h.getUnit() == null || h.getValues() == null || h.getValues().size() == 0)
				return false;
		}
		return true;
	}
}
