package utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;

import generator.BCrypt;
import generator.SHA512;
import static utils.Constants.*;

public class HashUtil {

	public static void convertUserPasswordFile(String inputFile, String outputFile) {

		// (2) Write a program to convert your password file into a file with
		// passwords hashed using SHA 512
		ArrayList<String> usersPlain = FileUtil.readFileToArray(inputFile); // read list of username
														// and passwords from
														// file
		ArrayList<String> usersHash = new ArrayList<String>();
		usersHash.add(usersPlain.get(0)); // Add the header row

		// iterate through all users
		for (int i = FILE_START; i < usersPlain.size(); i++) {
			String userRow = usersPlain.get(i);
			String newRow = userRow.split(",")[0];
			newRow = newRow + ",";
			String hashPassword = SHA512.hashThisString(userRow.split(",")[1]);
			newRow = newRow + hashPassword;

			usersHash.add(newRow);
		}

		FileUtil.writeToFile(outputFile, usersHash);
	}
	
	public static void hashWithBcrypt(String inputFile, String outputFile){
		
		ArrayList<String> usersPlain = FileUtil.readFileToArray(inputFile); // read list of username
		// and passwords from
		// file
		ArrayList<String> usersHash = new ArrayList<String>();
		usersHash.add(usersPlain.get(0)); // Add the header row

		// iterate through all users
		for (int i = FILE_START; i < usersPlain.size(); i++) {
			
			String salt = BCrypt.gensalt();

			String userRow = usersPlain.get(i);
			String newRow = userRow.split(",")[0];
			newRow = newRow + ",";

			String hashPassword = BCrypt.hashpw(userRow.split(",")[1], salt);
			newRow = newRow + hashPassword + "," + salt;

			usersHash.add(newRow);
		}

		FileUtil.writeToFile(outputFile, usersHash);
	}
	
	public static void userPassWithSalt(String inputFile, String outputFile) {
		/*
		 * (7) Next generate a 32-character salt for each of your 50 passwords
		 * and generate the corresponding hash values. Write the user name, salt
		 * value, and hashed password+salt value into a new file (let’s call it
		 * the hashed and salted password file). For generating hashed passwords
		 * with salt, refer to https://crackstation.net/hashing-security.htm
		 * (Links to an external site.)
		 */

		SecureRandom random = new SecureRandom();

		ArrayList<String> usersPlain = FileUtil.readFileToArray(inputFile); // read list of username
		// and passwords from
		// file
		ArrayList<String> usersHash = new ArrayList<String>();
		usersHash.add(usersPlain.get(0)); // Add the header row

		// iterate through all users
		for (int i = FILE_START; i < usersPlain.size(); i++) {
			byte bytes[] = new byte[32];
			random.nextBytes(bytes);
			String salt = Base64.getEncoder().encodeToString(bytes);

			String userRow = usersPlain.get(i);
			String newRow = userRow.split(",")[0];
			newRow = newRow + ",";

			String hashPassword = SHA512.hashThisString(salt + userRow.split(",")[1]);
			newRow = newRow + hashPassword + "," + salt;

			usersHash.add(newRow);
		}

		FileUtil.writeToFile(outputFile, usersHash);

	}
}
