package fws_simulator;

import java.util.Random;

import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.*;
//import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;

//java -classpath ./jamod-1.2.jar:. -Dnet.wimpi.modbus.debug=true fws_simulator.sim
public class sim {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ModbusTCPListener listener = null;
			SimpleProcessImage spi = null;
			int port = 30000;

			SimpleRegister reg = new SimpleRegister();
			reg.setValue(101);
			
			SimpleInputRegister reg1 = new SimpleInputRegister();
			reg1.setValue(201);
			
			SimpleInputRegister reg2 = new SimpleInputRegister();
			reg2.setValue(201);
			
			spi = new SimpleProcessImage();
			spi.addRegister(reg);
			spi.addInputRegister(reg1);
			spi.addInputRegister(reg2);
			
			//3. Set the image on the coupler
			ModbusCoupler.getReference().setProcessImage(spi);
			ModbusCoupler.getReference().setMaster(false);
			ModbusCoupler.getReference().setUnitID(15);   
			
			listener = new ModbusTCPListener(3);
			listener.setPort(port);
			listener.start();  
			
			while(true) {
				Random asdf  =new Random();
				int next = Math.abs(asdf.nextInt(20));
				reg1.setValue((short)next);
				
				next = Math.abs(asdf.nextInt(50));
				reg2.setValue((short)next);
				
				//System.out.println(reg.getValue());
				
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}
