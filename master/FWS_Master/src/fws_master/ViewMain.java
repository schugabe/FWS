package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class ViewMain {
	private Shell shell;
	@SuppressWarnings("unused")
	private Display display;
	private FWS_Master master;
	
	private Menu menuBar, fileMenu;
	private MenuItem fileMenuHeader;
	private MenuItem fileExitItem, fileParamsItem;
	
	public ViewMain(Shell shell, Display display,FWS_Master master) {
		this.shell = shell;
		this.display = display;
		this.master = master;
		this.shell.setText("FWS Master");
		InitMenuBar();
		

	}
	
	private void InitMenuBar() {
		menuBar = new Menu(shell, SWT.BAR);
	    fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    fileMenuHeader.setText("Datei");

	    fileMenu = new Menu(shell, SWT.DROP_DOWN);
	    fileMenuHeader.setMenu(fileMenu);

	    fileParamsItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileParamsItem.setText("Parameter bearbeiten");
	    
	    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileExitItem.setText("Beenden");

	    fileExitItem.addSelectionListener(new MenuItemListener());
	    fileParamsItem.addSelectionListener(new MenuItemListener());
	    
	    shell.setMenuBar(menuBar);
	}
	
	class MenuItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((MenuItem) event.widget)==fileExitItem) {
				shell.close();
			}
			else if (((MenuItem) event.widget) == fileParamsItem) {
				master.ParameterClicked();
			}
			
		}
	}
}

