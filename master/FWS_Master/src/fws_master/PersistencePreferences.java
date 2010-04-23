package fws_master;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class PersistencePreferences {
	private String path;
	private String filename;
	private Logger log;
	
	
	public PersistencePreferences(String path,String filename) {
		this.path = path;
		this.filename = filename;
		this.log = Logger.getLogger("FWS.xml");
	}
	
	public StationController loadStations(ParameterController params) {
		StationController stations = new StationController();
		StationContentHandler h = new StationContentHandler(stations,params);
		this.startParsing(h);
		return stations;
	}
	
	public ParameterController loadParameters() {
		ParameterController params = new ParameterController();
		ParameterContentHandler h = new ParameterContentHandler(params);
		this.startParsing(h);
		return params ;
	}
	
	public MasterContentHandler loadMasterConfig() {
		MasterContentHandler h = new MasterContentHandler();
		this.startParsing(h);
		return h;
	}
	
	public void saveSettings(ParameterController params,StationController stations,String outDir, int generatorTime) {
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.info(e.getLocalizedMessage());
			return;
		}
		Document document = documentBuilder.newDocument();
		
		Element rootElement = document.createElement("fws_config");
		document.appendChild(rootElement);
		
		Element masterEl = document.createElement("path");
		masterEl.appendChild(document.createTextNode(outDir));
		rootElement.appendChild(masterEl);
		
		masterEl = document.createElement("generatortime");
		masterEl.appendChild(document.createTextNode(""+generatorTime));
		rootElement.appendChild(masterEl);
		
		for (Parameter p:params.getParameters()) {
			Element tmp = document.createElement("parameter");
			
			Element nameEl = document.createElement("name");
			nameEl.appendChild(document.createTextNode(p.getName()));
			tmp.appendChild(nameEl);
			
			if (p instanceof ConfigParameter) {
				tmp.setAttribute("typ", "config");
			}
			else if (p instanceof InputParameter) {
				InputParameter ip = (InputParameter)p;
				tmp.setAttribute("typ", "input");
				
				Element tmpEl = document.createElement("unit");
				tmpEl.appendChild(document.createTextNode(Units.getString(ip.getUnit())));
				tmp.appendChild(tmpEl);
				
				tmpEl = document.createElement("format");
				tmpEl.appendChild(document.createTextNode(OutputFormats.getString(ip.getFormat())));
				tmp.appendChild(tmpEl);
				
				tmpEl = document.createElement("history");
				tmpEl.appendChild(document.createTextNode(""+ip.getHistory_function()));
				tmp.appendChild(tmpEl);
			}
			
			rootElement.appendChild(tmp);
		}
		for (Station s:stations.getStations()) {
			Element station = document.createElement("station");
			station.setAttribute("name", s.getStationName());
			station.setAttribute("ip", s.getIpAddress());
			station.setAttribute("intervall", ""+s.getPollingIntervall());
			
			for(Binding b:s.getParameters()) {
				Element bel = document.createElement("binding");
				if (b instanceof StationConfigBinding) {
					bel.setAttribute("type", "config");
					bel.setAttribute("value", ""+((StationConfigBinding)b).getValue());
				}
				if (b instanceof StationInputBinding) {
					bel.setAttribute("type", "input");
					bel.setAttribute("plotconfig", ""+((StationInputBinding)b).getPlotConfig());
					bel.setAttribute("active", ""+((StationInputBinding)b).isActive());
				}
				bel.setAttribute("address", ""+b.getAddress());
				bel.setAttribute("parameter", b.getParameter().getName());
				station.appendChild(bel);
			}
			rootElement.appendChild(station);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			log.info(e.getLocalizedMessage());
		}
		DOMSource source = new DOMSource(document);
		StreamResult result = null;
		try {
			result =  new StreamResult(new File(this.path,this.filename));
		} catch (Exception e) {
			log.info(e.getLocalizedMessage());
		}
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			log.info(e.getLocalizedMessage());
		}
	}
	
	private boolean startParsing(ContentHandler h) {
		FileReader stream = null;
		
		try {
			stream = new FileReader(new File(path,filename));
		} catch(Exception ex) {
			log.info("Config Datei nicht gefunden");
			return false;
		}
		
		XMLReader parser = null;
		try {
			parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(h);
		} catch(SAXException e) {
			log.info(e.getLocalizedMessage());
			return false;
		}
		try {
			parser.parse(new InputSource(stream));
		} catch(Exception e) {
			log.info(e.getLocalizedMessage());return false;
		}
		return true;
	}

}
