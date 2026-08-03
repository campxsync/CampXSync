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
        return get(KEY_JWT_SECRET, "campxsync_secret_key_needs_to_be_replaced_in_production");
    }

    public static String getJwtIssuer() {
        return get(KEY_JWT_ISSUER, "campxsync");
    }

    public static String getEncryptionKey() {
        return get(KEY_ENCRYPTION_KEY, "campxsync_default_aes_encryption_key_2026");
    }

    public static String getLogFormat() {
        return get(KEY_LOG_FORMAT, "TEXT");
    }
}
