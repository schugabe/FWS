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
	
	private Menu menuBar, fileMenu;
	private MenuItem fileMenuHeader;
	private MenuItem fileExitItem, fileParamsItem, fileStationsItem,fileOutDirItem;
	
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
		rowLayout.spacing = 0;
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
			statusLabel.setText("Status:");
			
			Label statusLabel2 = new Label(c, SWT.NONE);
			statusLabel2.setText("stopp                                              ");
			s.setStatusLabel(statusLabel2);
		}
		Button startButton = new Button(c_all, SWT.PUSH);
		startButton.setText(" Starten ");
		startButton.addSelectionListener(new ButtonListener());
	}
	
	private void InitMenuBar() {
		MenuItemListener l = new MenuItemListener();
		menuBar = new Menu(shell, SWT.BAR);
	    fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	    fileMenuHeader.setText("Datei");

	    fileMenu = new Menu(shell, SWT.DROP_DOWN);
	    fileMenuHeader.setMenu(fileMenu);
	    
	    fileOutDirItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileOutDirItem.setText("Ausgabe Ordner wählen");

	    fileStationsItem= new MenuItem(fileMenu, SWT.PUSH);
	    fileStationsItem.setText("Stationen bearbeiten");
	    
	    fileParamsItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileParamsItem.setText("Parameter bearbeiten");
	    
	    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
	    fileExitItem.setText("Beenden");

	    fileExitItem.addSelectionListener(l);
	    fileParamsItem.addSelectionListener(l);
	    fileStationsItem.addSelectionListener(l);
	    fileOutDirItem.addSelectionListener(l);
	    
	    shell.setMenuBar(menuBar);
	}
	
	
	class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			Button b = (Button)event.widget;
			if (b.getText().equals(" Starten ")) {
				master.StartClicked(true);
				fileParamsItem.setEnabled(false);
				fileStationsItem.setEnabled(false);
				b.setText("Stoppen");
			}
			else if (b.getText().equals("Stoppen")) {
				master.StartClicked(false);
				fileParamsItem.setEnabled(true);
				fileStationsItem.setEnabled(true);
				b.setText(" Starten ");
			}
		}
	}
	class MenuItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((MenuItem) event.widget)==fileExitItem) {
				shell.close();
			}
			else if (((MenuItem) event.widget) == fileParamsItem) {
				master.ParameterClicked();
			}
			else if (((MenuItem) event.widget) == fileStationsItem) {
				master.StationClicked();
			}
			else if (((MenuItem) event.widget) == fileOutDirItem) {
				master.FolderClicked();
			}
			
			
		}
	}
}

