package logger.constants;

public final class AuditConstants {
    private AuditConstants() {
        // Prevent instantiation
    }

    // HTTP Header Constants
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_CLIENT_IP = "X-Forwarded-For";

    // MDC Constants
    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_CLIENT_IP = "clientIp";
    public static final String MDC_SERVICE_NAME = "serviceName";
    public static final String MDC_ENVIRONMENT = "environment";

    // Audit Actions
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_READ = "READ";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_AUTHENTICATE = "AUTHENTICATE";
    public static final String ACTION_AUTHORIZE = "AUTHORIZE";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_IMPORT = "IMPORT";
    public static final String ACTION_UNKNOWN = "UNKNOWN";

    // System constants
    public static final String DEFAULT_SYSTEM_USER = "SYSTEM";
    public static final String DEFAULT_UNKNOWN_SOURCE = "UNKNOWN_SOURCE";
}
