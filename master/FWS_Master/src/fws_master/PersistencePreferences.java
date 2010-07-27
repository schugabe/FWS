package fws_master;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

/**
 * Class for loading configuration file. Loads the handler to the requested information. The xml file is read with the help of sax.
 * 
 * @author Johannes Kasberger
 *
 */
public class PersistencePreferences {
	private String path;
	private String filename;
	private Logger log = Logger.getLogger("fws_master.config");
	
	/**
	 * The configuration is searched in the path
	 * @param path
	 * @param filename
	 */
	public PersistencePreferences(String path,String filename) {
		this.path = path;
		this.filename = filename;
		log.config("Reading config from "+path+"/"+filename);
	}
	
	/**
	 * Load the slaves from the config file
	 * @param params
	 * @return a slave controller with alle slaves from the config
	 */
	public SlaveController loadSlaves(ParameterController params) {
		SlaveController slaves = new SlaveController();
		SlaveContentHandler h = new SlaveContentHandler(slaves,params);
		this.startParsing(h);
		return slaves;
	}
	
	/**
	 * Loads all parameters of the configuration file
	 * @return ParameterController with the loaded Parameters
	 */
	public ParameterController loadParameters() {
		ParameterController params = new ParameterController();
		ParameterContentHandler h = new ParameterContentHandler(params);
		this.startParsing(h);
		return params ;
	}
	
	/**
	 * The MasterConfig are all configuration values for the master itself (e.g. output file generation interval)
	 * @return the Handler with all loaded values
	 */
	public MasterContentHandler loadMasterConfig() {
		MasterContentHandler h = new MasterContentHandler();
		this.startParsing(h);
		return h;
	}
	
	/**
	 * Save all values to the xml file
	 * @param params
	 * @param slaves
	 * @param outDir
	 * @param generatorTime
	 */
	public void saveSettings(ParameterController params,SlaveController slaves,String outDir, int generatorTime, boolean autostart, int plotWidth, int plotHeight) {
		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.warning(e.getMessage());
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
		
		masterEl = document.createElement("autostart");
		masterEl.appendChild(document.createTextNode(""+autostart));
		rootElement.appendChild(masterEl);
		
		masterEl = document.createElement("plotwidth");
		masterEl.appendChild(document.createTextNode(""+plotWidth));
		rootElement.appendChild(masterEl);
		
		masterEl = document.createElement("plotheight");
		masterEl.appendChild(document.createTextNode(""+plotHeight));
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
		for (Slave s:slaves.getSlaves()) {
			Element slave = document.createElement("slave");
			slave.setAttribute("name", s.getSlaveName());
			slave.setAttribute("ip", s.getIpAddress());
			slave.setAttribute("intervall", ""+s.getPollingInterval());
			
			for(Binding b:s.getBindings()) {
				Element bel = document.createElement("binding");
				if (b instanceof SlaveConfigBinding) {
					bel.setAttribute("type", "config");
					bel.setAttribute("value", ""+((SlaveConfigBinding)b).getValue());
					bel.setAttribute("transfered", ""+((SlaveConfigBinding)b).isTransfered());
				}
				if (b instanceof SlaveInputBinding) {
					bel.setAttribute("type", "input");
					bel.setAttribute("plotconfig", ""+((SlaveInputBinding)b).getPlotConfig());
				}
				bel.setAttribute("address", ""+b.getAddress());
				bel.setAttribute("parameter", b.getParameter().getName());
				bel.setAttribute("active", ""+b.isActive());
				slave.appendChild(bel);
			}
			rootElement.appendChild(slave);
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (TransformerConfigurationException e) {
			log.warning(e.getMessage());
		}
		DOMSource source = new DOMSource(document);
		
		StreamResult result = null;
		try {
			result =  new StreamResult(new File(this.path,this.filename));
			
		} catch (Exception e) {
			log.warning(e.getMessage());
		}
		try {
			transformer.setOutputProperty("encoding", "UTF-8");
			transformer.setOutputProperty("indent", "yes");
			transformer.transform(source, result);
			
		} catch (TransformerException e) {
			log.warning(e.getMessage());
		}
	}
	
	/**
	 * For each kind of information the xml file is parsed.
	 * @param h
	 * @return if parsing was successful 
	 */
	private boolean startParsing(ContentHandler h) {
		//FileReader stream = null;
		InputStreamReader stream = null;
		try {
			stream = new InputStreamReader(new FileInputStream(new File(path,filename)),"UTF-8");
			
		} catch(Exception ex) {
			log.warning("Config file not found");
			return false;
		}
		
		XMLReader parser = null;
		try {
			parser = XMLReaderFactory.createXMLReader();
			
			parser.setContentHandler(h);
		} catch(SAXException e) {
			log.warning(e.getMessage());
			return false;
		}
		try {
			parser.parse(new InputSource(stream));
		} catch(Exception e) {
			log.warning(e.getMessage());return false;
		}
		return true;
	}

}
