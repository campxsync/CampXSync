package logger.encryption;

import logger.constants.SecurityConstants;
import logger.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public final class EncryptionUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EncryptionUtils() {
        // Prevent instantiation
    }

    /**
     * Encrypts the plain text using AES-256-GCM with PBKDF2 key derivation.
     *
     * Key derivation: PBKDF2WithHmacSHA256, 310,000 iterations (NIST SP 800-132 recommendation).
     * The 12-byte IV is generated randomly per encryption and prepended to the ciphertext.
     * The IV is also used as the PBKDF2 salt — same IV always produces the same derived key
     * for a given secret, allowing decryption without storing an extra salt.
     *
     * Output format: Base64([12-byte IV] + [AES-GCM ciphertext + 16-byte auth tag])
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

            SecretKeySpec keySpec = deriveKey(secretKey, iv);
            Cipher cipher = Cipher.getInstance(SecurityConstants.TRANSFORMATION_AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(SecurityConstants.GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Output: [12-byte IV] + [ciphertext + 16-byte GCM auth tag]
            byte[] encryptedBytes = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedBytes, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedBytes, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext produced by {@link #encrypt}.
     * Extracts the IV from the first 12 bytes, re-derives the key via PBKDF2, then decrypts.
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

            SecretKeySpec keySpec = deriveKey(secretKey, iv);
            Cipher cipher = Cipher.getInstance(SecurityConstants.TRANSFORMATION_AES_GCM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(SecurityConstants.GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            byte[] decryptedBytes = cipher.doFinal(ciphertext);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Derives a 256-bit AES key from the provided password using PBKDF2WithHmacSHA256.
     *
     * PBKDF2 with 310,000 iterations (NIST SP 800-132 / OWASP recommendation 2023) reduces
     * brute-force guessing speed from ~10 billion/sec (SHA-256) to ~32,000/sec on modern GPUs —
     * a 300,000× improvement in resistance to offline dictionary attacks.
     *
     * The IV is used as the PBKDF2 salt. Since each encryption operation uses a unique random IV,
     * each ciphertext block has a unique derived key — preventing key reuse attacks.
     *
     * @param password the secret key string
     * @param salt the IV bytes used as PBKDF2 salt (extracted from or assigned during encryption)
     */
    private static SecretKeySpec deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SecurityConstants.PBKDF2_ALGORITHM);
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                SecurityConstants.PBKDF2_ITERATIONS,
                SecurityConstants.AES_KEY_SIZE_BITS
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), SecurityConstants.ALGORITHM_AES);
    }
}
