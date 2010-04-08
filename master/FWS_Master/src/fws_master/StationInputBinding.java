package fws_master;

public class StationInputBinding extends Binding {
	private int buffer_size;
	
	public StationInputBinding(Station station, InputParameter parameter, int address) {
		super(station,parameter,address);
		this.buffer_size = 100;
	}
	public StationInputBinding(Station station, InputParameter parameter, int address, int buffer_size) {
		super(station,parameter,address);
		this.setBuffer_size(buffer_size);
	}

	/**
	 * @param buffer_size the buffer_size to set
	 */
	public void setBuffer_size(int buffer_size) {
		this.buffer_size = buffer_size;
	}

	/**
	 * @return the buffer_size
	 */
	public int getBuffer_size() {
		return buffer_size;
	}
}
