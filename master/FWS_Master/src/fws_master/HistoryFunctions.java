package fws_master;

public enum HistoryFunctions {
AVG,MAX,MIN;
	static public HistoryFunctions getHist(String name) {
		if (name.equals("AVG"))
			return AVG;
		if (name.equals("MAX"))
			return MAX;
		else 
			return MIN;
	}
}
