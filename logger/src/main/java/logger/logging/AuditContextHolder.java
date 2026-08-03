package logger.logging;

import logger.constants.AuditConstants;
import logger.dto.UserPrincipal;
import logger.utilities.MdcUtils;

public final class AuditContextHolder {
    private static final ThreadLocal<UserPrincipal> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_CLIENT_IP = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TRACE_ID = new ThreadLocal<>();

    private AuditContextHolder() {
        // Prevent instantiation
    }

    public static void setUser(UserPrincipal user) {
        CURRENT_USER.set(user);
        if (user != null) {
            MdcUtils.put(AuditConstants.MDC_USER_ID, user.getUserId());
        } else {
            MdcUtils.remove(AuditConstants.MDC_USER_ID);
        }
    }

    public static UserPrincipal getUser() {
        return CURRENT_USER.get();
    }

    public static void setClientIp(String ip) {
        CURRENT_CLIENT_IP.set(ip);
        if (ip != null) {
            MdcUtils.put(AuditConstants.MDC_CLIENT_IP, ip);
        } else {
            MdcUtils.remove(AuditConstants.MDC_CLIENT_IP);
        }
    }

    public static String getClientIp() {
        return CURRENT_CLIENT_IP.get();
    }

    public static void setTraceId(String traceId) {
        CURRENT_TRACE_ID.set(traceId);
        if (traceId != null) {
            MdcUtils.put(AuditConstants.MDC_TRACE_ID, traceId);
        } else {
            MdcUtils.remove(AuditConstants.MDC_TRACE_ID);
        }
    }

    public static String getTraceId() {
        String traceId = CURRENT_TRACE_ID.get();
        if (traceId == null) {
            traceId = MdcUtils.getOrCreateTraceId();
            CURRENT_TRACE_ID.set(traceId);
        }
        return traceId;
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_CLIENT_IP.remove();
        CURRENT_TRACE_ID.remove();
        MdcUtils.clear();
    }

    public static void initContext(UserPrincipal user, String clientIp, String traceId) {
        setTraceId(traceId);
        setUser(user);
        setClientIp(clientIp);
    }
}
