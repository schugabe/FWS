package fws_master;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CompassFormat;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Plot with x axis as time stamps and y axis the values of the measurements
 * @author johannes
 *
 */
public class PlotTime extends PlotBase {
	private static Logger log = Logger.getLogger("fws_master.plottime");
	
	/**
	 * Call the base constructor
	 * @param name
	 * @param controller
	 */
	public PlotTime(String name,PlotController controller) {
		super(name, controller);
	}

	/**
	 * Create the Plot
	 */
	public void createPlot(PlotData data,String prefix) {
		
		if (data == null || !data.checkData())
			return;
				
		String title = "";
		String fileName = "";
		TimeSeriesCollection dataset;
		int i,cnt;
		i = 0;
		cnt = data.getData().size();
		
		JFreeChart chart = null;
		XYPlot plot = null;
		
		for(MeasurementHistory hist:data.getData()) {
			dataset = new TimeSeriesCollection();
			TimeSeries s1 = new TimeSeries(hist.getParameter()+"["+Units.getString(hist.getUnit())+"]");
			if (title.indexOf(hist.getSlave()) == -1)
				title+=" "+hist.getSlave();
			fileName += hist.getSlave()+"_"+hist.getParameter();
			
			double min= Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			//double old = 0.0f;
			//double e = 0.08f;
			//boolean inited = false;
			for (MeasurementHistoryEntry m:hist.getValues()) {
				double tmp = m.getValue();
				/*if (!inited) {
					inited = true;
					old = tmp;
				}
				tmp = (1.0f-e)*old+e*tmp;
				old = tmp;*/
				if (tmp < min)
					min = tmp;
				if (tmp > max)
					max = tmp;
				
				s1.addOrUpdate(new Second(m.getTimestamp()), tmp);
			}
	        dataset.addSeries(s1);
	        
	        if (i == 0) {
	        	chart = ChartFactory.createTimeSeriesChart(title+" "+now(), "Zeit", "Wert", dataset, true, true, false );
	    		plot = chart.getXYPlot();
	    		
	    		dataset = new TimeSeriesCollection();
	        } else {
	        	plot.setDataset(i,dataset);
	        }
	        
	        NumberAxis yaxis = new NumberAxis(""+plot.getDataset(i).getSeriesKey(0));
	        yaxis.setAutoRangeIncludesZero(false);
	        plot.setRangeAxis(i, yaxis);
	        plot.mapDatasetToRangeAxis(i, i);
	        Range r = plot.getRangeAxis(i).getRange();
	        if (r.getLength() < 10.0f) {
	        	yaxis.setRange(min-5.0f, max+5.0f);
	        	plot.setRangeAxis(i, yaxis);
	        }
	        i++;
		}
		
		if (plot == null || chart == null)
			return;
					
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		
		if (data.getConfiguration().getTimeBase() == 'h')
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		else
			axis.setDateFormatOverride(new SimpleDateFormat("dd.MM"));
		
        ChartRenderingInfo info = new ChartRenderingInfo();
       
        XYLineAndShapeRenderer renderer,renderer_base; 
       
        renderer_base = (XYLineAndShapeRenderer) plot.getRenderer();
       
        for(i = 0; i < cnt; i++) {
        	Color tmp;
        	try {
				renderer = (XYLineAndShapeRenderer) renderer_base.clone();
			} catch (CloneNotSupportedException e) {
				return;
			}
        	if (i == 0)
        		tmp = Color.red;
        	else if (i == 1) 
        		tmp = Color.blue;
        	else if (i == 2)
        		tmp = Color.orange;
        	else if (i == 3)
        		tmp = Color.cyan;
        	else if (i == 4)
        		tmp = Color.pink;
        	else 
        		tmp = Color.black;
        	renderer.setSeriesPaint(0,tmp);
        	
        	
        	if (Units.DIRECTION == data.getData().get(i).getUnit()) {
        		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(i);
                rangeAxis.setAutoRangeIncludesZero(false);
                
                TickUnits units = new TickUnits();
                units.add(new NumberTickUnit(180.0, new CompassFormat()));
                units.add(new NumberTickUnit(90.0, new CompassFormat()));
                units.add(new NumberTickUnit(45.0, new CompassFormat()));
                units.add(new NumberTickUnit(22.5, new CompassFormat()));
                
                rangeAxis.setStandardTickUnits(units);
                plot.setRangeAxis(i, rangeAxis);
                plot.mapDatasetToRangeAxis(i, i);
                renderer.setSeriesLinesVisible(0, false);
                renderer.setSeriesShapesVisible(0,true);
        	}
        	plot.setRenderer(i,renderer);
        }
        
        try {
        	fileName += prefix+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,this.getController().getWidth(),this.getController().getHeight(),info);
    	} catch (IOException e) {
    		log.severe("Exception in createPlot(time): "+e.getStackTrace());
    	}
	}
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy ");
	    return sdf.format(cal.getTime());

	  }

}
