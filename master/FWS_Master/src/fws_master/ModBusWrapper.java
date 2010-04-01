package fws_master;

import java.net.*;
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.SimpleRegister;
import net.wimpi.modbus.util.*;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("unused")
public class ModBusWrapper {
	private TCPMasterConnection connection = null; 
	private static Logger log = Logger.getLogger("fws_master.modbus");
	
	public ModBusWrapper(String ip) {
		InetAddress address = null;
		int port = Modbus.DEFAULT_PORT;
	    int idx = ip.indexOf(':');
	    
	    log.info("Baue Verbindung zu "+ip+" auf");
	    
	    // Portangabe aus IP Adresse lesen
	    if(idx > 0) {
	      port = Integer.parseInt(ip.substring(idx+1));
	      ip = ip.substring(0,idx);
	    }
	    
	    try {
	    	address = InetAddress.getByName(ip);
	    	//address = InetAddress.getLocalHost();
	    } catch (Exception e) {
	    	log.throwing("ModBusWrapper", "Adresse erzeugen", e);
	    	return;
	    }
	    
	    
	    try {
	    	connection = new TCPMasterConnection(address);
		    connection.setPort(port);
			connection.connect();
			log.info("Verbindung zu "+ip+" aufgebaut");
		} catch (Exception e) {
			log.info("Fehler aufgetreten "+e.getMessage());
		}
		
	}
	
	public boolean hasConnection() {
		return this.connection.isConnected();
	}

	public void releaseConnection() {
		if (this.hasConnection())
			this.connection.close();
	}
	
	public int sendReadRequest(int address) {
		ReadInputRegistersRequest request = new ReadInputRegistersRequest(address,1);
		ReadInputRegistersResponse response = null;
		int value = 0;
		
		response = (ReadInputRegistersResponse)this.sendRequest(request);
		try {
			value = response.getRegisterValue(0);
		} catch (Exception ex) {
			log.log(Level.WARNING, "Fehler beim auslesen");
		}
		log.info("Wert: "+value);
		return value;
	}
	
	public boolean sendWriteRequest(int address,int value) {
		SimpleRegister reg = new SimpleRegister(value);
		
		WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(address,reg);
		WriteSingleRegisterResponse response = (WriteSingleRegisterResponse)this.sendRequest(request);
		
		if (value !=response.getRegisterValue()) {
			log.log(Level.WARNING, "Der gesendete Wert passt mit dem Empfangenen nicht zusammen");
			return false;
		}
		
		return true;
	}
	
	private ModbusResponse sendRequest(ModbusRequest request) {
		ModbusTCPTransaction transaction  = new ModbusTCPTransaction(this.connection);
		ModbusResponse response = null;
		transaction.setRequest(request);
		
		log.info("Sende Anfrage an "+this.connection.getAddress().toString()+" Anfrage "+request.toString());
		try {
			transaction.execute();
			response = transaction.getResponse();
			log.info("Antwort erhalten"+response.toString());
		} catch (Exception e) {
			log.throwing("ModBusWrapper", "sendRequest", e);
		}
		
		return response;
	}
}
