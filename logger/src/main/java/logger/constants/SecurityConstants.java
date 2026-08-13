package logger.constants;

public final class SecurityConstants {
    private SecurityConstants() {
        // Prevent instantiation
    }

    // Cryptography Algorithms
    public static final String ALGORITHM_AES = "AES";
    public static final String TRANSFORMATION_AES_GCM = "AES/GCM/NoPadding";
    
    // Key configuration limits
    public static final int AES_KEY_SIZE_BITS = 256;
    public static final int GCM_IV_LENGTH_BYTES = 12;
    public static final int GCM_TAG_LENGTH_BITS = 128;

    // PBKDF2 Key Derivation — replaces SHA-256 for cryptographically strong KDF
    // 310,000 iterations per NIST SP 800-132 and OWASP Password Storage Cheat Sheet (2023)
    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int PBKDF2_ITERATIONS = 310_000;

    
    // JWT Headers and Prefixes
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX_BEARER = "Bearer ";

    // JWT Claims Keys
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_TENANT_ID = "tenantId";
}
