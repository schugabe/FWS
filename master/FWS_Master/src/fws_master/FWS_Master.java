package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

public class FWS_Master {

	/**
	 * @param args
	 */
	private Parameter_Controller parameter_controller;
	private Station_Controller station_controller;
	@SuppressWarnings("unused")
	private ViewMain view;
	private Display display;
	
	private void generateParameters() {
		Config_Parameter c = new Config_Parameter("Messintervall",this.parameter_controller);
		this.parameter_controller.addParameter(c);
		
		Input_Parameter i = new Input_Parameter("Temperatur",this.parameter_controller,Units.TEMPERATURE,Output_Formats.NK1,History_Functions.MAX);
		this.parameter_controller.addParameter(i);
		
		Station s = new Station("Dach",this.station_controller,"192.168.2.7:30000",10);
		this.station_controller.addStation(s);
		
		Station_Config_Binding cb = new Station_Config_Binding(s,c,0,1);
		Station_Input_Binding b = new Station_Input_Binding(s,i,1);
		
		//s = new Station("Dach2",this.station_controller,"127.0.0.3",30);
		//this.station_controller.addStation(s);
	}
	
	private FWS_Master(Shell shell, Display display) {
		this.parameter_controller = new Parameter_Controller();
		this.station_controller = new Station_Controller();
		this.generateParameters();
		view = new ViewMain(shell,display,this);
		
		this.display = display;
		
	}
	
	public Station_Controller getStationController() {
		return this.station_controller;
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		
		@SuppressWarnings("unused")
		FWS_Master master = new FWS_Master(shell,display);
		shell.setSize(400,500);
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		display.dispose ();
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
	
	public void StartClicked(boolean start) {
		this.station_controller.startStation(start);
	}

}
