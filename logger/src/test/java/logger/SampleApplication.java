package logger;

import logger.cache.TtlCache;
import logger.constants.AuditConstants;
import logger.dto.UserPrincipal;
import logger.encryption.EncryptionUtils;
import logger.jwt.JwtProvider;
import logger.logging.AuditContextHolder;
import logger.logging.AuditLogger;
import logger.version.Version;

import java.util.Arrays;
import java.util.UUID;

public class SampleApplication {

    public static void main(String[] args) {
        System.out.println("=== Starting Logger Library Demo ===");
        System.out.println("Library Info: " + Version.getFullVersionString());
        System.out.println();

        // 1. JWT Provider Demo
        System.out.println("--- 1. Generating & Verifying JWT Token ---");
        String secret = "super_secret_signing_key_for_demo_app_2026";
        JwtProvider jwtProvider = new JwtProvider(secret, "campxsync");
        
        UserPrincipal originalUser = new UserPrincipal(
                "usr-789", 
                "alice_smith", 
                "alice@campx.com", 
                Arrays.asList("STUDENT", "MEMBER"), 
                "tenant-4"
        );
        System.out.println("Original Principal: " + originalUser);
        
        // Expiration = 1 minute (60,000 ms)
        String token = jwtProvider.createToken(originalUser, 60000);
        System.out.println("Generated JWT Token:\n" + token);
        
        UserPrincipal decodedUser = jwtProvider.validateAndDecode(token);
        System.out.println("Decoded Principal: " + decodedUser);
        System.out.println();

        // 2. Encryption Demo
        System.out.println("--- 2. Encrypting Sensitive Data (AES-256-GCM) ---");
        String aesKey = "encryption_secret_key_campx_2026";
        String sensitiveData = "Student Grade: A+, SSH-Key: d398fh9fh3r8f";
        System.out.println("Original Data:  " + sensitiveData);
        
        String cipherText = EncryptionUtils.encrypt(sensitiveData, aesKey);
        System.out.println("Encrypted Data: " + cipherText);
        
        String decryptedData = EncryptionUtils.decrypt(cipherText, aesKey);
        System.out.println("Decrypted Data: " + decryptedData);
        System.out.println();

        // 3. TTL Cache Demo
        System.out.println("--- 3. Testing In-Memory TTL Cache ---");
        TtlCache<String, String> cache = new TtlCache<>(1500); // 1.5 seconds TTL
        cache.put("session-token", token);
        System.out.println("Is key cached immediately? " + (cache.get("session-token") != null ? "Yes" : "No"));
        
        System.out.println("Waiting 2 seconds for cache entry to expire...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Is key cached after sleep? " + (cache.get("session-token") != null ? "Yes" : "No"));
        System.out.println();

        // 4. Audit Logging Demo
        System.out.println("--- 4. Initializing Context & Writing Audit Log ---");
        
        // Simulating the start of an HTTP request: Setting User, Client IP, and TraceId
        String mockTraceId = UUID.randomUUID().toString().replace("-", "");
        AuditContextHolder.initContext(decodedUser, "192.168.10.45", mockTraceId);
        
        System.out.println("Context initialized. Active Trace ID: " + AuditContextHolder.getTraceId());
        
        // 4a. Log in default TEXT format
        System.out.println("\n[Format: TEXT (Default)] Writing audit log event to Logback logging stream:");
        AuditLogger.builder()
                .action(AuditConstants.ACTION_UPDATE)
                .entity("COURSE_REGISTRATION", "reg-1102")
                .success()
                .message("Alice successfully enrolled in Computer Science 101")
                .detail("courseCode", "CS101")
                .detail("credits", 4)
                .executionTime(45)
                .log();

        // 4b. Log in JSON format
        System.out.println("\n[Format: JSON] Switching format and writing audit log event:");
        System.setProperty("campxsync.logger.format", "JSON");
        AuditLogger.builder()
                .action(AuditConstants.ACTION_AUTHENTICATE)
                .entity("USER_LOGIN", "usr-789")
                .success()
                .message("User logged in successfully")
                .executionTime(10)
                .log();
        System.clearProperty("campxsync.logger.format");

        // Simulating end of request: Clean up thread local context
        AuditContextHolder.clear();
        System.out.println("\nContext cleared.");
        System.out.println();
        System.out.println("=== Demo Finished Successfully ===");
    }
}
