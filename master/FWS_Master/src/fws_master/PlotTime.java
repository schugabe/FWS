package fws_master;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Plot with x axis as time stamps and y axis the values of the measurements
 * @author johannes
 *
 */
public class PlotTime extends PlotBase {

	/**
	 * Call the base constructor
	 * @param name
	 * @param path
	 */
	public PlotTime(String name, String path) {
		super(name, path);
	}

	/**
	 * Create the Plot
	 */
	public void createPlot(Vector<MeasurementHistory> data,String prefix) {
		
		if (data == null)
			return;
		
		if(data.firstElement() == null)
			return;
		
		String title = "";
		String fileName = "";
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for(MeasurementHistory hist:data) {
			TimeSeries s1 = new TimeSeries(hist.getParameter()+"["+Units.getString(hist.getUnit())+"]");
			title+=" "+hist.getStation();
			fileName += hist.getStation()+"_"+hist.getParameter();
			for (MeasurementHistoryEntry m:hist.getValues()) {
				
				s1.addOrUpdate(new Second(m.getTimestamp()), m.getValue());
			}
	        dataset.addSeries(s1);
		}
		
		

		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Zeitpunkt", "Wert", dataset, true, true, false );
		XYPlot plot = chart.getXYPlot();
		
		DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd.MM"));
        ChartRenderingInfo info = new ChartRenderingInfo();
        
        try {
        	fileName += prefix+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,800,600,info);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
}
