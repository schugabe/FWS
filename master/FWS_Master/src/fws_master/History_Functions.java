package fws_master;

public enum History_Functions {
AVG,MAX,MIN;
	static public History_Functions getHist(String name) {
		if (name.equals("AVG"))
			return AVG;
		if (name.equals("MAX"))
			return MAX;
		else 
			return MIN;
	}
}
