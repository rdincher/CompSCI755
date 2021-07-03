package utils;

import static utils.Constants.BREAK;
import static utils.Constants.ERROR;
import static utils.Constants.EXIT;
import static utils.Constants.NEWLINE;
import static utils.Constants.PUBLIC_KEY_EXCHANGE;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketUtil {

	/**
	 * Write a message to a socket
	 * 
	 * @param socket
	 *            - the open socket to communicate on
	 * @param message
	 *            - the message to send
	 * @param key
	 *            - the key to use for symmetric encryption
	 */
	public static void writeToSocket(Socket socket, String message, String key) {
		System.out.println("Replying...");
		// Open stream and reply
		try {
			DataOutputStream toSocket = new DataOutputStream(socket.getOutputStream());
			// Need to add a terminating new line so the stream reader can
			// process
			if (key.isEmpty()) {
				toSocket.writeBytes(message + NEWLINE);
			} else {
				toSocket.writeBytes(AESUtil.encrypt(message, key) + NEWLINE);
			}
			toSocket.flush();
		} catch (IOException e) {
			System.out.println("Unable to write to socket");
			e.printStackTrace();
			// System.exit(1);
		}
	}

	/**
	 * Read messages from the socket.
	 * 
	 * Handles the established protocol
	 * 
	 * MESSAGE TYPE::message body
	 * 
	 * The Message type cane be one of: login, success, failure, exchange,
	 * reserve, error
	 * 
	 * Message body may consist of further breaks (::)
	 * 
	 * @param client
	 *            - the open socket to read from
	 * @return - an array with the first element the Message type and second
	 *         element the message body with breaks(::) replaced with an space("
	 *         ")
	 */
	/*
	 * Message Protocol
	 * 
	 * method::messageBody
	 * 
	 * LOGIN::username:xxxx::password:xxxx
	 * 
	 * RESERVE::time:xx
	 * 
	 * SUCCESS::times (e.x. SUCCESS 1 2 3 4)
	 * 
	 * FAILURE::reason
	 * 
	 * ERROR::reason
	 * 
	 * EXCHANGE::algorithm:xxx::length:xx::padding:xxx
	 */
	public static String[] readMessageFromSocket(Socket socket, String key) {
		String[] message = { "", "" };
		System.out.println("Reading Message from socket ");

		try {
			// Open input stream read and decrypt message
			BufferedReader fromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg = fromSocket.readLine();
			
			if (msg == null) {
				message[0] = EXIT;
				return message;
			}
			if (PUBLIC_KEY_EXCHANGE.equalsIgnoreCase(msg.split("::")[0])) {
				message[0] = PUBLIC_KEY_EXCHANGE;
				message[1] = msg;
				return message;
			}
			String socketMsg = AESUtil.decrypt(msg, key);
			// format message by splitting on break character
			String[] formattedMsg = socketMsg.split(BREAK);

			System.out.println("Reading Message: " + formattedMsg[0]); // log
																		// the
																		// protocol
																		// of
																		// the
																		// message
			message[0] = formattedMsg[0];
			String body = "";
			// if there was no protocol then the socket disconnected
			if (formattedMsg[0].isEmpty()) {
				message[0] = EXIT;
			}
			// loop through remaining message items to format the message body
			for (int i = 1; i < formattedMsg.length; i++) {
				body = body + " " + formattedMsg[i];
			}
			body = body.trim(); // remove leading whitespace
			message[1] = body;

		} catch (Exception e) {
			System.out.println("Unable to read message");
			e.printStackTrace();
			message[0] = ERROR;
			message[1] = "Unable to read message";
		}

		return message;
	}
}
