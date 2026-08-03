package logger.utilities;

import logger.constants.AuditConstants;
import org.slf4j.MDC;
import java.util.UUID;

public final class MdcUtils {
    private MdcUtils() {
        // Prevent instantiation
    }

    public static void put(String key, String value) {
        if (key != null && value != null) {
            MDC.put(key, value);
        }
    }

    public static String get(String key) {
        return MDC.get(key);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static void clear() {
        MDC.clear();
    }

    public static String getOrCreateTraceId() {
        String traceId = MDC.get(AuditConstants.MDC_TRACE_ID);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(AuditConstants.MDC_TRACE_ID, traceId);
        }
        return traceId;
    }

    public static void populateContext(String traceId, String userId, String clientIp, String serviceName) {
        put(AuditConstants.MDC_TRACE_ID, traceId != null ? traceId : getOrCreateTraceId());
        put(AuditConstants.MDC_USER_ID, userId);
        put(AuditConstants.MDC_CLIENT_IP, clientIp);
        put(AuditConstants.MDC_SERVICE_NAME, serviceName);
    }
}
