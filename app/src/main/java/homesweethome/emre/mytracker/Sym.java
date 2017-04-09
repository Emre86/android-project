package homesweethome.emre.mytracker;




import android.util.Base64;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by emre on 05/03/17.
 */
public class Sym {

    private String symKey;
    private final static String APP = "MYTRACKER:" ;
    private final static String TAG = "Sym";

    public Sym(){
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            SecretKey secretKey = generator.generateKey();
            symKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            Log.d(TAG,symKey);
        }
        catch (NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
        }
    }

    public Sym(String symKey){
        this.symKey = symKey ;
    }

    public String getSymKey(){
        return symKey;
    }

    public String encrypt(String message){
        //Base64.getDecoder().decode(encodedKey);
        byte [] bSymKey = Base64.decode(symKey,Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(bSymKey, "AES");
        byte[] encrypted = null ;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(message.getBytes());
        }
        catch (NoSuchPaddingException nspe){
            Log.e(TAG,"No Such Padding Exception");
            nspe.printStackTrace();
        }
        catch (NoSuchAlgorithmException nsae){
            Log.e(TAG,"No Such Algorithm");
            nsae.printStackTrace();
        }
        catch (InvalidKeyException ike){
            Log.e(TAG,"Invalid Key Exception");
            StringWriter errors = new StringWriter();
            ike.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        catch (IllegalBlockSizeException ibse){
            Log.e(TAG,"Illegal Block Size Exception");
            ibse.printStackTrace();
        }
        catch (BadPaddingException bpe){
            Log.e(TAG,"Bad Padding Exception");
            bpe.printStackTrace();
        }
        return Base64.encodeToString(encrypted,Base64.DEFAULT);
        //return new String(encrypted);
    }


    public String decrypt(String encrypted) {
        byte[] original = null;
        try {
            byte [] bSymKey = Base64.decode(symKey,Base64.DEFAULT);
            SecretKeySpec skeySpec = new SecretKeySpec(bSymKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            original = cipher.doFinal(Base64.decode(encrypted,Base64.DEFAULT));
        }
        catch (InvalidKeyException ike){
            StringWriter errors = new StringWriter();
            ike.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        catch (NoSuchAlgorithmException nsae){
            StringWriter errors = new StringWriter();
            nsae.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        catch (NoSuchPaddingException nspe){
            StringWriter errors = new StringWriter();
            nspe.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        catch (IllegalBlockSizeException ibse){
            StringWriter errors = new StringWriter();
            ibse.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        catch (BadPaddingException bpe){
            StringWriter errors = new StringWriter();
            bpe.printStackTrace(new PrintWriter(errors));
            Log.e(TAG,errors.toString());
        }
        return new String(original);
    }


}
