package jeoip;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class for reading the GeoIP country database; this is a free database that
 * you can download from GeoIP.com, at
 * http://www.maxmind.com/download/geoip/database/GeoIP.dat.gz You must then
 * uncompress it. MaxMind.com retains ALL RIGHTS in the database; they also
 * provide a GPL'd codebase for reading the free database; they also provide
 * much better databases on a paid subscription basis; you are encouraged to use
 * those if you need finer resolution.
 * 
 * @author ian
 */
public class JeoIP {

	private final static boolean DEBUG = false;

	public static final String DIR = "/usr/local/share/examples/GeoIP/";

	public static final String DB = "GeoIP.dat";
	
	/** The constant value for the free country database;
	 * the older type > 106 databases are not supported.
	 */
	public static final int FREE_DB_TYPE = 1;

	public final static long COUNTRY_BEGIN = 16776960L;
	
	public final static long STRUCTURE_INFO_MAX_SIZE = 20L;

	public final static long DATABASE_INFO_MAX_SIZE = 100L;

	public final static long MAX_ORG_RECORD_LENGTH = 300L;
	
	private static String dbFullPath = DIR + "/" + DB;

	private static File dbFile;

	private static RandomAccessFile dbIO;
	
	private final static long databaseSegments = COUNTRY_BEGIN;

	private final static int recordLength = 3;
	
	static {
		// Check file and set up segment(s).
		try {

			dbFile = new File(dbFullPath);
			if (!dbFile.exists() || !dbFile.canRead()) {
				throw new IOException("File not readable: " + dbFullPath);
			}
			dbIO = new RandomAccessFile(dbFile, "r");
//			final long dbSize = dbIO.length();
//			dbIO.seek(dbSize - 3);
//			for (int i = 0; i < STRUCTURE_INFO_MAX_SIZE; i++) {
//				int[] delim = new int[3];
//				for (int j = 0; j < delim.length; j++) {
//					delim[j] = dbIO.readUnsignedByte();
//				}
//				System.out.println(i + " " + delim[0]+ " " +
//						delim[1]+ " " + delim[2]);
//				if (delim[0] == 255 && delim[1] == 255 && delim[2] == 255) {
//					System.out.println("Found DELIM, checking dbtype");
//				} else {
//					System.out.println("BACK...");
//					dbIO.seek(dbIO.getFilePointer()-4);
//				}
//			}
		} catch (IOException e) {
			throw new RuntimeException("Input error: " + e);
		}
	}

	/** Return the int country code given an IP V4 address packed into an int
	 * @param ipv4Num In hindsight w
	 * @return country code number
	 * @throws IOException
	 */
	private static long ipToCountryCode(int ipv4Num) throws IOException {
			int depth;
			long x = 0;
			int buf[] = new int[2 * recordLength];
			long offset = 0;

			for (depth = 31; depth >= 0; depth--) {
				final long seekTo = recordLength * 2 * offset;
				if (DEBUG)
				System.out.printf("Depth %d, offset %d, seekto %d...buf=", depth, offset, seekTo);
				dbIO.seek(seekTo);
				for (int i = 0; i < buf.length; i++) {
					buf[i] = dbIO.readUnsignedByte();
				}
				
				for (int i = 0; i < buf.length; i++) {
					System.out.printf("%02x,", buf[i]);
				}
				if (DEBUG)
				System.out.print(" x=");

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
				System.out.println(x);
				if (x >= databaseSegments) {
					return x - COUNTRY_BEGIN;
				}
				offset = x;
			}

			/* "NOTREACHED" */
			System.err.printf("Error Rummaging Thru Database for ip = %x - database may be corrupt?%n" ,ipv4Num);
			return 0;
	}

	private static long ipToCountryCode(String ipName) throws IOException {
		return ipToCountryCode(ipAddrToNum(ipName));
	}

	public static String getCountryName(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	public static String getCountryCode2(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}

	public static String getCountryCode3(String ip) throws IOException {
		Country c = Countries.getCountry((int)ipToCountryCode(ip));
		return (c != null ? c.name : null);
	}
	
	public static Continent getContinent(String ip) {
		return null;
	}

	public static int ipAddrToNum(String remoteHost) {
		try {
			InetAddress addr = InetAddress.getByName(remoteHost);
			return addr.hashCode();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e.toString());
		}
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		for (String host : new String[] { "209.135.104.16", "128.100.65.1" }) {
			System.out.printf("IPint %08x%n", ipAddrToNum(host));
			System.out.println(host + ": " + getCountryName(host));
		}
	}

}
