package fws_master;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

public class PlotCurrent extends PlotBase {
	private String bgPath;
	public PlotCurrent(String name, String path) {
		super(name, path);
		bgPath = path+File.separator+"compassbg.png";
	}

	@Override
	public void createPlot(PlotData data, String preFix) {
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
        
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image;
		try {
			image = ImageIO.read(new File(bgPath));
			plot.setBackgroundImage(image);
			chart.setBackgroundImage(image);
			plot.setBackgroundAlpha(0.0f);
			plot.setBackgroundPaint(new Color(255,255,255,0));
			plot.setRoseCenterPaint(new Color(255,255,255,0));
		} catch (IOException e1) {
			
		}
                
        chart.setTitle(data.getData().get(0).getStation()+" "+data.getData().get(0).getParameter()+" "+now());
              
        try {
        	ChartRenderingInfo info = new ChartRenderingInfo();
        	String fileName = data.getData().get(0).getStation()+"_"+data.getData().get(0).getParameter()+preFix+".png";
    		ChartUtilities.saveChartAsPNG(new File(this.getPath(), fileName),chart,800,600,info);
    		
    	} catch (IOException e) {
    		
    	}
	}
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy ");
	    return sdf.format(cal.getTime());

	  }
}
