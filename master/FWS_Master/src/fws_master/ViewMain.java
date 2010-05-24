package fws_master;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ViewMain {
	private Shell shell;
	@SuppressWarnings("unused")
	private Display display;
	private ScrolledComposite scroll;
	private FWSMaster master;
	
	private Menu menuBar, fileMenu,configMenu;
	private MenuItem fileMenuHeader,configMenuHeader;
	private MenuItem fileNewStationItem, fileSaveConfig, fileReloadConfig, fileExitItem, configParamsItem, configStationsItem, configOutDirItem, configSettingsItem;
	
	public ViewMain(Shell shell, Display display,FWSMaster master) {
		this.shell = shell;
		this.display = display;
		this.master = master;
		this.shell.setText("FWS Master");
		this.shell.setLayout(new FillLayout());
		InitMenuBar();
		scroll = new ScrolledComposite(shell, SWT.NONE  | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(400);
		scroll.setMinHeight(400);
		scroll.setLayout(new FillLayout());
		
		this.InitStationView();

	}
	
	public void reloadStationView() {
		this.InitStationView();
	}
	
	private void InitStationView() {
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = false;
		rowLayout.type = SWT.VERTICAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 0;
		
		Composite c_all = new Composite(scroll,SWT.NONE);
		c_all.setLayout(rowLayout);
		scroll.setContent(c_all);
		
		this.BuildStationView(c_all);
	}
	private void BuildStationView(Composite c_all) {
		RowLayout rowLayout = new RowLayout();
		
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 3;
		rowLayout.justify = false;
		rowLayout.pack = true;
		
		for(Station s:this.master.getStationController().getStations()) {
			Composite c = new Composite(c_all,SWT.NONE);
			
			c.setLayout(rowLayout);
			Label nameLabel = new Label(c, SWT.NONE);
			nameLabel.setText(s.getStationName());
			Label ipLabel = new Label(c, SWT.NONE);
			ipLabel.setText(s.getIpAddress());
			Label statusLabel = new Label(c, SWT.NONE);
			statusLabel.setText("State:");
			
			Label statusLabel2 = new Label(c, SWT.NONE);
			statusLabel2.setText("stop                                              ");
			s.setStatusLabel(statusLabel2);
		}
		Button startButton = new Button(c_all, SWT.PUSH);
		startButton.setText(" Start ");
		startButton.addSelectionListener(new ButtonListener());
	}
	
	private void InitMenuBar() {
		MenuItemListener l = new MenuItemListener();
		menuBar = new Menu(shell, SWT.BAR);
	    fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    fileMenuHeader.setText("File");

	    fileMenu = new Menu(shell, SWT.DROP_DOWN);
	    fileMenuHeader.setMenu(fileMenu);
	    
	    fileNewStationItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileNewStationItem.setText("Add Station");
	    
	    new MenuItem(fileMenu, SWT.SEPARATOR);
	     
	    fileSaveConfig = new MenuItem(fileMenu, SWT.PUSH);
	    fileSaveConfig.setText("Save Configuration");
	    
	    fileReloadConfig = new MenuItem(fileMenu, SWT.PUSH);
	    fileReloadConfig.setText("Reload Configuration");
	    
	    new MenuItem(fileMenu, SWT.SEPARATOR);
	    	    
	    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileExitItem.setText("Exit");

	    fileExitItem.addSelectionListener(l);
	    fileNewStationItem.addSelectionListener(l);
	   
	    fileSaveConfig.addSelectionListener(l);
	    fileReloadConfig.addSelectionListener(l);
	    
	    configMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    configMenuHeader.setText("Configure");
	    
	    configMenu = new Menu(shell, SWT.DROP_DOWN);
	    configMenuHeader.setMenu(configMenu);
	    
	    configStationsItem= new MenuItem(configMenu, SWT.PUSH);
	    configStationsItem.setText("Edit Stations");
	    
	    configParamsItem = new MenuItem(configMenu, SWT.PUSH);
	    configParamsItem.setText("Edit Parameters");
	    
	    configOutDirItem = new MenuItem(configMenu, SWT.PUSH);
	    configOutDirItem.setText("Set Output Folder");
   
	    configSettingsItem = new MenuItem(configMenu, SWT.PUSH);
	    configSettingsItem.setText("Settings");
	    
	    configParamsItem.addSelectionListener(l);
	    configStationsItem.addSelectionListener(l);
	    configOutDirItem.addSelectionListener(l);
	    configSettingsItem.addSelectionListener(l);
	    shell.setMenuBar(menuBar);
	}
	
	public void enableMenu(boolean enable) {
		configMenu.setEnabled(enable);
		fileMenu.setEnabled(enable);
		
	}
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			Button b = (Button)event.widget;
			if (b.getText().equals(" Start ")) {
				master.StartClicked(true);
				b.setText(" Stop ");
			}
			else if (b.getText().equals(" Stop ")) {
				master.StartClicked(false);
				b.setText(" Start ");
			}
		}
	}
	class MenuItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((MenuItem) event.widget)==fileExitItem) {
				master.exitClicked();
			}
			else if (((MenuItem) event.widget) == configParamsItem) {
				master.ParameterClicked();
			}
			else if (((MenuItem) event.widget) == configStationsItem) {
				master.StationClicked();
			}
			else if (((MenuItem) event.widget) == configOutDirItem) {
				master.FolderClicked();
			}
			else if (((MenuItem) event.widget) == fileSaveConfig) {
				master.saveConfigClicked();
			}
			else if (((MenuItem) event.widget) == fileReloadConfig) {
				master.reloadConfigClicked();
			}
			else if (((MenuItem) event.widget) == configSettingsItem) {
				master.settingsClicked();
			}
			else if (((MenuItem) event.widget) == fileNewStationItem) {
				master.viewAddStationClicked();
			}
		}
	}
}

