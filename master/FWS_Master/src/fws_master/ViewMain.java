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
	
	private Menu menuBar, fileMenu,configMenu, helpMenu;
	private MenuItem fileMenuHeader,configMenuHeader,helpMenuHeader;
	private MenuItem fileNewSlaveItem, fileSaveConfig, fileReloadConfig,fileViewData, fileExitItem, 
	configParamsItem, configSlavesItem, configOutDirItem, configSettingsItem, helpAboutItem, helpHomepageItem;
	private Button startButton;
	
	public ViewMain(Shell shell, Display display,FWSMaster master) {
		this.shell = shell;
		this.display = display;
		this.master = master;
		this.shell.setText("FWS Master");
		this.shell.setLayout(new FillLayout());
		InitMenuBar();
		
		initScroll();
		InitSlaveView();

	}
	
	public void reloadSlaveView() {
		
	}
	private void initScroll() {
		scroll = new ScrolledComposite(shell, SWT.NONE  | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinWidth(400);
		scroll.setMinHeight(400);
		scroll.setLayout(new FillLayout());
		
	}
	private void InitSlaveView() {
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
		
		this.BuildSlaveView(c_all);
	}
	private void BuildSlaveView(Composite c_all) {
		RowLayout rowLayout = new RowLayout();
		
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 3;
		rowLayout.justify = false;
		rowLayout.pack = true;
		
		for(Slave s:this.master.getSlaveController().getSlaves()) {
			Composite c = new Composite(c_all,SWT.NONE);
			
			c.setLayout(rowLayout);
			Label nameLabel = new Label(c, SWT.NONE);
			nameLabel.setText(s.getSlaveName());
			Label ipLabel = new Label(c, SWT.NONE);
			ipLabel.setText(s.getIpAddress());
			Label statusLabel = new Label(c, SWT.NONE);
			statusLabel.setText("State:");
			
			Label statusLabel2 = new Label(c, SWT.NONE);
			statusLabel2.setText("stop                                              ");
			s.setStatusLabel(statusLabel2);
		}
		startButton = new Button(c_all, SWT.PUSH);
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
	    
	    fileNewSlaveItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileNewSlaveItem.setText("Add Slave");
	    
	    new MenuItem(fileMenu, SWT.SEPARATOR);
	     
	    fileSaveConfig = new MenuItem(fileMenu, SWT.PUSH);
	    fileSaveConfig.setText("Save Configuration");
	    
	    fileReloadConfig = new MenuItem(fileMenu, SWT.PUSH);
	    fileReloadConfig.setText("Reload Configuration");
	    
	    new MenuItem(fileMenu, SWT.SEPARATOR);
	    
	    fileViewData = new MenuItem(fileMenu, SWT.PUSH);
	    fileViewData.setText("View Data");
	    
	    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileExitItem.setText("Exit");

	    fileViewData.addSelectionListener(l);
	    fileExitItem.addSelectionListener(l);
	    fileNewSlaveItem.addSelectionListener(l);
	   
	    fileSaveConfig.addSelectionListener(l);
	    fileReloadConfig.addSelectionListener(l);
	    
	    configMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    configMenuHeader.setText("Configure");
	    
	    configMenu = new Menu(shell, SWT.DROP_DOWN);
	    configMenuHeader.setMenu(configMenu);
	    
	    configSlavesItem= new MenuItem(configMenu, SWT.PUSH);
	    configSlavesItem.setText("Edit Slaves");
	    
	    configParamsItem = new MenuItem(configMenu, SWT.PUSH);
	    configParamsItem.setText("Edit Parameters");
	    
	    configOutDirItem = new MenuItem(configMenu, SWT.PUSH);
	    configOutDirItem.setText("Set Output Folder");
   
	    configSettingsItem = new MenuItem(configMenu, SWT.PUSH);
	    configSettingsItem.setText("Settings");
	    
	    helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    helpMenuHeader.setText("Help");
	    
	    helpMenu = new Menu(shell, SWT.DROP_DOWN);
	    helpMenuHeader.setMenu(helpMenu);
	    
	    helpHomepageItem = new MenuItem(helpMenu, SWT.PUSH);
	    helpHomepageItem.setText("Go to project homepage");
	    
	    helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
	    helpAboutItem.setText("About");
	    
	  
	    
	    configParamsItem.addSelectionListener(l);
	    configSlavesItem.addSelectionListener(l);
	    configOutDirItem.addSelectionListener(l);
	    configSettingsItem.addSelectionListener(l);
	    helpHomepageItem.addSelectionListener(l);
	    helpAboutItem.addSelectionListener(l);
	    shell.setMenuBar(menuBar);
	}
	
	public void enableMenu(boolean enable) {
		
		fileNewSlaveItem.setEnabled(enable);
		fileSaveConfig.setEnabled(enable);
		fileReloadConfig.setEnabled(enable);
		
		fileExitItem.setEnabled(enable);
		configSlavesItem.setEnabled(enable);
		configParamsItem.setEnabled(enable);
		configOutDirItem.setEnabled(enable);
		configSettingsItem.setEnabled(enable);
	}
	
	public void toogleStartButton() {
		if (this.startButton.getText().equals(" Stop ")) {
			this.startButton.setText(" Start ");
		} else {
			this.startButton.setText(" Stop ");
		}
	}
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			Button b = (Button)event.widget;
			if (b.getText().equals(" Start ")) {
				master.StartClicked(true);
				toogleStartButton();
			}
			else if (b.getText().equals(" Stop ")) {
				master.StartClicked(false);
				toogleStartButton();
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
			else if (((MenuItem) event.widget) == configSlavesItem) {
				master.SlaveClicked();
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
			else if (((MenuItem) event.widget) == fileNewSlaveItem) {
				master.viewAddSlaveClicked();
			}
			else if (((MenuItem) event.widget) == fileViewData) {
				master.viewDataClicked();
			}
			else if (((MenuItem) event.widget) == helpAboutItem) {
				master.aboutClicked();
			}
			else if (((MenuItem) event.widget) == helpHomepageItem) {
				master.homepageClicked();
			}
			
		}
	}
	
	
}

