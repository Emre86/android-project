package homesweethome.emre.mytracker;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by emre on 26/02/17.
 */
public class Asym {

    //private KeyPair kp;
    private static String publicKeyBytesBase64;
    private static String privateKeyBytesBase64;

    public Asym(){
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA"); // Original
            kpg.initialize(1024, new SecureRandom()); // 512 is the keysize.//try 1024 biit
            KeyPair kp = kpg.genKeyPair();

            PublicKey publicKey = kp.getPublic();
            byte[] publicKeyBytes = publicKey.getEncoded();
            publicKeyBytesBase64 = new String(Base64.encode(publicKeyBytes, Base64.DEFAULT));

            PrivateKey privateKey = kp.getPrivate();
            byte[] privateKeyBytes = privateKey.getEncoded();
            privateKeyBytesBase64 = new String(Base64.encode(privateKeyBytes, Base64.DEFAULT));

        }
        catch (NoSuchAlgorithmException nae){
            nae.printStackTrace();
        }
    }


    public Asym(String publicKeyBytesBase64,String privateKeyBytesBase64){
        this.publicKeyBytesBase64 = publicKeyBytesBase64;
        this.privateKeyBytesBase64 = privateKeyBytesBase64;
    }

    public String getPublicKeyAsym(){
        return publicKeyBytesBase64 ;
    }

    public String getPrivateKeyAsym(){
        return privateKeyBytesBase64 ;
    }

    public String encryptKeyAsim(String dataToEncrypt){
        String encrypted = encryptRSAToString(dataToEncrypt);
        return encrypted ;
    }

    public String decryptKeyAsim(String encrypted) {
        String decrypted = decryptRSAToString(encrypted);
        return decrypted ;
    }

    public static String encryptRSAToString(String clearText) {
        String encryptedBase64 = "";
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(publicKeyBytesBase64.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePublic(keySpec);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(clearText.getBytes("UTF-8"));
            encryptedBase64 = new String(Base64.encode(encryptedBytes, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedBase64.replaceAll("(\\r|\\n)", "");
    }


    public static String decryptRSAToString(String encryptedBase64) {

        String decryptedString = "";
        try {
            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            //KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKey.trim().getBytes(), Base64.DEFAULT));
            KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyBytesBase64.trim().getBytes(), Base64.DEFAULT));
            Key key = keyFac.generatePrivate(keySpec);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            // encrypt the plain text using the public key
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            decryptedString = new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptedString;
    }

}
