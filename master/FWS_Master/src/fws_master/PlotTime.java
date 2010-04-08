package fws_master;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class PlotTime extends PlotBase {

	public PlotTime(String name, String path) {
		super(name, path);
	}

	public void createPlot(Vector<Measurement> data) {
		
		if (data.size() == 0)
			return;
		
		Measurement mT = data.firstElement();
		InputParameter ip = mT.getParameter();
		
		if (ip == null)
			return;
		
		final TimeSeries s1 = new TimeSeries(ip.getName());
		
		for (Measurement m:data) {
			Date d = (Date)new Time(m.getTimestamp());
			s1.add(new Second(d), m.getConvValue());
		}
		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
	            mT.getStation().getStationName()+" "+ip.getName(),
	            "Zeitpunkt", Units.getString(ip.getUnit()),
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
        	String fileName = mT.getStation().getStationName()+"_"+mT.getParameter().getName()+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,800,600,info);
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    		
		/*DefaultXYDataset data = new DefaultXYDataset();
		double [][] tmp = new double[2][8];
		for(int y=0;y<8;y++)
		{
		tmp[0][y] = y;
		tmp[1][y] = y;
		}
		data.addSeries("bla", tmp);
		JFreeChart chart = ChartFactory.createXYLineChart("Bla", "hui", "test", data, PlotOrientation.HORIZONTAL, true, false, false);
		ChartRenderingInfo info = new ChartRenderingInfo();
		try {
		ChartUtilities.saveChartAsPNG(new File("freespace.png"),chart,600,400,info);
		} catch (IOException e) {
		e.printStackTrace();
		}*/

	}

}
