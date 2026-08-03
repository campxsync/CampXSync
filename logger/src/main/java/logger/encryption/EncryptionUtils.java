package logger.encryption;

import logger.constants.SecurityConstants;
import logger.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class EncryptionUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EncryptionUtils() {
        // Prevent instantiation
    }

    /**
     * Encrypts the plain text using the provided secret key.
     * The key must be 32 bytes (256 bits) for AES-256. If a shorter string is provided,
     * it will be padded or hashed to form a valid 256-bit key.
     */
    public static String encrypt(String plainText, String secretKey) {
        if (plainText == null) {
            return null;
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new EncryptionException("Encryption key must not be null or empty");
        }

        try {
            byte[] iv = new byte[SecurityConstants.GCM_IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(SecurityConstants.TRANSFORMATION_AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(SecurityConstants.GCM_TAG_LENGTH_BITS, iv);
            SecretKeySpec keySpec = deriveKey(secretKey);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext: [12 bytes IV] + [encrypted content]
            byte[] encryptedBytes = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedBytes, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedBytes, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts the Base64 encoded cipher text using the provided secret key.
     */
    public static String decrypt(String base64CipherText, String secretKey) {
        if (base64CipherText == null) {
            return null;
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new EncryptionException("Decryption key must not be null or empty");
        }

        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(base64CipherText);
            if (encryptedBytes.length < SecurityConstants.GCM_IV_LENGTH_BYTES) {
                throw new EncryptionException("Ciphertext is too short (missing IV)");
            }

            byte[] iv = new byte[SecurityConstants.GCM_IV_LENGTH_BYTES];
            System.arraycopy(encryptedBytes, 0, iv, 0, iv.length);

            int ciphertextLength = encryptedBytes.length - iv.length;
            byte[] ciphertext = new byte[ciphertextLength];
            System.arraycopy(encryptedBytes, iv.length, ciphertext, 0, ciphertextLength);

            Cipher cipher = Cipher.getInstance(SecurityConstants.TRANSFORMATION_AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(SecurityConstants.GCM_TAG_LENGTH_BITS, iv);
            SecretKeySpec keySpec = deriveKey(secretKey);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            byte[] decryptedBytes = cipher.doFinal(ciphertext);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Derives a 256-bit SecretKeySpec from the provided string.
     * Uses SHA-256 to consistently hash the secretKey to 32 bytes.
     */
    private static SecretKeySpec deriveKey(String key) throws Exception {
        java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, SecurityConstants.ALGORITHM_AES);
    }
}
