package logger.config;

import java.io.InputStream;
import java.util.Properties;

public final class LibraryConfig {
    private static final String CONFIG_FILE = "logger.properties";

    // Keys
    public static final String KEY_ENV = "campxsync.logger.environment";
    public static final String KEY_SERVICE_NAME = "campxsync.logger.service.name";
    public static final String KEY_JWT_SECRET = "campxsync.logger.jwt.secret";
    public static final String KEY_JWT_ISSUER = "campxsync.logger.jwt.issuer";
    public static final String KEY_ENCRYPTION_KEY = "campxsync.logger.encryption.key";
    public static final String KEY_LOG_FORMAT = "campxsync.logger.format";

    private static final Properties CONFIG_PROPS = new Properties();

    static {
        // Load default config file from classpath
        try (InputStream is = LibraryConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                CONFIG_PROPS.load(is);
            }
        } catch (Exception e) {
            // Ignore, fallback to properties and environment
        }
    }

    private LibraryConfig() {
        // Prevent instantiation
    }

    public static String get(String key, String defaultValue) {
        // 1. Check System Properties
        String value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        // 2. Check Environment Variables (converting dot.notation to UPPER_SNAKE_CASE)
        String envKey = key.replace('.', '_').toUpperCase();
        value = System.getenv(envKey);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        // 3. Check loaded properties file
        value = CONFIG_PROPS.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        return defaultValue;
    }

    public static String getEnvironment() {
        return get(KEY_ENV, "dev");
    }

    public static String getServiceName() {
        return get(KEY_SERVICE_NAME, "anonymous-service");
    }

    public static String getJwtSecret() {
        String value = get(KEY_JWT_SECRET, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                "[CampXSync Logger] STARTUP FAILURE: Required secret '" + KEY_JWT_SECRET +
                "' is not configured. Set the environment variable 'CAMPXSYNC_LOGGER_JWT_SECRET' " +
                "or the system property '" + KEY_JWT_SECRET + "' before starting the application. " +
                "Never use a hardcoded default for cryptographic secrets."
            );
        }
        return value;
    }

    public static String getJwtIssuer() {
        return get(KEY_JWT_ISSUER, "campxsync");
    }

    public static String getEncryptionKey() {
        String value = get(KEY_ENCRYPTION_KEY, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                "[CampXSync Logger] STARTUP FAILURE: Required secret '" + KEY_ENCRYPTION_KEY +
                "' is not configured. Set the environment variable 'CAMPXSYNC_LOGGER_ENCRYPTION_KEY' " +
                "or the system property '" + KEY_ENCRYPTION_KEY + "' before starting the application. " +
                "Never use a hardcoded default for cryptographic secrets."
            );
        }
        return value;
    }

    public static String getLogFormat() {
        return get(KEY_LOG_FORMAT, "TEXT");
    }
}
