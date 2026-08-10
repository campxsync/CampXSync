package logger;

import logger.cache.TtlCache;
import logger.constants.AuditConstants;
import logger.dto.AuditLogRecord;
import logger.dto.UserPrincipal;
import logger.encryption.EncryptionUtils;
import logger.events.AuditEvent;
import logger.events.AuditEventPublisher;
import logger.exception.EncryptionException;
import logger.exception.JwtException;
import logger.exception.ValidationException;
import logger.jwt.JwtProvider;
import logger.logging.AuditContextHolder;
import logger.logging.AuditLogger;
import logger.mapper.ModelMapper;
import logger.utilities.DateUtils;
import logger.utilities.JsonUtils;
import logger.utilities.MdcUtils;
import logger.utilities.NetworkUtils;
import logger.validation.ValidationUtils;
import logger.version.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class LoggerLibraryTest {

    private static final String SECRET_KEY = "test_super_secure_key_for_jwt_signing_purposes";
    private static final String ENCRYPTION_KEY = "aes_test_encryption_key_2026_logger";
    private JwtProvider jwtProvider;

    @BeforeEach
    public void setUp() {
        jwtProvider = new JwtProvider(SECRET_KEY, "test-issuer");
        AuditContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        AuditContextHolder.clear();
    }

    @Test
    public void testVersion() {
        assertNotNull(Version.getVersion());
        assertNotNull(Version.getName());
        assertTrue(Version.getFullVersionString().contains("campxsync"));
    }

    @Test
    public void testEncryptionUtils() {
        String original = "Confidential data: $1000";
        String cipher = EncryptionUtils.encrypt(original, ENCRYPTION_KEY);
        assertNotNull(cipher);
        assertNotEquals(original, cipher);

        String decrypted = EncryptionUtils.decrypt(cipher, ENCRYPTION_KEY);
        assertEquals(original, decrypted);

        // Test bad key failure
        assertThrows(EncryptionException.class, () -> {
            EncryptionUtils.decrypt(cipher, "wrong_key_12345678");
        });

        // Test encryption with nulls
        assertNull(EncryptionUtils.encrypt(null, ENCRYPTION_KEY));
        assertNull(EncryptionUtils.decrypt(null, ENCRYPTION_KEY));
    }

    @Test
    public void testTtlCache() throws InterruptedException {
        TtlCache<String, String> cache = new TtlCache<>(50); // 50ms TTL

        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        // Wait for TTL expiration
        Thread.sleep(70);

        assertNull(cache.get("key1"));
        assertEquals(0, cache.size());
    }

    @Test
    public void testJwtProvider() {
        UserPrincipal principal = new UserPrincipal(
                "user-123",
                "john_doe",
                "john@example.com",
                Arrays.asList("ROLE_USER", "ROLE_ADMIN"),
                "tenant-xyz"
        );

        String token = jwtProvider.createToken(principal, 5000); // 5s expiration
        assertNotNull(token);

        UserPrincipal decoded = jwtProvider.validateAndDecode(token);
        assertNotNull(decoded);
        assertEquals(principal.getUserId(), decoded.getUserId());
        assertEquals(principal.getUsername(), decoded.getUsername());
        assertEquals(principal.getEmail(), decoded.getEmail());
        assertEquals(principal.getTenantId(), decoded.getTenantId());
        assertTrue(decoded.getRoles().contains("ROLE_USER"));
        assertTrue(decoded.getRoles().contains("ROLE_ADMIN"));

        // Test invalid token
        assertThrows(JwtException.class, () -> {
            jwtProvider.validateAndDecode(token + "corrupted");
        });
    }

    @Test
    public void testValidationUtils() {
        assertDoesNotThrow(() -> ValidationUtils.notNull("not-null", "testField"));
        assertThrows(ValidationException.class, () -> ValidationUtils.notNull(null, "testField"));

        assertDoesNotThrow(() -> ValidationUtils.notEmpty("non-empty", "testField"));
        assertThrows(ValidationException.class, () -> ValidationUtils.notEmpty("", "testField"));

        assertDoesNotThrow(() -> ValidationUtils.validEmail("test@example.com", "emailField"));
        assertThrows(ValidationException.class, () -> ValidationUtils.validEmail("invalid-email", "emailField"));
    }

    @Test
    public void testModelMapper() {
        AuditLogRecord record = new AuditLogRecord();
        record.setAction("TEST_ACTION");
        record.setStatus("SUCCESS");
        record.setTimestamp(DateUtils.currentIsoString());

        Map<?, ?> map = ModelMapper.map(record, Map.class);
        assertNotNull(map);
        assertEquals("TEST_ACTION", map.get("action"));
        assertEquals("SUCCESS", map.get("status"));
    }

    @Test
    public void testNetworkUtils() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-For", "192.168.1.100, 10.0.0.1");

        String clientIp = NetworkUtils.getClientIp(headers);
        assertEquals("192.168.1.100", clientIp);
    }

    @Test
    public void testAuditContextAndMdc() {
        UserPrincipal principal = new UserPrincipal(
                "user-999", "admin", "admin@campx.com",
                Collections.singletonList("ADMIN"), "tenant-1"
        );

        AuditContextHolder.initContext(principal, "192.168.1.1", "test-trace-123");

        assertEquals("user-999", AuditContextHolder.getUser().getUserId());
        assertEquals("192.168.1.1", AuditContextHolder.getClientIp());
        assertEquals("test-trace-123", AuditContextHolder.getTraceId());

        // Verify MDC synchronization
        assertEquals("user-999", MDC.get(AuditConstants.MDC_USER_ID));
        assertEquals("192.168.1.1", MDC.get(AuditConstants.MDC_CLIENT_IP));
        assertEquals("test-trace-123", MDC.get(AuditConstants.MDC_TRACE_ID));

        AuditContextHolder.clear();

        assertNull(AuditContextHolder.getUser());
        assertNull(MDC.get(AuditConstants.MDC_USER_ID));
    }

    @Test
    public void testAuditLoggerAndEvents() throws InterruptedException {
        UserPrincipal principal = new UserPrincipal(
                "user-456", "tester", "tester@example.com",
                Collections.singletonList("TESTER"), "tenant-1"
        );
        AuditContextHolder.initContext(principal, "127.0.0.1", "trace-abc");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AuditLogRecord> capturedRecordRef = new AtomicReference<>();

        // Register custom event listener to test publisher
        AuditEventPublisher.registerListener(event -> {
            capturedRecordRef.set(event.getRecord());
            latch.countDown();
        });

        // Trigger logging
        AuditLogger.builder()
                .action(AuditConstants.ACTION_CREATE)
                .entity("USER_PROFILE", "profile-789")
                .success()
                .message("User profile created successfully")
                .executionTime(12)
                .detail("registrationType", "EMAIL")
                .log();

        // Wait for asynchronous event listener trigger
        boolean triggered = latch.await(2, TimeUnit.SECONDS);
        assertTrue(triggered, "Event listener was not triggered in time");

        AuditLogRecord record = capturedRecordRef.get();
        assertNotNull(record);
        assertEquals(AuditConstants.ACTION_CREATE, record.getAction());
        assertEquals("USER_PROFILE", record.getEntityName());
        assertEquals("profile-789", record.getEntityId());
        assertEquals("SUCCESS", record.getStatus());
        assertEquals("user-456", record.getUserId());
        assertEquals("tester", record.getUsername());
        assertEquals("127.0.0.1", record.getClientIp());
        assertEquals("trace-abc", record.getTraceId());
        assertEquals("EMAIL", record.getDetails().get("registrationType"));
        assertNotNull(record.getTimestamp());
        assertNotNull(record.getId());
    }

    @Test
    public void testTextAndJsonFormatting() {
        AuditLogRecord record = new AuditLogRecord();
        record.setId("rec-id-123");
        record.setTraceId("trace-xyz");
        record.setTimestamp("2026-08-03T14:30:00.000Z");
        record.setUserId("user-999");
        record.setUsername("alice_smith");
        record.setAction("UPDATE");
        record.setEntityName("STUDENT");
        record.setEntityId("stu-456");
        record.setStatus("SUCCESS");
        record.setExecutionTimeMs(35L);
        record.setMessage("Updated profile details");
        record.addDetail("credits", 4);

        // Verify TEXT layout fields
        String textOutput = AuditLogger.formatAsSimpleText(record);
        assertNotNull(textOutput);
        assertTrue(textOutput.contains("AUDIT"));
        assertTrue(textOutput.contains("[Trace: trace-xyz]"));
        assertTrue(textOutput.contains("[Actor: alice_smith (user-999)]"));
        assertTrue(textOutput.contains("Action: UPDATE | Entity: STUDENT (stu-456)"));
        assertTrue(textOutput.contains("Status: SUCCESS | Execution: 35ms"));
        assertTrue(textOutput.contains("Msg: Updated profile details"));
        assertTrue(textOutput.contains("Details: {credits=4}"));

        // Verify Property key selection
        System.setProperty("campxsync.logger.format", "JSON");
        assertEquals("JSON", logger.config.LibraryConfig.getLogFormat());
        System.clearProperty("campxsync.logger.format");
    }

    @Test
    public void testLogExecutionAnnotation() {
        logger.annotation.LogExecution annotation = DummyClass.class.getAnnotation(logger.annotation.LogExecution.class);
        assertNotNull(annotation);
        assertTrue(annotation.logArguments());
        assertTrue(annotation.logResult());
    }

    @logger.annotation.LogExecution
    private static class DummyClass {
        public void dummyMethod() {}
    }
}
