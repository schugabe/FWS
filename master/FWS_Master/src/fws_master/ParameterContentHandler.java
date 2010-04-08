package fws_master;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ParameterContentHandler implements ContentHandler {

	private Parameter_Controller params;
	private States state;
	private SubStates substate;
	private String name;
	private Units unit;
	private History_Functions hist;
	private Output_Formats format;
	private int fields;
	
	public ParameterContentHandler(Parameter_Controller params) {
		this.params = params;
		this.state = States.IDLE;
		this.substate = SubStates.IDLE;
		this.reInitFields();
	}
	
	private void reInitFields() {
		this.name = null;
		this.unit = Units.UNKNOWN;
		this.format = Output_Formats.UNKNOWN;
		this.hist = History_Functions.AVG;
		this.substate = SubStates.IDLE;
		this.state = States.IDLE;
		fields = 0;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		if (state== States.IDLE)
			return;
		
		char [] conv = new char[length];
		System.arraycopy(ch, start, conv, 0, length);
		String content = new String(conv);
		
		
		fields++;
		switch(substate) {
			case NAME: this.name = content; break;
			case FORMAT: this.format = Output_Formats.getFormat(content); break;
			case UNIT: this.unit = Units.getUnit(content); break;
			case FUNC: this.hist = History_Functions.getHist(content); break;
		}
		
	}

	@Override
	public void endDocument() throws SAXException {
		return;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (name!=null && !this.name.equals("")) {
			Parameter p = null;
			if (state == States.CP && fields==1) {
				p = new Config_Parameter(this.name,this.params);
				this.params.addParameter(p);
				this.reInitFields();
			}
			if (state == States.IP && fields==4) {
				if (this.unit != Units.UNKNOWN && this.format != Output_Formats.UNKNOWN) {
					p= new Input_Parameter(this.name,this.params,unit,format,hist);
					this.params.addParameter(p);
					this.reInitFields();
				}
			}
		}
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator locator) {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
		if (this.params.getParameters().size() > 0) {
			for(Parameter p:this.params.getParameters()) {
				this.params.removeParameter(p);
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (localName == null)
			return;
		if (state == States.IDLE && localName.equals("parameter") && atts!=null) {
			String pKind = atts.getValue(0);
			if (pKind== null)
				return;
			if (pKind.equals("input")) {
				this.state = States.IP;
			} else if (pKind.equals("config")) {
				this.state = States.CP;
			}
		}
		
		if (state!=States.IDLE) {
			if (localName.equals("name")) {
				this.substate = SubStates.NAME;
			}
		}
		if (state == States.IP) {
			if (localName.equals("format")) {
				this.substate = SubStates.FORMAT;
			}
			if (localName.equals("unit")) {
				this.substate = SubStates.UNIT;
			}
			if (localName.equals("history")) {
				this.substate = SubStates.FUNC;
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}
	private enum States {
		IDLE, CP,IP;
	}
	
	private enum SubStates {
		IDLE,NAME,UNIT,FORMAT,FUNC;
	}
}

