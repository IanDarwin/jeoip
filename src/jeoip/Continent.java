package jeoip;

public enum Continent {
	AFRICA("Africa"),
	ANTARCTICA("Antarctica"),
	ASIA_PACIFIC("Asia/Pacific"),
	AUSTRALIA("Australia"),
	EUROPE("Europe"),
	NORTH_AMERICA("North America"),
	OFF_CONTINENT("Off Continent"),
	SOUTH_AMERICA("South America"),
	UNKNOWN("Unknown");

	String name;

	Continent(String d) { name = d; }

	public static Continent byName(String desc) {
		if ("--".equals(desc))
				return Continent.UNKNOWN;
		return Continent.valueOf(desc.replaceAll("[ /]", "_").toUpperCase());
	}
}
