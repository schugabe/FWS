package fws_master;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Loads the general configuration parameters of the master.
 * @author Johannes Kasberger
 *
 */
public class MasterContentHandler implements ContentHandler {
	private States state;
	private String path;
	private int generatorTime;
	private boolean autoStart;
	
	public MasterContentHandler() {
		this.state = States.IDLE;
		this.path = "";
		this.generatorTime = 30;
		this.autoStart = false;
	}
	
	public int getGeneratorTime() {
		return this.generatorTime;
	}
	public String getPath() {
		return path;
	}
	

	/**
	 * @return the autoStart
	 */
	public boolean isAutoStart() {
		return autoStart;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (state == States.IDLE) {
			return;
		}
		
		char [] conv = new char[length];
		System.arraycopy(ch, start, conv, 0, length);
		String content = new String(conv);
		
		switch(state) {
		case PATH: this.path = content; break;
		case GENERATORTIME: try {this.generatorTime = Integer.parseInt(content);} catch(Exception e) {this.generatorTime = 30; } break;
		case AUTOSTART: try {this.autoStart = Boolean.parseBoolean(content); } catch(Exception e) { this.autoStart = false; } break;
		}
	}

	@Override
	public void endDocument() throws SAXException {
		this.state = States.IDLE;

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		this.state = States.IDLE;

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

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (this.state==States.IDLE) {
			if (localName.equals("path")) {
				this.state = States.PATH;
			} else if (localName.equals("generatortime")) {
				this.state = States.GENERATORTIME;
			} 
			else if (localName.equals("autostart")) {
				this.state = States.AUTOSTART;
			} 
		} else {
			this.state = States.IDLE;
		}

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}
	
	private enum States {
		IDLE,PATH,GENERATORTIME,AUTOSTART;
	}

}
