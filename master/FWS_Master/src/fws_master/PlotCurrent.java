package fws_master;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;


import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

public class PlotCurrent extends PlotBase {
	private static Logger log = Logger.getLogger("fws_master.plotcurrent");
	public PlotCurrent(String name,PlotController controller) {
		super(name, controller);
		
	}

	@Override
	public void createPlot(PlotData data, String preFix) {
		if (!data.checkData())
			return;
		MeasurementHistory hist = data.getData().get(0);
		
		double avg = 0.0f;
		/*for (MeasurementHistoryEntry e: hist.getValues()) {
			avg += e.getValue();
		}
		avg /= hist.getValues().size();*/
		avg = hist.getValues().getLast().getValue();
		
		ValueDataset dataset = new DefaultValueDataset(avg);
		CompassPlot plot = new CompassPlot(dataset);
        plot.setSeriesNeedle(7);
        plot.setSeriesPaint(0, Color.red);
        plot.setSeriesOutlinePaint(0, Color.red);
        
        plot.setRoseCenterPaint(new Color(255,255,255,0));
        plot.setBackgroundPaint(new Color(255,255,255,0));
        plot.setRosePaint(new Color(255,0,0,50));
        JFreeChart chart = new JFreeChart(plot);
        
        chart.setBackgroundPaint(new Color(255,255,255,0)); 
		
        chart.setTitle(data.getData().get(0).getSlave()+" "+data.getData().get(0).getParameter()+" "+now());
              
        try {
        	ChartRenderingInfo info = new ChartRenderingInfo();
        	String fileName = data.getData().get(0).getSlave()+"_"+data.getData().get(0).getParameter()+preFix+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,this.getController().getWidth(),this.getController().getHeight(),info,true,80);    		
    	} catch (IOException e) {
    		log.severe("Plotting current failed"+e.getMessage());
    	}
	}
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy ");
	    return sdf.format(cal.getTime());

	  }
}
