package logger.logging;

import logger.config.LibraryConfig;
import logger.dto.AuditLogRecord;
import logger.dto.UserPrincipal;
import logger.events.AuditEvent;
import logger.events.AuditEventPublisher;
import logger.utilities.DateUtils;
import logger.utilities.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AuditLogger {
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT_LOGGER");

    private AuditLogger() {
        // Prevent instantiation
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void log(AuditLogRecord record) {
        if (record == null) {
            return;
        }

        // Fill default details if missing
        if (record.getId() == null) {
            record.setId(UUID.randomUUID().toString());
        }
        if (record.getTimestamp() == null) {
            record.setTimestamp(DateUtils.currentIsoString());
        }
        if (record.getTraceId() == null) {
            record.setTraceId(AuditContextHolder.getTraceId());
        }
        if (record.getServiceName() == null) {
            record.setServiceName(LibraryConfig.getServiceName());
        }
        if (record.getEnvironment() == null) {
            record.setEnvironment(LibraryConfig.getEnvironment());
        }

        // Populate User credentials if context is active
        UserPrincipal user = AuditContextHolder.getUser();
        if (user != null) {
            if (record.getUserId() == null) {
                record.setUserId(user.getUserId());
            }
            if (record.getUsername() == null) {
                record.setUsername(user.getUsername());
            }
        } else {
            if (record.getUserId() == null) {
                record.setUserId("SYSTEM");
            }
            if (record.getUsername() == null) {
                record.setUsername("SYSTEM_ACTOR");
            }
        }

        // Populate Client source IP
        if (record.getClientIp() == null) {
            String ip = AuditContextHolder.getClientIp();
            record.setClientIp(ip != null ? ip : "UNKNOWN_SOURCE");
        }

        // Convert record to the configured format and write to dedicated Audit stream
        String logMessage;
        if ("JSON".equalsIgnoreCase(LibraryConfig.getLogFormat())) {
            logMessage = JsonUtils.toJson(record);
        } else {
            logMessage = formatAsSimpleText(record);
        }
        AUDIT_LOG.info(logMessage);

        // Publish event to custom listeners asynchronously
        AuditEventPublisher.publishAsync(new AuditEvent(record));
    }

    public static String formatAsSimpleText(AuditLogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(record.getTimestamp()).append("] ");
        sb.append("AUDIT ");
        sb.append("[Trace: ").append(record.getTraceId() != null ? record.getTraceId() : "None").append("] ");
        sb.append("[Actor: ").append(record.getUsername() != null ? record.getUsername() : "SYSTEM");
        if (record.getUserId() != null && !record.getUserId().equals("SYSTEM")) {
            sb.append(" (").append(record.getUserId()).append(")");
        }
        sb.append("] ");
        sb.append("[IP: ").append(record.getClientIp() != null ? record.getClientIp() : "UNKNOWN_SOURCE").append("] ");
        sb.append("Action: ").append(record.getAction() != null ? record.getAction() : "UNKNOWN").append(" | ");
        sb.append("Entity: ").append(record.getEntityName() != null ? record.getEntityName() : "None");
        if (record.getEntityId() != null) {
            sb.append(" (").append(record.getEntityId()).append(")");
        }
        sb.append(" | ");
        sb.append("Status: ").append(record.getStatus()).append(" | ");
        if (record.getExecutionTimeMs() != null) {
            sb.append("Execution: ").append(record.getExecutionTimeMs()).append("ms | ");
        }
        sb.append("Msg: ").append(record.getMessage() != null ? record.getMessage() : "No message");
        if (record.getDetails() != null && !record.getDetails().isEmpty()) {
            sb.append(" | Details: ").append(record.getDetails());
        }
        return sb.toString();
    }

    public static class Builder {
        private String action;
        private String entityName;
        private String entityId;
        private String status = "SUCCESS";
        private String message;
        private Long executionTimeMs;
        private final Map<String, Object> details = new HashMap<>();
        private String clientIp;
        private String userAgent;

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder entity(String entityName, String entityId) {
            this.entityName = entityName;
            this.entityId = entityId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder success() {
            this.status = "SUCCESS";
            return this;
        }

        public Builder failure(String errorMessage) {
            this.status = "FAILURE";
            this.message = errorMessage;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder executionTime(long ms) {
            this.executionTimeMs = ms;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder detail(String key, Object value) {
            if (key != null) {
                this.details.put(key, value);
            }
            return this;
        }

        public Builder details(Map<String, Object> details) {
            if (details != null) {
                this.details.putAll(details);
            }
            return this;
        }

        public void log() {
            AuditLogRecord record = new AuditLogRecord();
            record.setAction(this.action);
            record.setEntityName(this.entityName);
            record.setEntityId(this.entityId);
            record.setStatus(this.status);
            record.setMessage(this.message);
            record.setExecutionTimeMs(this.executionTimeMs);
            record.setDetails(this.details);
            record.setClientIp(this.clientIp);
            record.setUserAgent(this.userAgent);
            
            AuditLogger.log(record);
        }
    }
}
