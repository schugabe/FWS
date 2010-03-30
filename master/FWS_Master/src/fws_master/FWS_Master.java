package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

public class FWS_Master {

	/**
	 * @param args
	 */
	private Parameter_Controller parameter_controller;
	@SuppressWarnings("unused")
	private ViewMain view;
	private Display display;
	
	private void generateParameters() {
		Config_Parameter c = new Config_Parameter("IP Addresse");
		this.parameter_controller.addParameter(c);
		
		Input_Parameter i = new Input_Parameter("Temperatur",Units.TEMPERATURE,Output_Formats.NK1,History_Functions.MAX);
		this.parameter_controller.addParameter(i);
	}
	
	private FWS_Master(Shell shell, Display display) {
		this.parameter_controller = new Parameter_Controller();
		view = new ViewMain(shell,display,this);
		this.generateParameters();
		this.display = display;
		
	}
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		
		@SuppressWarnings("unused")
		FWS_Master master = new FWS_Master(shell,display);
		
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		display.dispose ();
	}
	
	public void ParameterClicked() {
		Shell param_shall = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation ();
		param_shall.setLocation (pt.x, pt.y);
		param_shall.setText ("Parameter Verwalten");
		param_shall.setSize (600, 400);
		
		@SuppressWarnings("unused")
		ViewParameters view_parameters = new ViewParameters(param_shall,this.parameter_controller);
		param_shall.open();
		
		return;
	}

}
