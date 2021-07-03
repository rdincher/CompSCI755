package portal;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import utils.KeyExchangeUtil;
import utils.SocketUtil;

import static utils.Constants.*;

/**
 * Client Portal for reserving timeslots in a meeting room
 * 
 * @author Ryan
 *
 */
public class ClientPortal {
	// Global Constants for use

	private static String SESSION_KEY = "";

	/**
	 * Main method to run the client portal
	 * 
	 * @param args
	 * @throws Exception
	 *             - IOExceptions from connecting to/reading from a socket
	 */
	public static void main(String[] args) throws Exception {

		
		Socket server = connectToServer(HOST, PORT);
		KeyExchangeUtil.clientNegotiatePublicKeyExchange(server);
		
		SESSION_KEY = KeyExchangeUtil.clientSessionKeys(server);
		
		server.close();

		runClient();

	}

	/**
	 * Logic to handle the operation of the portal
	 * 
	 * @throws Exception
	 *             - IOExceptions from connecting to/reading from a socket
	 */
	private static void runClient() throws Exception {
		Scanner keyboard = new Scanner(System.in); // Create scanner to read
													// input from command line
		String msg = ""; // create variable to hold server response

		// Initialize variables, no user logged in, no login attempts, and not
		// exiting
		boolean loggedIn = false;
		boolean exit = false;
		int loginAttempt = 0;

		// Loop until the user wishes to exit
		while (!exit) {
			Socket server = connectToServer(HOST, PORT); // open socket to the
															// server
			// if the user is not logged in require a login first
			if (!loggedIn) {
				String[] attempt = login(server, keyboard, loginAttempt);
				// on successful login, format and show response from server,
				// toggle the user login
				// if unsuccessful increment login attempts and prompt to retry
				if (attempt[0].equals(SUCCESS)) {
					loggedIn = true;
					String[] timeSlots = attempt[1].split(" ");
					printTimes(timeSlots);
				} else {
					System.out.println("Please Try again");
					loginAttempt++;
				}
			} else { // if the user is already logged in the only option is to
						// reserve a time

				msg = reserveTime(server, keyboard); // attempt to reserve a
														// time
				// if the user wished to exit toggle "exit" to end the loop
				if (msg.equals(EXIT)) {
					exit = true;
				}

			}

			disconnect(server); // close the socket at the completion of each
								// loop
		}
		System.out.println("Exiting...");
	}

	/**
	 * Reserve a timeslot from the available timeslots
	 * 
	 * @param server
	 *            - the socket connected to the server
	 * @param keyboard
	 *            - the input source
	 * @return The empty string or "EXIT" if the user wished to exit
	 * @throws Exception
	 *             - IOException from the socket or user input
	 */
	private static String reserveTime(Socket server, Scanner keyboard) throws Exception {

		// Prompt user to enter a time to reserve or exit the portal
		System.out.println("Enter Time you would like to reserve or type EXIT: ");
		String time = keyboard.nextLine();

		// when user exits return exit, otherwise attempt RESERVE protocol
		if (time.equalsIgnoreCase(EXIT)) {
			return EXIT;
		} else {
			// format the reserve protocol and encrypt. A new line is added at
			// the end of the message
			// so that the server input stream reads the message
			SocketUtil.writeToSocket(server, RESERVE + BREAK + time, SESSION_KEY);
			System.out.println("......");
			// Decrypt and read reply from server
			
			String[] serverReply = SocketUtil.readMessageFromSocket(server, SESSION_KEY);

			System.out.println(serverReply[0]); // display SUCCESS/FAILURE of
													// reservation
			String[] times = serverReply[1].split(" ");
			// if reservation failed print reason to user, and then reprint the
			// times
			// otherwise print the new available times
			if (serverReply[0].equals(FAILURE)) {
				System.out.println(serverReply[1]);

				printTimes(times);
			} else {
				printTimes(times);
			}
			return "";
		}

	}

	/**
	 * Print the timeslots for the user
	 * 
	 * @param times
	 *            - Array of possible times, first item is skipped
	 */
	private static void printTimes(String[] times) {
		// If there are no time slots inform user and exit portal
		if (times[1].equals(NO_TIME)) {
			System.out.println(NO_TIME);
			System.exit(0);
		}
		// loop through and print out the avialable times
		System.out.println("Available Timeslots:");
		for (int i = 1; i < times.length; i++) {
			System.out.println(times[i]);
		}
	}

	/**
	 * Perform the login protocol Will attempt at least one log in before
	 * exiting if the attempts is 5 or more
	 * 
	 * @param server
	 *            - the socket connected to the server
	 * @param keyboard
	 *            - input source
	 * @param attempt
	 *            - The number of login attempts already made
	 * @return - The message from the server
	 * @throws Exception
	 *             - IO Exception from Socket or input
	 */
	private static String[] login(Socket server, Scanner keyboard, int attempt) throws Exception {

		// Prompt user to enter login credentials
		System.out.println("Enter Username: ");
		String username = keyboard.nextLine();
		System.out.println("Enter Password: ");
		String password = keyboard.nextLine();

		// Inform user of the login attempt number
		System.out.println("Login Attempt " + (attempt + 1) + "/5");
		// Write format and encrypt login protocol,
		// write to server with newline ("\n") added so that the server can read
		// the line
		SocketUtil.writeToSocket(server, LOGIN + BREAK + "username:" + username + BREAK + "password:" + password, SESSION_KEY);

		String serverReply[] = SocketUtil.readMessageFromSocket(server, SESSION_KEY);
		System.out.println("......");
		String status = serverReply[0];
		// If the login failed print the reason, then check if user exceeded
		// login attempts
		if (status.equals(FAILURE)) {
			attempt++;
			System.out.println(serverReply[1]);
			if (attempt >= 5) {
				System.out.println("Too many login attempts");
				System.exit(0); // exit program in there were to many login
								// attempts
			}
		}

		return serverReply;
	}

	/**
	 * Open a socket on the specified host and port
	 * 
	 * @param host
	 *            - Host to connect to
	 * @param port
	 *            - port to connect to
	 * @return - connected socket on host:port
	 */
	private static Socket connectToServer(String host, int port) {

		System.out.print("Connecting to server...");
		Socket clientSocket = null;
		// Create new socket, if unable to connect exit the program
		try {
			clientSocket = new Socket(host, port); // create a new socket
		} catch (Exception e) {
			System.out.println(" Unable to Connect to Server on host: " + host + " and port: " + port);
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(" Connected.");
		return clientSocket;
	}

	/**
	 * Close a open socket
	 * 
	 * @param server
	 *            - socket to close
	 */
	private static void disconnect(Socket server) {
		// Attempt disconnect from server, if failed print error message
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("Error disconnecting from Server: " + server.getRemoteSocketAddress());
			e.printStackTrace();
		}
	}
}
