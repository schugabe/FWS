package fws_master;


import java.io.File;
import java.io.IOException;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;


public class FWS_Master {

	/**
	 * @param args
	 */
	private Parameter_Controller parameter_controller;
	private Station_Controller station_controller;
	@SuppressWarnings("unused")
	private ViewMain view;
	private Display display;
	private Shell shell;
	private MeasurementCollector collector;
	private String outDir;
	
	/*private void generateParameters() {
		Config_Parameter c = new Config_Parameter("Messintervall",this.parameter_controller);
		this.parameter_controller.addParameter(c);
		
		Input_Parameter i = new Input_Parameter("Temperatur",this.parameter_controller,Units.TEMPERATURE,Output_Formats.NK1,History_Functions.MAX);
		this.parameter_controller.addParameter(i);
		
		Station s = new Station("Dach",this.station_controller,"192.168.2.7:30000",10);
		this.station_controller.addStation(s);
		
		Station_Config_Binding cb = new Station_Config_Binding(s,c,0,1);
		Station_Input_Binding b = new Station_Input_Binding(s,i,1);
		
	}*/
	
	private FWS_Master(Shell shell, Display display,String outDir) {
		this.outDir = outDir;
		PersistencePreferences pref = new PersistencePreferences(".","settings.xml");
		
		this.parameter_controller = pref.loadParameters();
		this.station_controller = pref.loadStations(this.parameter_controller);
		
		this.collector = new MeasurementCollector(this.station_controller,10,this.outDir);
		
		//this.generateParameters();
		view = new ViewMain(shell,display,this);
		this.shell = shell;
		this.display = display;
		this.collector.start();
	}
	
	public Station_Controller getStationController() {
		return this.station_controller;
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);		
		FWS_Master master = new FWS_Master(shell,display,".");
		
		shell.setSize(400,500);
		shell.open ();
		
		
		DefaultXYDataset data = new DefaultXYDataset();
		double [][] tmp = new double[2][8];
		for(int y=0;y<8;y++)
		{
		tmp[0][y] = y;
		tmp[1][y] = y;
		}
		data.addSeries("bla", tmp);
		JFreeChart chart = ChartFactory.createXYLineChart("Bla", "hui", "test", data, PlotOrientation.HORIZONTAL, true, false, false);
		/*ChartRenderingInfo info = new ChartRenderingInfo();
		try {
		ChartUtilities.saveChartAsPNG(new File("freespace.png"),chart,600,400,info);
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}*/
		
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		display.dispose();
		master.Shutdown();
		System.exit(0);
	}
	
	private void Shutdown() {
		PersistencePreferences pref = new PersistencePreferences(".","settings.xml");
		pref.saveSettings(this.parameter_controller,this.station_controller);
		
	}

	public void ParameterClicked() {
		Shell param_shall = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		param_shall.setLocation (pt.x, pt.y);
		param_shall.setText ("Parameter Verwalten");
		param_shall.setSize (600, 400);
		
		@SuppressWarnings("unused")
		ViewParameters view_parameters = new ViewParameters(param_shall,this.parameter_controller);
		param_shall.open();
		
		return;
	}

	public void StationClicked() {
		Shell tmp_shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		tmp_shell.setLocation (pt.x, pt.y);
		tmp_shell.setText ("Stationen verwalten");
		tmp_shell.setSize (600, 400);
		
		@SuppressWarnings("unused")
		ViewStation view_stat = new ViewStation(tmp_shell,this.station_controller,this.parameter_controller);
		tmp_shell.open();
		
		return;
		
	}
	
	public void FolderClicked() {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		String platform = SWT.getPlatform();
		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
		this.outDir =  dialog.open();
		this.collector.setOutDir(this.outDir);
	}
	
	public void StartClicked(boolean start) {
		this.station_controller.startStation(start);
	}

}
