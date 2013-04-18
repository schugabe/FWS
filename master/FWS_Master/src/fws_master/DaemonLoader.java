package fws_master;

public class DaemonLoader  {
	private FWSMaster master;
	
	public void init(String[] arguments) {
		master = new FWSMaster();		
	}

	public void start() throws Exception {
		master.start();		
	}

	public void stop() throws Exception {
		master.stop();		
	}

	public void destroy() {
	}

}
