/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 2006.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package jeoip;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Look up the country in which an IPV4 address is located.
 * <p>
 * Works by reading the GeoIP country database, a free database that
 * you can download from GeoIP.com, at
 * http://www.maxmind.com/download/geoip/database/GeoIP.dat.gz You must then
 * uncompress it. MaxMind.com retains ALL RIGHTS in the database; they also
 * provide a GPL'd C codebase for reading the free database. They provide
 * much better databases on a paid subscription basis; you are encouraged to use
 * those if you need finer resolution (this program at present only supports
 * the free version).
 * MaxMind's download package includes an update program that they suggest you
 * run monthly.
 * <p>
 * On OpenBSD, the port/package "GeoIP" (category net) installs the database
 * and the C programs to use it.
 * <p>
 * Once you have GeoIP installed, you can use JeoIP; here is an example
 * from a (non-optimal, non-well-structured) JSP:
 * <p><pre>
 * &lt;font size="-4">
 * Your IP address is &lt;%= request.getRemoteAddr() %>.
 * <br/>
 * You are located in
 * &lt;%= new jeoip.JeoIP().getCountryName(request.getRemoteAddr()) %>.
 * &lt;/font>
 * </pre>
 * @author Ian Darwin
 */
public class JeoIP {

	private final boolean DEBUG = false;

	/** This is the default location when you install the OpenBSD port/package. */
	public static final String DEFAULT_DIR = "/usr/local/share/examples/GeoIP/";

	public static final String DB = "GeoIP.dat";

	private final long COUNTRY_BEGIN = 16776960L;

	private final String dbFullPath;

	private RandomAccessFile dbIO;

	private final long databaseSegments = COUNTRY_BEGIN;

	private final int recordLength = 3;

	public JeoIP(String dir) throws IOException {
		dbFullPath = dir + "/" + DB;

		// Make sure file is readable.
		File dbFile = new File(dbFullPath);
		if (!dbFile.exists() || !dbFile.canRead()) {
			throw new IOException("File not readable: " + dbFullPath);
		}

		// Set up random access file.
		dbIO = new RandomAccessFile(dbFile, "r");
	}

	public JeoIP() throws IOException {
		this(DEFAULT_DIR);
	}

	public void close() throws IOException {
		dbIO.close();
		dbIO = null;
	}

	/** Get the name of the country for a given IP address.
	 * @param ip The IPV4 address as a string.
	 * @return The country name, or "--"
	 * @throws IOException if something goes wrong reading the database.
	 */
	public String getCountryName(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	/** Get the two-letter ISO country code for a given IP
	 * @param ip The IPV4 address as a string.
	 * @return The country code, or "--" if unknown.
	 * @throws IOException
	 */
	public String getCountryCode2(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	/** Get the three-letter ISO country code for a given IP
	 * @param ip The IPV4 address as a string.
	 * @return The country code, or "--" if unavailable.
	 * @throws IOException
	 */
	public String getCountryCode3(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	/**
	 * Not implemented.
	 * @param ip
	 * @return The Continent
	 */
	public Continent getContinent(String ip) {
		throw new IllegalArgumentException("Not written yet, sorry.");
	}

	/** Convert a remote host name to a packed integer IPV4 address.
	 * @param remoteHost The host name or IPV4 address
	 * @return the IP address packed into an int.
	 */
	public static int ipAddrToNum(String remoteHost) {
		try {
			InetAddress addr = InetAddress.getByName(remoteHost);
			return addr.hashCode();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.toString());
		}
	}

	// PRIVATE METHODS

	/** Return the int country code given an IP V4 address packed into an int
	 * The inner logic of this was adapted from MaxMind's implementation.
	 * @param ipv4Num The IP address, packed into an int.
	 * @return country code number
	 * @throws IOException If anything much goes wrong.
	 */
	private long ipToCountryCode(int ipv4Num) throws IOException {

		if (dbIO == null) {
			throw new IOException("file has been closed");
		}

		int buf[] = new int[2 * recordLength];
		long offset = 0, tempOffset = 0;

		for (int depth = 31; depth >= 0; depth--) {
			final long seekTo = recordLength * 2 * offset;
			if (DEBUG)
				System.out.printf("Depth %d, offset %d, seekto %d...buf=", depth, offset, seekTo);
			dbIO.seek(seekTo);
			for (int i = 0; i < buf.length; i++) {
				buf[i] = dbIO.readUnsignedByte();
			}
			if (DEBUG) {
				for (int i = 0; i < buf.length; i++) {
					System.out.printf("%02x,", buf[i]);
				}
				System.out.print(" tempOffset=");
			}

			// Don't mess with these parens...
			if ((ipv4Num & (1 << depth)) != 0) { /* take right branch */
				if (DEBUG)
					System.out.print('R');
				tempOffset = ((long)(buf[3]) << 0) |
				((long)(buf[4]) << 8) |
				((long)(buf[5]) << 16);
			} else {							/* left branch */
				if (DEBUG)
					System.out.print('L');
				tempOffset = ((long)(buf[0]) << 0) |
				((long)(buf[1]) << 8) |
				((long)(buf[2]) << 16);
			}
			if (DEBUG)
				System.out.println(tempOffset);
			if (tempOffset >= databaseSegments) {
				return tempOffset - COUNTRY_BEGIN;
			}
			offset = tempOffset;
		}

		/* Should be "NOTREACHED" */
		System.err.printf("Error grubbing around in database for ip = %x - database may be corrupt?%n" ,ipv4Num);
		return 0;
	}

	/** Convert IP name to an IPV4 addrss */
	private long ipToCountryCode(String ipName) throws IOException {
		return ipToCountryCode(ipAddrToNum(ipName));
	}


}
