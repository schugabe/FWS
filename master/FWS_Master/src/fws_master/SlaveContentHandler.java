package fws_master;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
/**
 * Loads the Slaves from the configuration files.
 * @author Johannes Kasberger
 *
 */
public class SlaveContentHandler implements ContentHandler {
	private SlaveController controller;
	private ParameterController pcontroller;
	private Slave lastSlave;
	private States state;
	
	public SlaveContentHandler(SlaveController controller,ParameterController pcontroller) {
		this.controller = controller;
		this.pcontroller = pcontroller;
		this.state = States.IDLE;
		this.lastSlave = null;
	}
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("slave")) 
		{
			this.lastSlave = null;
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
		
		if (localName.equals("slave") && state==States.IDLE) {
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
			Slave s;
			try {
				s = new Slave(name,this.controller,ip,intervall);
			} catch(Exception e) {
				return;
			}
			try {
				this.controller.addSlave(s);
			} catch (Exception e) {
				
			}
			this.lastSlave = s;
			this.state = States.SLAVE;
		}
		if (state==States.SLAVE && localName.equals("binding")) {
			String type = null,param = null;
			int address,value;
			boolean active = false;
			boolean transfered = false;
			String plotConfig = "";
			address = value = -1;
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
				else if (tmpName.equals("plotconfig")) {
					plotConfig = atts.getValue(i);
				}
				else if (tmpName.equals("active")) {
					active = Boolean.parseBoolean(atts.getValue(i));
				}
				else if (tmpName.equals("transfered")) {
					transfered = Boolean.parseBoolean(atts.getValue(i));
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
			
			if (type.equals("input") && address != -1 && !plotConfig.equals("")) {
				new SlaveInputBinding(this.lastSlave,(InputParameter) p,address,plotConfig,active);
			} 
			else if (type.equals("config") && address != -1 & value != -1) {
				new SlaveConfigBinding(this.lastSlave, (ConfigParameter)p,address,value,active,transfered);
			}
		}

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}
	
	private enum States {
		IDLE,SLAVE;
	}

}
