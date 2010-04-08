package fws_master;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class StationContentHandler implements ContentHandler {
	private StationController controller;
	private ParameterController pcontroller;
	private Station lastStation;
	private States state;
	
	public StationContentHandler(StationController controller,ParameterController pcontroller) {
		this.controller = controller;
		this.pcontroller = pcontroller;
		this.state = States.IDLE;
		this.lastStation = null;
	}
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("station")) 
		{
			this.lastStation = null;
			this.state = States.IDLE;
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	@Override
	public void startDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (localName==null)
			return;
		
		if (localName.equals("station") && state==States.IDLE) {
			String name,ip;
			name = ip = null;
			int intervall;
			intervall = 0;
			for(int i=0;i<atts.getLength();i++) {
				String tmpName = atts.getLocalName(i);
				if (tmpName.equals("ip")) {
					ip = atts.getValue(i);
				}
				else if (tmpName.equals("name")) {
					name = atts.getValue(i);
				}
				else if (tmpName.equals("intervall")) {
					intervall = Integer.parseInt(atts.getValue(i));
				}	
			}
			Station s = new Station(name,this.controller,ip,intervall);
			this.controller.addStation(s);
			this.lastStation = s;
			this.state = States.STATION;
		}
		if (state==States.STATION && localName.equals("binding")) {
			String type = null,param = null;
			int address,value,bufferSize;
			address = value = bufferSize= -1;
			for(int i=0;i<atts.getLength();i++) {
				String tmpName = atts.getLocalName(i);
				if (tmpName.equals("type")) {
					type = atts.getValue(i);
				}
				else if (tmpName.equals("address")) {
					address = Integer.parseInt(atts.getValue(i));
				}
				else if (tmpName.equals("value")) {
					value = Integer.parseInt(atts.getValue(i));
				}
				else if (tmpName.equals("buffersize")) {
					bufferSize = Integer.parseInt(atts.getValue(i));
				}
				else if (tmpName.equals("parameter")) {
					param = atts.getValue(i);
				}
			}
			if (param == null)
				return;
			
			Parameter p = this.pcontroller.findParameter(param);
			
			if (p == null)
				return;
			
			if(type == null)
				return;
			
			if (type.equals("input") && address != -1 && bufferSize != -1) {
				new StationInputBinding(this.lastStation,(InputParameter) p,address,bufferSize);
			} 
			else if (type.equals("config") && address != -1 & value != -1) {
				new StationConfigBinding(this.lastStation, (ConfigParameter)p,address,value);
			}
		}

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}
	
	private enum States {
		IDLE,STATION;
	}

}
