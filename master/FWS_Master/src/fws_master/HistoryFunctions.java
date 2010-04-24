package fws_master;
/**
 * Functions to calculate one value per day. This value will be saved in a history of the last year. This allows the user to configure how this value is computed.
 * <ul>
 * <li>AVG: Average value is computed</li>
 * <li>MAX: Maximum value is selected</li>
 * <li>MIN: Minium value is selected<li>
 * </ul>
 * @author Johannes Kasberger
 *
 */
public enum HistoryFunctions {
AVG,MAX,MIN;

	/**
	 * Get the function for a certain function name	
	 * @param name Name of the Function
	 * @return the HistoryFunctions value of name
	 */
	static public HistoryFunctions getHist(String name) {
		if (name.equals("AVG"))
			return AVG;
		if (name.equals("MAX"))
			return MAX;
		else 
			return MIN;
	}
}
