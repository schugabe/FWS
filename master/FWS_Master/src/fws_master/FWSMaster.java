package fws_master;


import java.io.File;
import java.util.concurrent.Semaphore;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;



/**
 * This is the main class of this program. It creates all the windows, loads the configuration and starts the data processing threads.
 * The path of the configuration file is dependent of the operating system. 
 * The flow of the data is:
 * The User can add and edit parameters. These parameters represent measurements of the slaves. To get the values of the slaves, it's necessary
 * to add the station to the master. For each station you can setup which parameters should be polled of the slave. This process is called binding.
 * There are two kinds of bindings. StationConfigurationBindings and StationInputBindings. 
 * <ul>
 * <li>A ConfigurationBinding is transfered from the master to the slave</li>
 * <li>A InputBinding is transfered from the slave to the master</li>
 * </ul>
 * The configuration is transfered to the slave when the user requests it. During configuration phase the user sets a polling interval for 
 * each station.
 * For each station a own thread is started. This thread pulls the measurement values over the modbus protocol. Another Thread collects the
 * data from the station threads. For each active InputBinding the collector thread creates an entry in a text file that represents the current
 * status of all sensors. 
 * The measurements are also added to a MeasurementHistory. This history is saved for each parameter on each station. This History has no
 * real references to stations or parameters. It saves all necessary data to plot diagrams. The history is separated in two parts.
 * <ol>
 * <li>Recent history: All values of the last hours. At least 24 hours are saved</li>
 * <li>Long term History: All values of each day are aggregated in one representing value (HistoryFunction determines how this value is computed).
 * This representing is saved in a history of the last 365 days</li>
 * </ol>
 * This history is serialized to the hard disk. 
 * For each binding the user can configure what diagrams should be generated. It's possible to generate a diagram of the last hours or of the 
 * long term history. It's also configurable how much data should be presented in the diagram. It's also possible to draw more one data series
 * in one diagram. 
 * 
 * This Application can only be closed over the "Exit" entry in the Menus. Otherwise it's minimized to the tray.
 * 
 *  The configuration is saved to a xml file (see PersistencePreferences, MasterContentHandler, ParameterContentHandler, StationContentHandler)
 * @author Johannes Kasberger
 *
 */
public class FWSMaster {
	private ParameterController parameter_controller;
	private StationController station_controller;
	private ViewMain view;
	private Display display;
	private Shell shell;
	private MeasurementCollector collector;
	private String configDir;
	private String outDir;
	private int generatorTime;
	private static boolean closing = false;
	private static Logger log = Logger.getLogger("fws_master");
	private MenuItem trayHideItem, trayStartItem, trayExitItem;
	private boolean autoStart;
	private TrayItem trayItem;
	private Semaphore shutdownSem;
		
	/**
	 * Generates the configPath. Normally it's a folder .fwsmaster in the home directory.
	 * @param args
	 */
	public static void main(String[] args) {
		Display.setAppName("FWS Master");
		Display display = new Display();
		
		final Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.MIN);
		// Set the Application Image
		Image appImg = new Image(display,FWSMaster.class.getResourceAsStream("/resources/logo.png") );
		shell.setImage(appImg);
		//Generate configPath
		String os = System.getProperty("os.name");
		String basePath = System.getProperty("user.home");
		String configDirPath;
		
		if (os.equals("Mac OS X")) {
			configDirPath = basePath+"/Library/Application Support/FWSMaster";
		} else {
			configDirPath= basePath+File.separator+".fwsmaster";
		}
		
		//Create configDir if not existent
		File configDir = new File(configDirPath);
		if(!configDir.isDirectory())
			configDir.mkdir();
				
		//Create the Master
		FWSMaster master = new FWSMaster(shell,display,configDirPath);
		
		shell.setSize(400,500);
		shell.open();
		shell.setText("FWS Master");
		
		if (master.isAutoStart())
			shell.setVisible(false);
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep();
		}
		display.dispose();
		master.shutdown();
		System.exit(0);
	}
	
	/**
	 * Constructor creates a MainView and creates the Tray Icon
	 * @param shell The SWT Shell
	 * @param display the SWT Display
	 * @param configDir the os dependent config path
	 */
	private FWSMaster(final Shell shell, Display display,String configDir) {
		//Generate the Log File
		try {
			//max. 2 mb log file 
			FileHandler fh = new FileHandler(configDir+"/fws_master%g.log", 2000000,3,true);
			log.addHandler(fh);
			log.setLevel(Level.INFO);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (Exception ex) {
			System.out.println("Error during creating log Handler: "+ex.getMessage());
		}
		
		shutdownSem = new Semaphore(1);
		
		this.shell = shell;
		this.display = display;
		
		//only enable disposing when exit in menu was called. Otherwise hide the shell
		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!closing) {
					event.doit = false;
					trayHideItem.setText("Show");
					shell.setVisible(false);
				}
			}
		});
		
		this.createTray();
		//Load the preferences 
		this.configDir = configDir;
		
		this.loadConfig();
		
		//this.generateParameters();
		view = new ViewMain(shell,display,this);
		
		this.collector.start();
		if (this.autoStart) {
			this.StartClicked(true);
			this.shell.setVisible(true);
			this.view.toogleStartButton();
			HideShow();
		}
		else {
			this.shell.setVisible(false);
			HideShow();
		}
	}
	
	/**
	 * Creates a Tray Icon with some useful menu items
	 */
	private void createTray() {
		Tray tray = display.getSystemTray();
		
		if(tray != null) {
			trayItem = new TrayItem(tray, SWT.NONE);
			trayItem.setToolTipText("Stop");
			TrayItemListener l = new TrayItemListener();
			Image trayImg = new Image(display,FWSMaster.class.getResourceAsStream("/resources/tray.png") );
			trayItem.setImage(trayImg);
			
			final Menu menu = new Menu(shell, SWT.POP_UP);
			trayHideItem = new MenuItem(menu, SWT.PUSH);
			trayHideItem.setText("Hide");
			trayHideItem.addSelectionListener(l);
			
			trayStartItem = new MenuItem(menu, SWT.PUSH);
			trayStartItem.setText("Start");
			trayStartItem.addSelectionListener(l);
			
			trayExitItem = new MenuItem(menu, SWT.PUSH);
			trayExitItem.setText("Exit");
			trayExitItem.addSelectionListener(l);
			
			// Show/Hide MainView on DoubleClick on TrayIcon
			trayItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					HideShow();
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					
				}
				
			});
			
			// Show menu on right mouse button
			trayItem.addListener (SWT.MenuDetect, new Listener () {
				public void handleEvent (Event event) {
					menu.setVisible(true);
				}
			});
		}
	}

	/**
	 * Loads the Configuration of the settings.xml
	 */
	private void loadConfig() {
		PersistencePreferences pref = new PersistencePreferences(configDir,"settings.xml");
		this.parameter_controller = pref.loadParameters();
		this.station_controller = pref.loadStations(this.parameter_controller);
		MasterContentHandler config = pref.loadMasterConfig();
		
		this.outDir = config.getPath();
		//if there is no outDir given create on in the configDir
		try {
			File testOutDir = new File(this.outDir);
			if (!testOutDir.isDirectory())
				throw new Exception("no dir");
		} catch (Exception ex) {
			File outDirFile = new File(configDir,"output");
			if (!outDirFile.isDirectory()) {
				outDirFile.mkdir();
			}
			this.outDir = configDir+File.separatorChar+"output";
		}
				
		
		log.config("Output Directory: "+this.outDir);
		
		this.generatorTime = config.getGeneratorTime();
		this.autoStart = config.isAutoStart();
		this.collector = new MeasurementCollector(this,this.station_controller,generatorTime,outDir,configDir);
	}
	
	/**
	 * Get the StationController
	 * @return the controller
	 */
	public StationController getStationController() {
		return this.station_controller;
	}
	
	
	
	/**
	 * Save the current settings in the xml config file
	 */
	private void shutdown() {
		this.blockShutdown();
		PersistencePreferences pref = new PersistencePreferences(configDir,"settings.xml");
		pref.saveSettings(this.parameter_controller,this.station_controller,this.outDir,this.generatorTime,this.autoStart);
	}

	/**
	 * Open the parameter window
	 */
	public void ParameterClicked() {
		Shell param_shall = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		param_shall.setLocation (pt.x, pt.y);
		param_shall.setText ("Configure Parameters");
		param_shall.setSize (600, 400);
		
		@SuppressWarnings("unused")
		ViewParameters view_parameters = new ViewParameters(param_shall,this.parameter_controller);
		param_shall.open();
		
		return;
	}

	/**
	 * Open the station window
	 */
	public void StationClicked() {
		Shell tmp_shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		tmp_shell.setLocation (pt.x, pt.y);
		tmp_shell.setText ("Configure Stations");
		tmp_shell.setSize (600, 700);
		
		@SuppressWarnings("unused")
		ViewStation view_stat = new ViewStation(tmp_shell,this.station_controller,this.parameter_controller);
		tmp_shell.open();
		
		return;
		
	}
	
	/**
	 * Open the outDir config window
	 */
	public void FolderClicked() {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		
		dialog.setFilterPath(this.outDir);
		
		String newOutDir = dialog.open();
		if (newOutDir == null || newOutDir.equals(""))
			return;
		this.outDir = newOutDir;
		this.collector.setOutDir(this.outDir);
	}
	
	/**
	 * Start or Stopp all stations
	 * @param start if true start the stations, if false pause them
	 */
	public void StartClicked(boolean start) {
		this.station_controller.startStations(start);
		this.view.enableMenu(!start);
		
		if (start) {
			trayStartItem.setText("Stop");
			trayExitItem.setEnabled(false);
			trayItem.setToolTipText("Running");
		} else {
			trayStartItem.setText("Start");
			trayExitItem.setEnabled(true);
			trayItem.setToolTipText("Stop");
		}
	}

	/**
	 * Show the add station view
	 */
	public void viewAddStationClicked() {
		Shell tmp_shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		tmp_shell.setLocation (pt.x, pt.y);
		tmp_shell.setText ("Add new Station");
		tmp_shell.setSize (300, 200);
		
		new ViewNew(tmp_shell,this);
		tmp_shell.open();
	}
	/**
	 * Called when User selects add station from the fileMenu
	 */
	public boolean addStationClicked(String name, String ip) {
		try {
			Station newStation  = new Station(name, this.station_controller, ip, 60);
			this.station_controller.addStation(newStation);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}
	
	/**
	 * Called when User selects save Config from the fileMenu
	 */
	public void saveConfigClicked() {
		PersistencePreferences pref = new PersistencePreferences(configDir,"settings.xml");
		pref.saveSettings(this.parameter_controller,this.station_controller,this.outDir,this.generatorTime,this.autoStart);
	}
	
	/**
	 * Called when User selects reload Config from the fileMenu
	 */
	public void reloadConfigClicked() {
		this.loadConfig();
	}
	
	/**
	 * Called when the user selects the settings entry from the configMenu. Shows the Settings view.
	 */
	public void settingsClicked() {
		Shell tmp_shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point pt = display.getCursorLocation();
		tmp_shell.setLocation (pt.x, pt.y);
		tmp_shell.setText ("Settings");
		tmp_shell.setSize (200, 300);
		
		new ViewSettings(tmp_shell,this);
		tmp_shell.open();
		
		return;
	}

	/**
	 * Enables the disposing of the display and disposes it. Only allow closing when the Stations are stopped.
	 */
	public void exitClicked() {
		if (!this.station_controller.isRunning()) {
			closing = true;
			display.dispose();
		}
	}
	
	/**
	 * Toggle the Main View
	 */
	private void HideShow() {
		if (shell.isVisible()) {
			shell.setVisible(false);
			trayHideItem.setText("Show");
		}
		else {
			shell.setVisible(true);
			shell.forceActive();
			shell.forceFocus();
			trayHideItem.setText("Hide");
		}
	}

	/**
	 * Creates a View for the currently saved data
	 */
	public void viewDataClicked() {
		Shell tmp_shell = new Shell(this.display, SWT.RESIZE | SWT.CLOSE | SWT.TITLE);
		Point pt = display.getCursorLocation();
		tmp_shell.setLocation (pt.x, pt.y);
		tmp_shell.setText ("Collected Data");
		tmp_shell.setSize (700, 800);
		
		new ViewData(this,tmp_shell);
		tmp_shell.open();
		
		return;
	}
	
	/**
	 * Listener for Tray Events
	 * @author Johannes Kasberger
	 *
	 */
	class TrayItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((MenuItem) event.widget)==trayHideItem) {
				HideShow();
				
			}
			else if (((MenuItem) event.widget)==trayStartItem) {
				if (station_controller.isRunning()) {
					StartClicked(false);
				}
				else {
					StartClicked(true);
				}
				view.toogleStartButton();
			}
			else if (((MenuItem) event.widget)==trayExitItem) {
				exitClicked();
			}
		}
	}

	/**
	 * Get the Generator Time
	 * @return the time interval in seconds in that the output is generated
	 */
	public int getGeneratorTime() {
		return generatorTime;
	}

	/**
	 * The Generatortime is the time interval after that the outputs are generated
	 * @param generatorTime
	 */
	public void setGeneratorTime(int generatorTime) {
		this.generatorTime = generatorTime;
	}

	/**
	 * @param autoStart the autoStart to set
	 */
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	/**
	 * @return the autoStart
	 */
	public boolean isAutoStart() {
		return autoStart;
	}
	
	
	/**
	 * Get the current History Controller
	 * @return the controller
	 */
	public MeasurementHistoryController getHistoryController() {
		return this.collector.getMeasurementHistoryController();
	}
	
	public synchronized boolean blockShutdown() {
		try {
			this.shutdownSem.acquire();
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public synchronized void releaseShutdown() {
		this.shutdownSem.release();
	}
}
