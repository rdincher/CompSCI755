package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import static utils.Constants.*;

public class KeyExchangeUtil {

	private static ArrayList<String> serverNonceValues = new ArrayList<String>();
	private static ArrayList<String> clientNonceValues = new ArrayList<String>();
	
	public static void clientNegotiatePublicKeyExchange(Socket server) throws Exception {
		/*
		 * Connect to server exchange Algo: RSA key length: 2048 padding: OAEP
		 * 
		 * Server reply: exchange Algo: key length: padding:
		 * 
		 * client reply send pub key
		 * 
		 * server send pub key
		 */

		String startExchange = "EXCHANGE::algorithm:RSA::length:2048::padding:OAEP";

		SocketUtil.writeToSocket(server, startExchange, "");
		String serverReply[] = SocketUtil.readMessageFromSocket(server, "");
		String[] details = serverReply[1].split("::");
		System.out.println(serverReply[1]);
		ALGORITHM = details[1].split(":")[1];
		PADDING = details[3].split(":")[1];
		if (!ALGORITHM.equals("RSA")) {
			System.out.println("Could not agree on Key algorithm");
			System.exit(1);
		}
		if (!(PADDING.equals("OAEP") || PADDING.equals("PKCS"))) {
			System.out.println("Could not agree on Padding");
			System.exit(1);
		}

		String clientPubKey = FileUtil.readFileToString(CLIENT_PUB);
		SocketUtil.writeToSocket(server, "EXCHANGE::key:"+ clientPubKey, "");
		String serverPubKeyResponse[] = SocketUtil.readMessageFromSocket(server, "");
		String serverPubKey[] = serverPubKeyResponse[1].split("::");
		System.out.println(serverPubKey[1].split(":")[1]);
		 FileUtil.writeToFile(SERVER_PUB, serverPubKey[1].split(":")[1]);
		System.out.println("Public Keys Exchanged");
	}

	public static String clientSessionKeys(Socket server) throws Exception {
		/*
		 * Now the client and the server start a session key exchange as in Fig.
		 * 15.5 (slide 60 in Module 5 slides). Client send Nonce & ID Server
		 * reply nonce1 and nonce2 Client send back nonce2 Client send
		 * authenticated session key
		 */
		String servPubKey = FileUtil.readFileToString(SERVER_PUB);
		String clientPrivKey = FileUtil.readFileToString(CLIENT_PRIV);

		String paddingMode = PADDING.equalsIgnoreCase("PKCS") ? PKCS : OAEP;

		Random random = new Random();
		int clientNonce = random.nextInt(50);

		String sessionKey = "A23BN67DFIONMTN5";
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
		String startExchange = "SESSION::nonce:" + String.valueOf(clientNonce) + "::id:client";
		String encryptedString = encryptToString(startExchange, servPubKey, paddingMode);

		SocketUtil.writeToSocket(server, encryptedString, "");

		String serverData = fromServer.readLine();
		String serverReply = RSAUtil.decrypt(serverData, clientPrivKey, paddingMode);
		;
		// SESSION::nonce:CLIENT_NONCE::nonce:SERVER_NONCE
		String[] details = serverReply.split("::");
		String returnNonce = details[1].split(":")[1];
		String serverNonce = details[2].split(":")[1];
		if (!String.valueOf(clientNonce).equals(returnNonce)) {
			System.out.println("Incorrect Nonce retruned from 'Server'");
			System.exit(1);
		}
		if(serverNonceValues.contains(serverNonce)){
			System.out.println("Repeated packet");
			System.exit(1);
		}
		serverNonceValues.add(serverNonce);
		String returnServerNonce = "SESSION::nonce:" + serverNonce;
		encryptedString = encryptToString(returnServerNonce, servPubKey, paddingMode);
		SocketUtil.writeToSocket(server, encryptedString, "");

		Signature signature = Signature.getInstance("SHA256WithRSA");
		signature.initSign(RSAUtil.getPrivateKey(clientPrivKey));
		signature.update(sessionKey.getBytes());
		byte[] signatureBytes = signature.sign();
		String sig = new String(Base64.getEncoder().encodeToString(signatureBytes));

		String sendSessionKey = "SESSION::session_key:" + sessionKey;
		encryptedString = encryptToString(sendSessionKey, servPubKey, paddingMode);
		SocketUtil.writeToSocket(server, encryptedString + "::" + sig, "");
		System.out.println("Exchanged Session Key");
		return sessionKey;
	}

	public static void serverNegotiatePublicKeyExchange(Socket client, String msg) throws Exception {
		/*
		 * Connect to server exchange Algo: RSA key length: 2048 padding: OAEP
		 * 
		 * Server reply: exchange Algo: key length: padding:
		 * 
		 * client reply send pub key
		 * 
		 * server send pub key
		 */
		String[] details = msg.split("::");
		ALGORITHM = details[1].split(":")[1];
		PADDING = details[3].split(":")[1];
		if (!ALGORITHM.equals("RSA")) {
			System.out.println("Could not agree on Key algorithm");
			System.exit(1);
		}
		if (!(PADDING.equals("OAEP") || PADDING.equals("PKCS"))) {
			System.out.println("Could not agree on Padding");
			System.exit(1);
		}
		String startExchange = "EXCHANGE::algorithm:" + ALGORITHM + "::length:2048::padding:" + PADDING;

		SocketUtil.writeToSocket(client, startExchange, "");

		String clientPubKeyResponse[] = SocketUtil.readMessageFromSocket(client, "");
		String clientPubKey[] = clientPubKeyResponse[1].split("::");
		FileUtil.writeToFile(CLIENT_PUB, clientPubKey[1].split(":")[1]);

		String serverPubKey = FileUtil.readFileToString(SERVER_PUB);
		SocketUtil.writeToSocket(client, "EXCHANGE::key:"+ serverPubKey, "");

		System.out.println("Public Keys Exchanged");
	}

	public static String serverSessionKeys(Socket client) throws Exception {
		/*
		 * Now the client and the server start a session key exchange as in Fig.
		 * 15.5 (slide 60 in Module 5 slides). Client send Nonce & ID Server
		 * reply nonce1 and nonce2 Client send back nonce2 Client send
		 * authenticated session key
		 */

		String clientPubKey = FileUtil.readFileToString(CLIENT_PUB);
		String servPrivKey = FileUtil.readFileToString(SERVER_PRIV);

		String paddingMode = PADDING.equalsIgnoreCase("PKCS") ? PKCS : OAEP;

		Random random = new Random();
		int serverNonce = random.nextInt(50);

		BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

		String clientData = fromClient.readLine();
		String clientMsg = RSAUtil.decrypt(clientData, servPrivKey, paddingMode);
		
		String[] details = clientMsg.split("::");
		String clientNonce = details[1].split(":")[1];
		// String clientId = details[2].split(":")[1];
		if(clientNonceValues.contains(clientNonce)){
			System.out.println("Repeated packet");
			return "";
		}
		clientNonceValues.add(clientNonce);
		String validateNonce = "SESSION::nonce:" + clientNonce + "::nonce:" + String.valueOf(serverNonce);
		String encryptedString = encryptToString(validateNonce, clientPubKey, paddingMode);
		SocketUtil.writeToSocket(client, encryptedString, "");

		String clientReply = fromClient.readLine();
		clientMsg = RSAUtil.decrypt(clientReply, servPrivKey, paddingMode);
		
		details = clientMsg.split("::");
		String returnNonce = details[1].split(":")[1];

		if (!String.valueOf(serverNonce).equals(returnNonce)) {
			System.out.println("Incorrect Nonce retruned from 'Client'");
			return "";
		}
		String getSessionKey = fromClient.readLine();
		String withSig[] = getSessionKey.split("::");
		String sessionKeyLine = RSAUtil.decrypt(withSig[0], servPrivKey, paddingMode);
		// SESSION::session_key:KEY_VALUE
		String sessionKey = sessionKeyLine.split("::")[1].split(":")[1];
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initVerify(RSAUtil.getPublicKey(clientPubKey));
		sign.update(sessionKey.getBytes());

		// Verifying the signature
		boolean verified = sign.verify(Base64.getDecoder().decode(withSig[1].getBytes()));
		if (verified) {
			System.out.println("Exchanged Session Key");
			return sessionKey;
		}
		return "";
	}

	private static String encryptToString(String msg, String pubKey, String padding) throws Exception {
		return Base64.getEncoder().encodeToString(RSAUtil.encrypt(msg, padding, pubKey));
	}
}
