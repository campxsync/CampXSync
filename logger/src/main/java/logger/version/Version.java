package logger.version;

import java.io.InputStream;
import java.util.Properties;

public final class Version {
    private static final String VERSION_FILE = "version.properties";
    private static final String PROPERTY_VERSION = "version";
    private static final String PROPERTY_NAME = "name";
    private static final String DEFAULT_VERSION = "1.0.0";
    private static final String DEFAULT_NAME = "campxsync-shared-logger";

    private static String version = DEFAULT_VERSION;
    private static String name = DEFAULT_NAME;

    static {
        try (InputStream is = Version.class.getClassLoader().getResourceAsStream(VERSION_FILE)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                version = props.getProperty(PROPERTY_VERSION, DEFAULT_VERSION);
                name = props.getProperty(PROPERTY_NAME, DEFAULT_NAME);
            }
        } catch (Exception e) {
            // Fallback silently to defaults
        }
    }

    private Version() {
        // Prevent instantiation
    }

    public static String getVersion() {
        return version;
    }

    public static String getName() {
        return name;
    }

    public static String getFullVersionString() {
        return name + " v" + version;
    }
}
