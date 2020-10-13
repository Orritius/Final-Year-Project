package albumcredibilityapplication.core;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public abstract class PasswordHash {

    /**
     * Generates a random salt.
     * @return a randomly generated salt for password security
     */
    public static byte[] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /**
     *
     * @param password
     * @return A byte array containing the hashed password with the salt.
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static byte[] createHashedPassword(char[] password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec spec = new PBEKeySpec(password, salt, 65536, 512);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        return factory.generateSecret(spec).getEncoded();
    }

    /**
     *
     * @param bytes
     * @return a String representation of the byte array in hexadecimal format
     */
    public static String byteToHex(byte[] bytes){
        String hexString = Hex.encodeHexString(bytes);
        return hexString;
    }

    /**
     *
     * @param hexString
     * @return a byte array representation of the hexadecimal string.
     */
    public static byte[] hexToByte(String hexString){
        byte[] bytes = new byte[0];
        try{
            bytes = Hex.decodeHex(hexString);
            return bytes;
        } catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }
}
