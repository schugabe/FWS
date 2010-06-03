package fws_master;

import java.net.*;
import net.wimpi.modbus.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.SimpleRegister;
import java.util.logging.Logger;

/**
 * This class wraps the jaMod library. Connects to the slaves and reads the values.
 * @author Johannes Kasberger
 *
 */
public class ModBusWrapper {
	private TCPMasterConnection connection = null; 
	private static Logger log = Logger.getLogger("fws_master.modbus");
	private ModbusTCPTransaction transaction;
	private InetAddress address = null;
	private int port = Modbus.DEFAULT_PORT;
	
	/**
	 * Creates the ModBusWrapper.
	 * @param ip can contain the port information
	 */
	public ModBusWrapper(String ip) {
	    int idx = ip.indexOf(':');
	   
	    // extract port information
	    if(idx > 0) {
	      port = Integer.parseInt(ip.substring(idx+1));
	      ip = ip.substring(0,idx);
	    }
	    
	    try {
	    	address = InetAddress.getByName(ip);
	    } catch (Exception e) {
	    	log.severe("Failed to create address: "+ip+";"+e.getMessage());
	    	return;
	    }
	}
	
	/**
	 * Read one value of the slave	
	 * @param address Address of Memory of value in slave
	 * @return the read value
	 */
	public int sendReadRequest(int address) throws Exception {
		
		ReadInputRegistersResponse response = null;
		int value = 0;
		
		
		try {
			ReadInputRegistersRequest request = new ReadInputRegistersRequest(address,1);
			response = (ReadInputRegistersResponse)this.sendRequest(request);
			value = response.getRegisterValue(0);
			if (value == 0xFFFF)
				throw new Exception("0xFFFF received - value ignored");
		} catch (Exception ex) {
			log.warning("Error on reading value: "+ex.getMessage());
			throw ex;
		}
		return value;
	}
	
	/**
	 * Write a configuration value to the slave
	 * @param address Address of the configuration value
	 * @param value Value to be set
	 * @return true if value received correctly
	 */
	public boolean sendWriteRequest(int address,int value) {
		SimpleRegister reg = new SimpleRegister(value);
		
		try {
			WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(address,reg);
			WriteSingleRegisterResponse response = (WriteSingleRegisterResponse)this.sendRequest(request);
			
			if (value !=response.getRegisterValue()) {
				log.warning("The sent value isn't the same as the received value");
				return false;
			} 
		} catch (Exception ex) {
			log.warning("Error on writing value: "+ex.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Send a request to JAMod library. Opens and closes the connection each time. Otherwise the TCP Stack of the slaves seams to have problems
	 * creating a response.
	 * @param request
	 * @return the response
	 */
	private ModbusResponse sendRequest(ModbusRequest request) {
		if (request==null)
			return null;
		prepareConnection();
		
		ModbusResponse response = null;
		transaction.setRequest(request);
		
		try {
			transaction.execute();
			response = transaction.getResponse();
		} catch (Exception e) {
			log.warning("Sending request not successful: "+e.getMessage());
		}
		this.connection.close();
		
		return response;
	}

	/**
	 * Opens a new connection to the slave.
	 */
	private void prepareConnection() {
		try {
			connection = new TCPMasterConnection(address);
		    connection.setPort(port);
		    
			connection.connect();
			
			transaction  = new ModbusTCPTransaction(this.connection);
		} catch (Exception e) {
			log.warning("Opening connection not successful: "+e.getMessage());
		}
		
	}
}
