package fws_master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * A thread that pulls the data of the stations. This data is collected and a text file is generated with the current values in it. This happens in a certain interval.
 * @author Johannes Kasberger
 *
 */
public class MeasurementCollector extends Thread {
	private StationController controller;
	private int interval;
	private String outDir;
	private MeasurementHistoryController historyController;
	private static Logger log = Logger.getLogger("fws_master.collector");
	private boolean newDay = false;
	private static String eol = System.getProperty( "line.separator" );
	private String historyFile; 
	
	/**
	 * Constructor
	 * @param controller list of stations
	 * @param interval
	 * @param outDir Directory where plots and summary text file should be generated
	 */
	public MeasurementCollector(StationController controller, int interval,String outDir, String historyDir) {
		this.interval = 1000*interval;
		this.controller = controller;
		this.outDir = outDir;
		this.historyFile =  historyDir+File.separator+"history.ser";
		
		historyController = loadHistory(); 
	}
	


	public void run() {
		long sleepTime = this.interval;
		long runTime;
		while (true) {
			try {
				Thread.sleep(sleepTime);
				runTime = System.currentTimeMillis();
				
				// Start collecting Data
				log.fine("Getting Data from Stations");
				
				
				Vector<String> result = getData();
				if(result.size()>0)
					this.WriteOutput("result.txt", result);
				
				log.fine("Generating Plots");
				this.buildPlots();
				
				saveHistory();
				newDay = false;
				log.fine("Collector done");
				
				//calculate the runtime of the generation. Wait intervall-runtime to stick to the intervall.
				long tmpTime = System.currentTimeMillis();
				tmpTime = tmpTime-runTime;
				sleepTime = this.interval-tmpTime;
				if (sleepTime <= 0)
					sleepTime = 1;
			} catch (Exception ex) {
				log.severe("Exception collecting Data: "+ex.getMessage()+ "\n"+ex.getStackTrace());
			}
		}
	}

	private void saveHistory() {
		FileOutputStream fs;
		ObjectOutputStream os;
		
		try {
			fs = new FileOutputStream(historyFile);
		} catch (FileNotFoundException e) {
			log.severe("Creating History on Hard Disk failed: "+e.getLocalizedMessage());
			return;
		}
		try {
			os = new ObjectOutputStream(fs);
		} catch (IOException e) {
			log.severe("Creating ObjectOutputStream failed: "+e.getLocalizedMessage());
			return;
		}
		try {
			os.writeObject(this.historyController);
		} catch (IOException e) {
			log.severe("Writing History to Hard Disk failed: "+e.getStackTrace());
		}
	}
	
	private MeasurementHistoryController loadHistory() {
		
		FileInputStream fs;
		ObjectInputStream is;
		MeasurementHistoryController controller;
		
		try {
			fs = new FileInputStream(historyFile);
		} catch (FileNotFoundException e) {
			log.severe("Loading History: History not found : "+e.getLocalizedMessage());
			return new MeasurementHistoryController();
		}
		
		try {
			is = new ObjectInputStream(fs);
		} catch (IOException e) {
			log.severe("Loading History: ObjectInputStream not created: "+e.getLocalizedMessage());
			return new MeasurementHistoryController();
		}

		try {
			controller = (MeasurementHistoryController)is.readObject();
		} catch (IOException e) {
			controller = new MeasurementHistoryController();
			log.severe("Loading History: I/O Exception: "+e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			controller = new MeasurementHistoryController();
			log.severe("Loading History: Error during loading History: "+e.getLocalizedMessage());
		}
		
		return controller;
	}

	/**
	 * Collects the Data from the Stations, add it to the MeasurementHistory and extract the current value of all active InputParameters. 
	 * The station manages all Measurements in one collection. So the Measurements must be sorted before the average of the last values can be calculated.
	 * The sorting happens with a HashMap. The different measurements are added to the collection. After all Measurements are sorted the output can be generated.
	 * @return Result for the text file
	 */
	private Vector<String> getData() {
		Vector<String> result  = new Vector<String>(this.controller.getStations().size());

		for(Station s:this.controller.getStations()) {
			Vector<Measurement> measurements = s.getLastMeasurements();
			if (measurements==null)
				continue;
			
			log.fine("Station "+s.getStationName()+" has "+measurements.size()+" new Measurements");
			
			if(measurements.size() == 0)
				continue;
			
			//Sort Measurements
			HashMap<String,Vector<Measurement>> params = new HashMap<String,Vector<Measurement>>();
			
			for(Measurement m:measurements) {
				Vector<Measurement> tmp = params.get(m.getParameter().getName());
				if (tmp == null) {
					tmp = new Vector<Measurement>(measurements.size()/s.getInputParamsCount()+1);
					params.put(m.getParameter().getName(), tmp);
				} 
				tmp.add(m);
				
			}
			log.fine("Generating Output");
			result.add(s.getStationName()+"\n"+this.buildOutput(params));
		}
		return result;
	}

	/**
	 * Builds the Output for the current status message. Adds the measurements to the History.
	 * @param data Measurements of a slave, all of the same sensor (e.g. temperature of station 1)
	 * @return Parameter Name:Average Value;Standard deviation of value;
	 */
	private String buildOutput(HashMap<String,Vector<Measurement>> data) {
		String res = "";
	
		for(Entry<String, Vector<Measurement>> e:data.entrySet()) {
			Vector<Measurement> ms = e.getValue();
			
			Measurement tmp = ms.firstElement();
			if (ms.size() == 0)
				continue;
			newDay = this.historyController.addData(tmp.getStation().getStationName(), tmp.getParameter().getName(), ms);
			
			double avg = 0;
			
			for(Measurement m : ms) {
				avg += m.getConvValue();
			}
			
			avg /= ms.size();
			
			double sd = 0;
			
			for(Measurement m: ms) {
				sd = sd + ((m.getConvValue()-avg)*(m.getConvValue()-avg));
			}
			
			int div = ms.size()-1;
			if (ms.size() == 1)
				div = 1;
			
			sd = Math.sqrt(sd/(div));
			res+=ms.firstElement().getParameter().getName()+"["+Units.getString(ms.firstElement().getParameter().getUnit())+"]"+":"+avg+";"+sd+";\n";
		}
		return res;
	}
	
	/**
	 * Gets the Measurement from the History
	 * @param cfg Plot Configuration
	 * @param station Name of the Station
	 * @param parameter Name of the Parameter
	 * @return History of values
	 */
	private MeasurementHistory buildPlotData(PlotConfig cfg,String station, String parameter) {
		if (cfg.getTimeBase()=='d')
			return this.historyController.getLastHistoryDays(station, parameter, cfg.getCount());
		else if (cfg.getTimeBase() == 'h')
			return this.historyController.getLastHistory(station, parameter, cfg.getCount());
		
		return null;
	}
	
	/**
	 * Generates the plots. Each InputBinding can require one or more plots. These are saved in a collection of PlotConfig values. For each PlotConfig one Diagram is generated.
	 */
	private void buildPlots() {
		PlotController plotController = new PlotController(this.outDir);
		PlotBase timePlot = plotController.getPlot("Zeitverlauf");
		
		HashMap<Integer,Vector<MeasurementHistory>> plots = new HashMap<Integer,Vector<MeasurementHistory>>();
		
		
		for (Station s:this.controller.getStations()) {
			for (Binding b:s.getBindings()) {
				if (b instanceof StationInputBinding) {
					StationInputBinding ib = (StationInputBinding)b;
					
					if(!ib.isActive())
						continue;
					int plotCount = 0;
					
					for (PlotConfig cfg:ib.getPlots()) {
						// Daily Diagrams are only generated when Day has changed 
						if (cfg.getTimeBase() == 'd' && newDay==false)
							continue;
						
						//If only one kind of Data should be in the Diagram it will be generated
						Vector<MeasurementHistory> tmpData;
						if (cfg.getId() == -1) {
							tmpData = new Vector<MeasurementHistory>(1);
						} else {
							//If more than one kind of data should be in the diagram the drawing process starts after processing the plotconfig of all stations
							tmpData = plots.get(cfg.getId());
							if (tmpData == null) {
								tmpData = new Vector<MeasurementHistory>();
								plots.put(cfg.getId(), tmpData);
							}
						}
						tmpData.add(this.buildPlotData(cfg, s.getStationName(), b.getParameter().getName()));
						
						// Plot the Data if everything is loaded
						if (cfg.getId() == -1) {
							timePlot.createPlot(tmpData,""+plotCount);
							plotCount++;
						}
						
					}
				}
			}
		}
		
		//Plot the Diagrams with more than one data series in it
		for(Entry<Integer, Vector<MeasurementHistory>> tmpPlots:plots.entrySet()) {
			timePlot.createPlot(tmpPlots.getValue(),""+tmpPlots.getKey());
		}
		
	}

	/**
	 * Generate the output file
	 * @param fileName
	 * @param result
	 */
	private void WriteOutput(String fileName,Vector<String> result) {
		

		try {
			File file = new File(this.outDir,fileName);
			
			FileWriter fw = new FileWriter(file,false);
			
			String DATE_FORMAT = "HH:mm:ss dd.MM.yyyy";
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		    
		    fw.write(sdf.format(new Date())+eol);
		    			
			for(String s:result) {
				fw.write(s+"\n");
			}
			fw.close();
		} catch(Exception ex) {
			log.severe(ex.getMessage());
		}
	}
	
	/**
	 * Get the current outPutDir
	 * @return output directory
	 */
	public String getOutDir() {
		return outDir;
	}

	/**
	 * Set the Output directory 
	 * @param outDir
	 */
	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}



	public MeasurementHistoryController getMeasurementHistoryController() {
		return this.historyController;
	}
}
