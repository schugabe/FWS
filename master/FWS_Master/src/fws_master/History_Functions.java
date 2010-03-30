package fws_master;

public enum History_Functions {
AVG,MAX,MIN;
	static public History_Functions getHist(String name) {
		if (name == "AVG")
			return AVG;
		if (name == "MAX")
			return MAX;
		else 
			return MIN;
	}
}
