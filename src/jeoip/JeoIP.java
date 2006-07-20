package jeoip;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class for looking up the country in which an IPV4 address is located.
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
	
	/**
	 * Demonstration/utility to run the main part.
	 * @param args
	 * @throws IOException 
	 */
	public void main(String[] args) throws IOException {
		for (String host : args) {
			System.out.printf("IPint %08x%n", ipAddrToNum(host));
			System.out.println(host + ": " + getCountryName(host));
		}
	}
	
	public JeoIP(String dir) throws IOException {
		dbFullPath = dir + "/" + DB;
		
		setupFile(dbFullPath);
	}
	
	public JeoIP() throws IOException {
		this(DEFAULT_DIR);
	}
	
	public void close() throws IOException {
		dbIO.close();
		dbIO = null;
	}
	
	public void setDirectory(String dir) throws IOException {
		setupFile(dir + "/" + DB);
	}
	
	/** Get the name of the country for a given IP address.
	 * @param ip
	 * @return The country name, or "--"
	 * @throws IOException if something goes wrong reading the database.
	 */
	public String getCountryName(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	/** Get the two-letter ISO country code for a given IP
	 * @param ip
	 * @return
	 * @throws IOException
	 */
	public String getCountryCode2(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	/** Get the three-letter ISO country code for a given IP
	 * @param ip
	 * @return
	 * @throws IOException
	 */
	public String getCountryCode3(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}
	
	/**
	 * Not implemented.
	 * @param ip
	 * @return
	 */
	public Continent getContinent(String ip) {
		throw new IllegalArgumentException("Not written yet, sorry.");
	}

	/** Convert a remote host name to a packed integer IPV4 address.
	 * @param remoteHost
	 * @return
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
	
	private void setupFile(String dbFullpath) throws IOException {
		
		// Make sure file is readable.
		File dbFile = new File(dbFullPath);
		if (!dbFile.exists() || !dbFile.canRead()) {
			throw new IOException("File not readable: " + dbFullPath);
		}

		// Set up random access file.
		dbIO = new RandomAccessFile(dbFile, "r");
	}
	
	/** Return the int country code given an IP V4 address packed into an int
	 * @param ipv4Num In hindsight we should have done...?
	 * @return country code number
	 * @throws IOException
	 */
	private long ipToCountryCode(int ipv4Num) throws IOException {
		
		if (dbIO == null) {
			throw new IOException("file has been closed");
		}
		
		long x = 0;
		int buf[] = new int[2 * recordLength];
		long offset = 0;

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
				System.out.print(" x=");
			}

			// Don't mess with these parens...
			if ((ipv4Num & (1 << depth)) != 0) { /* take right branch */
				if (DEBUG)
					System.out.print('R');
				x = ((long)(buf[3]) << 0) |
				((long)(buf[4]) << 8) |
				((long)(buf[5]) << 16);
			} else {							/* left branch */
				if (DEBUG)
					System.out.print('L');
				x = ((long)(buf[0]) << 0) |
				((long)(buf[1]) << 8) |
				((long)(buf[2]) << 16);
			}
			if (DEBUG)
				System.out.println(x);
			if (x >= databaseSegments) {
				return x - COUNTRY_BEGIN;
			}
			offset = x;
		}

		/* Should be "NOTREACHED" */
		System.err.printf("Error grubbing around in database for ip = %x - database may be corrupt?%n" ,ipv4Num);
		return 0;
	}

	private long ipToCountryCode(String ipName) throws IOException {
		return ipToCountryCode(ipAddrToNum(ipName));
	}


}
