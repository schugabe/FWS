package fws_master;

import java.util.Vector;

public class PlotData {
	private PlotConfig configuration;
	private Vector<MeasurementHistory> data;
	
	public PlotData(PlotConfig configuration, Vector<MeasurementHistory> data) {
		this.setConfiguration(configuration);
		this.setData(data);
	}
	
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
}
