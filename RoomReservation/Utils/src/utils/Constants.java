package utils;

public final class Constants {
	public static final int PORT = 1112;
	public static final String USERS = "users.txt"; //file storing username and password
	public static final String USERS_HASH_SALT = "usersBcrypt.txt";
	public static final String RESERVATIONS = "reservations.txt"; // file of time slots, availability, and reserved by
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String LOGIN = "LOGIN";
	public static final String RESERVE = "RESERVE";
	public static final String ERROR = "ERROR";
	public static final String EXIT = "EXIT";
	public static final String PUBLIC_KEY_EXCHANGE = "EXCHANGE";
	public static final String NO_TIME = "NO TIMES AVALIABLE";
	public static final String NEWLINE = "\n";
	public static final String BREAK = "::";
	public static final String AVAILABLE = "available";
	public static final String BOOKED = "reserved";
	public static final int FILE_START = 1; // Header Line is on 0

	public static String ALGORITHM = "RSA";
	public static String PADDING = "OAEP";
	public static final String CLIENT_PUB = "client.pub";
	public static final String CLIENT_PRIV = "client.private";
	public static final String SERVER_PUB = "server.pub";
	public static final String SERVER_PRIV = "server.private";
	public static final String OAEP = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
	public static final String PKCS = "RSA/ECB/PKCS1Padding";
	
	public static final String HOST = "localhost";

}
