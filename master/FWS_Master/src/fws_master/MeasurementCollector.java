package fws_master;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;


public class MeasurementCollector extends Thread {
	private StationController controller;
	private int intervall;
	private String outDir;
	private MeasurementHistoryController historyController;
	
	private int testDaychange = 0;
	public MeasurementCollector(StationController controller, int intervall,String outDir) {
		this.intervall = intervall;
		this.controller = controller;
		this.outDir = outDir;
		historyController = new MeasurementHistoryController();
	}
	
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000*this.intervall);
				testDaychange++;
				Vector<String> result  = new Vector<String>(this.controller.getStations().size());

				for(Station s:this.controller.getStations()) {
					Vector<Measurement> measurements = s.getLastMeasurements();
					if(measurements.size() == 0)
						continue;
					HashMap<String,Vector<Measurement>> params = new HashMap<String,Vector<Measurement>>();
					
					for(Measurement m:measurements) {
						Vector<Measurement> tmp = params.get(m.getParameter().getName());
						if (tmp == null) {
							tmp = new Vector<Measurement>(measurements.size()/s.getInputParamsCount()+1);
							params.put(m.getParameter().getName(), tmp);
						} 
						tmp.add(m);
						
					}
					
					result.add(s.getStationName()+"\n"+this.buildOutput(params));
					
 				}
				if(result.size()>0)
					this.WriteOutput("result.txt", result);
				
				this.buildPlots();
				
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	private String buildOutput(HashMap<String,Vector<Measurement>> data) {
		String res = "";
	
		for(Entry<String, Vector<Measurement>> e:data.entrySet()) {
			Vector<Measurement> ms = e.getValue();
			
			Measurement tmp = ms.firstElement();
			
			this.historyController.addData(tmp.getStation().getStationName(), tmp.getParameter().getName(), ms);
			if (testDaychange%10 == 3) {
				this.historyController.changeDay(tmp.getStation().getStationName(), tmp.getParameter().getName(), tmp.getParameter().getHistory_function(), new Date());
			}
			double avg = 0;
			
			for(Measurement m : ms) {
				avg += m.getConvValue();
			}
			
			avg /= ms.size();
			
			double sd = 0;
			
			for(Measurement m: ms) {
				sd = sd + ((m.getConvValue()-avg)*(m.getConvValue()-avg));
			}
			
			sd = Math.sqrt(sd/(ms.size()-1));
			res+=ms.get(0).getParameter().getName()+":"+avg+";"+sd+";\n";
		}
		return res;
	}
	
	private MeasurementHistory buildPlotData(PlotConfig cfg,String station, String parameter) {
		if (cfg.getTimeBase()=='d')
			return this.historyController.getLastHistoryDays(station, parameter, cfg.getCount());
		else if (cfg.getTimeBase() == 'h')
			return this.historyController.getLastHistory(station, parameter, cfg.getCount());
		
		return null;
	}
	
	private void buildPlots() {
		PlotController plotController = new PlotController(this.outDir);
		PlotBase timePlot = plotController.getPlot("Zeitverlauf");
		
		HashMap<Integer,Vector<MeasurementHistory>> plots = new HashMap<Integer,Vector<MeasurementHistory>>();
		
		
		for (Station s:this.controller.getStations()) {
			for (Binding b:s.getParameters()) {
				if (b instanceof StationInputBinding) {
					StationInputBinding ib = (StationInputBinding)b;
					
					if(!ib.isActive())
						continue;
					int plotCount = 0;
					
					for (PlotConfig cfg:ib.getPlots()) {
						Vector<MeasurementHistory> tmpData;
						if (cfg.getId() == -1) {
							tmpData = new Vector<MeasurementHistory>(1);
						} else {
							tmpData = plots.get(cfg.getId());
							if (tmpData == null) {
								tmpData = new Vector<MeasurementHistory>();
								plots.put(cfg.getId(), tmpData);
							}
						}
						tmpData.add(this.buildPlotData(cfg, s.getStationName(), b.getParameter().getName()));
						
						if (cfg.getId() == -1) {
							timePlot.createPlot(tmpData,""+plotCount);
							plotCount++;
						}
						
					}
				}
			}
		}
		
		for(Entry<Integer, Vector<MeasurementHistory>> tmpPlots:plots.entrySet()) {
			timePlot.createPlot(tmpPlots.getValue(),""+tmpPlots.getKey());
		}
		
	}

	private void WriteOutput(String fileName,Vector<String> result) {
		try {
			File file = new File(this.outDir,fileName);
			
			FileWriter fw = new FileWriter(file,false);
			
			String DATE_FORMAT = "HH:mm:ss dd.MM.yyyy";
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		    
		    fw.write(sdf.format(new Date())+"\n");

			
			for(String s:result) {
				fw.write(s+"\n");
			}
			fw.close();
		} catch(Exception ex) {
			Logger.getLogger("IOLogger").info("Fehler bei Ausgabe"+ex.getLocalizedMessage());
		}
	}
	
	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}
}
