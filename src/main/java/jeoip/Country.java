package jeoip;

/**
 * Simple bean for the Country name, codes, continents, etc.
 */
public class Country {
	/** The 2-letter ISO-3166 country code */
	String code2;
	/** The 3-letter ISO-3166 country code */
	String code3;
	/** Country Name */
	String name;
	/** The continent */
	Continent continent;

	public Country(String name, String code2, String code3, Continent continent) {
		this.name = name;
		this.code2 = code2;
		this.code3 = code3;
		this.continent = continent;
	}

	public Country(String name, String code2, String code3, String continent) {
		this(name, code2, code3, Continent.byName(continent));
	}
}
