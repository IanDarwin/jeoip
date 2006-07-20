package jeoip;

import java.io.IOException;

import junit.framework.TestCase;

public class JeoIPTest extends TestCase {
	JeoIP target;
	
	@Override
	protected void setUp() throws Exception {
		target = new JeoIP();
	}
	
	public void testIP() throws Exception {
		String country = target.getCountryName("www.darwinsys.com");
		assertEquals("Canada", country);
		country = target.getCountryName("www.staffs.ac.uk");
		assertEquals("United Kingdom", country);
//		country = target.getCountryName("www.sun.com");
		country = target.getCountryName("72.5.124.61");
		assertEquals("United States of America", country);
	}
	
	public void tearDown() throws IOException {
		target.close();
	}
}
