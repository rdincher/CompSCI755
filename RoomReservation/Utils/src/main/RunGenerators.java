package main;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

import generator.RSAKeyPairGenerator;
import utils.FileUtil;
import utils.HashUtil;

public class RunGenerators {

	private static RSAKeyPairGenerator keyPairGenerator;

	// Method to generate a key pair
	// Takes a scanner to read input from keyboard
	private static void generateKeys(Scanner keys) throws InvalidKeySpecException, IOException {
		int keySize = 1024;
		System.out.println("Enter a Key size (1024, 2048, 3072)"); // Chose the
																	// RSA Key
																	// Size
		keySize = keys.nextInt();
		keys.nextLine();

		try {
			keyPairGenerator = new RSAKeyPairGenerator(keySize);
			// write generated keys to files

			FileUtil.writeToFile("server.pub", Base64.getEncoder().encodeToString(keyPairGenerator.getPublicKey().getEncoded()));
			FileUtil.writeToFile("server.private",
					Base64.getEncoder().encodeToString(keyPairGenerator.getPrivateKey().getEncoded()));
			System.out.println("Keys written to file");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Unable to Generate Keys: " + e);
			e.printStackTrace();
		}
	}

	private static void generateHashes(Scanner keys){

		System.out.println("Enter a path of password file");
		String inputFile = keys.nextLine();
		System.out.println("Enter name for hashed password file");
		String outputFile = keys.nextLine();
		HashUtil.hashWithBcrypt(inputFile, outputFile);
	}
	
	private static int prompt(Scanner keyboard) {

		System.out.println("What would you like to do:");
		System.out.println("1 Generate keys");
		System.out.println("2 Generate Hashes");
		System.out.println("3 Exit");
		System.out.println("Enter the number for your choice:");
		String userChoice = keyboard.nextLine();
		return Integer.parseInt(userChoice);
	}

	public static void main(String[] args) throws InvalidKeySpecException, IOException {
		Scanner keyboard = new Scanner(System.in);
		boolean exit = false;
		while (!exit) {
			int command = prompt(keyboard);
			switch (command) {
			case 1:
				generateKeys(keyboard);
				break;
			case 2:
				// Encrypt File
//				HashUtil.userPassWithSalt(USERS, USERS_HASH_SALT);
				generateHashes(keyboard);
				break;
			case 3:
				exit = true;
				break;
			default:
				System.out.println("Please enter a number 1 - 5");
				break;
			}
		}

		keyboard.close();
	}
}
