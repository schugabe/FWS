package fws_master;

public enum Units {
	UNKNOWN,SPEEDKMH,SPEEDMS,FREQUENCY,DIRECTION,TEMPERATURE;

	static public String getString(Units unit) {
		switch(unit) {
			case SPEEDKMH: return "km/h";
			case SPEEDMS: return "m/s";
			case FREQUENCY: return "Hz";
			case DIRECTION: return "Richtung";
			case TEMPERATURE: return "¡C";
			default: return "";
		}
	}
	
	static public Units getUnit(String unit) {
		if (unit == "km/h")
		 return SPEEDKMH;
		if (unit ==  "m/s")
			return SPEEDMS;
		if (unit ==  "Hz")
			return FREQUENCY;
		if (unit ==  "Richtung")
			return DIRECTION;
		if (unit ==  "¡C")
			return TEMPERATURE;
		 return UNKNOWN;
	}
}
