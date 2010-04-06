package fws_master;

public enum Units {
	UNKNOWN,SPEEDKMH,SPEEDMS,FREQUENCY,DIRECTION,TEMPERATURE;

	static public String getString(Units unit) {
		switch(unit) {
			case SPEEDKMH: return "km/h";
			case SPEEDMS: return "m/s";
			case FREQUENCY: return "Hz";
			case DIRECTION: return "Richtung";
			case TEMPERATURE: return "GradC";
			default: return "";
		}
	}
	
	static public Units getUnit(String unit) {
		if (unit.equals("km/h"))
		 return SPEEDKMH;
		if (unit.equals("m/s"))
			return SPEEDMS;
		if (unit.equals("Hz"))
			return FREQUENCY;
		if (unit.equals("Richtung"))
			return DIRECTION;
		if (unit.equals("GradC"))
			return TEMPERATURE;
		 return UNKNOWN;
	}
}
