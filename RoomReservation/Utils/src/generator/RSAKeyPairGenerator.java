package generator;

import java.security.*;

public class RSAKeyPairGenerator {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // Generate a RSA key pair of length keySize
    public RSAKeyPairGenerator(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

    }

    // Retrieve the private key that was generated
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    //Retrieve the public key that was generated
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    
    
}