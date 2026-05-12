package com.duck.model.authentication;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for AES encryption and decryption.
 */
public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "DuckSecurityKey1"; 

    /**
     * Encrypts the given string using AES and returns a Base64-
     * encoded result.
     * @param data the plaintext to encrypt
     * @return the Base64-encoded ciphertext
     */
    public static String encrypt(String data) throws Exception {
        // 1. prepare key
        SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        // 2. initialize cipher 
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        // 3. encrypt
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        // 4. encode
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts a Base64-encoded AES ciphertext back to plaintext.
     * @param encryptedData the Base64-encoded ciphertext
     * @return the decrypted plaintext
     */
    public static String decrypt(String encryptedData) throws Exception {
        // 1. prepare key
        SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        // 2. initialize cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, spec);
        // 3. decode
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        // 4. decrypt
        return new String(cipher.doFinal(decodedBytes));
    }
}