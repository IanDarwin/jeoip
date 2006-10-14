package jeoip;

import java.io.IOException;

public class Main {

	/**
	 * Demonstration/utility to run the main part.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			System.err.printf("Usage: %s IP4Addr [...]%n", "JeoIP");
			System.exit(1);
		}
		JeoIP p = new JeoIP();
		for (String host : args) {
			System.out.printf("Host %s Country %s%n",
					host, p.getCountryName(host));
		}
	}

}
