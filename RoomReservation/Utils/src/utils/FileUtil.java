package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class FileUtil {

	/**
	 * Read in a file and return it as an array of lines
	 * @param filename - File to read
	 * @return ArrayList with each item being a line of the file
	 */
	public static ArrayList<String> readFileToArray(String filename) {

		ArrayList<String> fileData = new ArrayList<String>(); // Initialize an empty array list
		try {
			//Create file and read all lines
			File myObj = new File(filename);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				fileData.add(myReader.nextLine());

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return fileData;
	}
	
	/**
	 * Read in a file and return it as an array of lines
	 * 
	 * @param filename
	 *            - File to read
	 * @return ArrayList with each item being a line of the file
	 */
	public static String readFileToString(String filename) {

		byte[] fileData = null; // Initialize an empty
		try {

			Path myPath = Paths.get(filename);
			fileData = Files.readAllBytes(myPath);

		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return new String(fileData, StandardCharsets.UTF_8);
	}

	/**
	 * Write out lines to a file
	 * @param filename - File to overwrite
	 * @param data - the data to write to the file as an ArrayList of lines
	 */
	public static void writeToFile(String filename, ArrayList<String> data) {
		try {
			// Open file and write each line adding the necessary new line character
			FileWriter myWriter = new FileWriter(filename);
			for (int i = 0; i < data.size(); i++) {
				myWriter.write(data.get(i) + "\n");
			}
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Write out lines to a file
	 * 
	 * @param filename
	 *            - File to overwrite
	 * @param data
	 *            - the data to write to the file
	 */
	public static void writeToFile(String filename, String data) {
		try {
			// Open file and write each line adding the necessary new line
			// character
			FileWriter myWriter = new FileWriter(filename);
			myWriter.write(data);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	// Encrypt a file, takes a scanner to read from keyboard
	public static void encryptFile(String filepath, String key) {

		String fileData = FileUtil.readFileToString(filepath);

		String encryptedString = "";
		try {
			encryptedString = AESUtil.encrypt(fileData, key);
		} catch (Exception e) {
			System.out.println("Unable to encrypt file");
			e.printStackTrace();
		}
		FileUtil.writeToFile("encrypted_" + filepath, encryptedString);
	}
	
	// decrypt a file, takes a scanner to read from keyboard
	public static String decryptAndReadFile(String filepath, String key) {
		String fileData = FileUtil.readFileToString(filepath);

		String decryptedString = "";
		try {
			decryptedString = AESUtil.decrypt(fileData, key);
		} catch (Exception e) {
			System.out.println("Unable to decrypt file");
			e.printStackTrace();
		}
		return decryptedString;
//		FileUtil.writeToFile(filepath.replace("encrypted_", "decrypted_"), decryptedString);
	}
	
}
