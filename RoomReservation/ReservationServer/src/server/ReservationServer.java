package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import generator.BCrypt;
import utils.KeyExchangeUtil;

import static utils.Constants.*;
import utils.SocketUtil;
import utils.FileUtil;

/**
 * Server to handle reservation requests
 * 
 * @author Ryan
 *
 */
public class ReservationServer {

	
	protected static String SESSION_KEY = "";
	// Global variable to track the last logged in user
	protected static String username = "";
	private static String FILE_ENCRYPTION_KEY = "A23BTIKn8dwNMTN5";
	static ArrayList<String> users = new ArrayList<String>();

	/**
	 * Main method for the class
	 * @param args
	 * @throws Exception - IOException from reading/writing socket or file
	 */
	public static void main(String[] args) throws Exception {


//		FileUtil.encryptFile(USERS_HASH_SALT, FILE_ENCRYPTION_KEY);
		Collections.addAll(users, FileUtil.decryptAndReadFile("encrypted_"+USERS_HASH_SALT, FILE_ENCRYPTION_KEY).split(NEWLINE));
		runServer();
		
	}
	
	/**
	 * Main logic of running the server
	 * @throws Exception - IOException from reading/writing socket or file
	 */
	private static void runServer() throws Exception {
		// Start listening for connections on the port
		ServerSocket listenerSocket = startServer(PORT);

		// continually process accepted connections
		while (true) {
			Socket connectionSocket = listenerSocket.accept(); // connect to an incoming request
			System.out.println("Connected to " + connectionSocket.getInetAddress());

			// Read message from the socket and log the protocol 
			String[] clientMsg = SocketUtil.readMessageFromSocket(connectionSocket, SESSION_KEY);
			System.out.println("Received " + clientMsg[0] + " request from client");
			String serverResponse = "";
			// determine which protocol and perform necessary functions
			switch (clientMsg[0]) {
			case LOGIN:
				serverResponse = loginUser(clientMsg[1]); //attempt to login the user
				// on successful login pull available times
				if (serverResponse.split(BREAK)[0].equals(SUCCESS)) {
					ArrayList<String> timeslots = FileUtil.readFileToArray(RESERVATIONS);
					serverResponse = serverResponse + findAvailableTimes(timeslots);
				}
				System.out.println(serverResponse); //log response for debug
				SocketUtil.writeToSocket(connectionSocket, serverResponse, SESSION_KEY);
				break;
			case RESERVE:
				serverResponse = reserveTime(clientMsg[1]); //perform the reserve protocol
				System.out.println(serverResponse);
				SocketUtil.writeToSocket(connectionSocket, serverResponse, SESSION_KEY);
				break;
			// Exit case to handle the empty message received as the client portal exits
			case EXIT:
				username = "";
				SESSION_KEY = "";
				break;
			case PUBLIC_KEY_EXCHANGE:
				KeyExchangeUtil.serverNegotiatePublicKeyExchange(connectionSocket, clientMsg[1]);
				SESSION_KEY = KeyExchangeUtil.serverSessionKeys(connectionSocket);
				break;
			//In error or protocol besides LOGIN/RESERVE send FAILURE reply
			case ERROR:
			default:
				serverResponse = FAILURE;
				System.out.println(serverResponse);
				SocketUtil.writeToSocket(connectionSocket, serverResponse, SESSION_KEY);
			}

		}
	}

	/**
	 * Find timeslots that are free from a list of all timeslots
	 * @param timeslots - the list of time slots read from the file
	 * @return a string of the free times separated by the "BREAK" character, or "NO TIMES AVAILABLE"
	 */
	private static String findAvailableTimes(ArrayList<String> timeslots) {
		String times = "";
		// loop through the time slots, starting with FILE_START
		for (int i = FILE_START; i < timeslots.size(); i++) {
			//timeslots have the format time,availability,reserved by
			String timeslotRow = timeslots.get(i);
			//check the availability portion of the row
			if (AVAILABLE.equals(timeslotRow.split(",")[1])) {
				times = times + timeslotRow.split(",")[0] + BREAK;
			}
		}
		// if not times are added, set NO_TIME instead
		if (times.equals("")) {
			times = NO_TIME;
		}
		return times;
	}

	/**
	 * Perform the RESERVE protocol. 
	 * Attempt to reserve a time, if the time is unavailable return a FAILURE
	 * If the time is available update the availability of the time and record the user
	 * it is reserved to
	 * @param timeslot - the time to reserve
	 * @return - Formatted reply to client. on SUCCESS return SUCCESS and updated available times, on FAILURE
	 * return FAILURE the failure reason and a list of available times.
	 */
	private static String reserveTime(String timeslot) {

		System.out.println("Timeslot: " + timeslot);
		// Read all time slots from the file
		ArrayList<String> timeslots = FileUtil.readFileToArray(RESERVATIONS);
		// Find which times are available
		String availableTimes = findAvailableTimes(timeslots);
		String[] timesArray = availableTimes.split(BREAK); // Take string of available times into an array
		boolean hasTimeAvailable = false;
		// Iterate over available times to see if requested time is available
		for (int i = 0; i < timesArray.length; i++) {
			System.out.println("Checking against: " + timesArray[i]);
			// If time is available break out of loop, and indicate a time is available
			if (timeslot.equals(timesArray[i])) {
				hasTimeAvailable = true;
				break;
			}
		}
		// if requested time is available reserve it, otherwise reply failure
		if (hasTimeAvailable) {
			//format new row for timeslot
			String newSlot = timeslot + "," + BOOKED + "," + username;
			// Find which row to update
			int timeslotId = timeslots.indexOf(timeslot + "," + AVAILABLE + ",");
			if (timeslotId != -1) {
				timeslots.set(timeslotId, newSlot);
				FileUtil.writeToFile(RESERVATIONS, timeslots);
				return SUCCESS + BREAK + findAvailableTimes(timeslots);
			}
			return FAILURE + BREAK + "Error Reserving timeslot" + BREAK + findAvailableTimes(timeslots);
		} else {
			return FAILURE + BREAK + "Unable to reserve time" + BREAK + findAvailableTimes(timeslots);
		}

	}

	/**
	 * Perform Login Protocol
	 * Check username agaisnt list of usernames, then verify password for that user
	 * @param credentials - username and password in format "username:USERNAME password:PASSWORD"
	 * @return SUCCESS or FAILURE followed by the BREAK character. In the case of failure a reson as well.
	 */
	private static String loginUser(String credentials) {
		// Assume failure, require a match to change to SUCCESS
		String response = FAILURE + BREAK + "Incorrect Username or Password";

//		ArrayList<String> users = FileUtil.readFileToArray(USERS_HASH_SALT); // read list of username and passwords from file

		// pull out the username and password to check
		String clientUser = credentials.split(" ")[0];
		String clientPass = credentials.split(" ")[1];
		String user = clientUser.split(":")[1];
		String password = clientPass.split(":")[1];
		// iterate through all users
		for (int i = FILE_START; i < users.size(); i++) {
			String userRow = users.get(i);
			// first check for a username match, then a password match
			if (user.equals(userRow.split(",")[0])) {
				if (BCrypt.checkpw(password, userRow.split(",")[1])) {
					response = SUCCESS + BREAK;
					 username = user; // set last logged in user
					break;
				}
			}
		}

		return response;
	}


	/**
	 * Start a socket to listen on the designated port
	 * @param PORT - port to listen on
	 * @return the ServerSocket listening on the port
	 */
	private static ServerSocket startServer(int PORT) {

		ServerSocket listener = null;
		// Try to start a listener, if unsuccessful exit then stop the program
		try {
			listener = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Unable to start server on port: " + PORT);
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Server Started and listening at " + listener.getLocalSocketAddress());

		return listener;
	}

}
