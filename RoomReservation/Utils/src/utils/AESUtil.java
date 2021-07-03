package utils;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author Ryan
 *
 */
public class AESUtil {
//    private static final String encryptionKey = "A23BN67DFIONMTN5"; // Key to use with Encryption/Decryption
    private static final String encoding = "UTF8"; //Specify the encoding of the input
    private static final String cipherTransformation = "AES/CBC/PKCS5PADDING"; // Specify cipher to use
    private static final String aesEncryptionAlgorithm = "AES"; // Algorithm for Key gen

    public static void encryptFile(){
    	 
    }
    
    /**
     * Method to encrypt a string with AES
     * @param p - The string to encrypt
     * @return the AES ciphertext for the string p
     */
    public static String encrypt(String p, String sessionKey) {
        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            byte[] key = sessionKey.getBytes("UTF-8");
            // Generate AES key and IV from the given encryption key
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithm);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            // Initialize the cipher to encrypt using the secret and IV
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            //Encrypt the text
            byte[] cipherText = cipher.doFinal(p.getBytes(encoding));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);
        } catch(Exception e) {
            System.err.println("\nEncryption error: " + e);
        }
        return encryptedText;
    }

    /**
     * Method to decrypt a string that was encrypted using AES
     * @param c - The cipher text to decrypt
     * @return The plain text representation of the cipher text
     */
    public static String decrypt(String c, String sessionKey) {
        String decryptedText = "";
        try {
        	//Create the cipher object
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            byte[] key = sessionKey.getBytes("UTF-8");
            //Generate the key and IV to use from the encryption key
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptionAlgorithm);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            // Initialize the cipher to decrypt
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
            Base64.Decoder decoder = Base64.getDecoder();
            // perform the decryption
            byte[] cipherText = decoder.decode(c.getBytes(encoding));
            decryptedText = new String(cipher.doFinal(cipherText), "UTF-8");
        } catch(Exception e) {
            System.out.println("\nDecryption error: " + e);
        }
        return decryptedText;
    }
}