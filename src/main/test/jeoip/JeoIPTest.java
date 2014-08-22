package jeoip;

import java.io.IOException;

import org.junit.*;

import static org.junit.Assert.*;

public class JeoIPTest {
	JeoIP target;

	@Before
	protected void setUp() throws Exception {
		target = new JeoIP();
	}

	public void testIP() throws Exception {
		String country = target.getCountryName("www.darwinsys.com");
		assertEquals("Canada", country);
		country = target.getCountryName("www.staffs.ac.uk");
		assertEquals("United Kingdom", country);
		country = target.getCountryName("72.5.124.61"); // sun.com
		assertEquals("United States of America", country);
	}

	@After
	public void tearDown() throws IOException {
		target.close();
	}
}
