package fws_master;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class PlotTime extends PlotBase {

	public PlotTime(String name, String path) {
		super(name, path);
	}

	public void createPlot(MeasurementHistory data) {
		
		if (data == null)
			return;
		
		final TimeSeries s1 = new TimeSeries(data.getParameter());
		
		for (MeasurementHistoryEntry m:data.getValues()) {
			
			s1.add(new Second(m.getTimestamp()), m.getValue());
		}
		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
	            data.getStation()+" "+data.getParameter(),
	            "Zeitpunkt", Units.getString(data.getUnit()),
	            dataset,
	            true,
	            true,
	            false
	        );
		final XYPlot plot = chart.getXYPlot();
		
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm dd.MM"));
        ChartRenderingInfo info = new ChartRenderingInfo();
        
        try {
        	String fileName = data.getStation()+"_"+data.getParameter()+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,800,600,info);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
}
