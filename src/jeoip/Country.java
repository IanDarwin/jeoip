package jeoip;
public class Country {

	String code2;

	String code3;

	String name;

	String continent;

	public Country(String name, String code2, String code3, String continent) {
		this.name = name;
		this.code2 = code2;
		this.code3 = code3;
		this.continent = continent;
	}
}