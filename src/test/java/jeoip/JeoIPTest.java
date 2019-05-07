package jeoip;

import java.io.IOException;

import org.junit.*;

import static org.junit.Assert.*;

public class JeoIPTest {
	JeoIP target;

	@Before
	public void setUp() throws Exception {
		target = new JeoIP();
	}

	@Test
	public void testDarwin() throws Exception {
		String actual = target.getCountryName("www.darwinsys.com");
		assertEquals("Canada", actual);
	}
	public void testStaffs() throws Exception {
		String actual = target.getCountryName("www.gov.uk");
		assertEquals("United Kingdom", actual);
	}
	public void testOracle() throws Exception {
		String actual = target.getCountryName("72.5.124.61"); // sun.com
		assertEquals("United States of America", actual);
	}

	@After
	public void tearDown() throws IOException {
		if (target != null) {
			target.close();
		}
	}
}
