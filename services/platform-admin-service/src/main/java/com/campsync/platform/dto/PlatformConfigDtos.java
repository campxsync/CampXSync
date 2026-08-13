package com.campxsync.platform.dto;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class PlatformConfigDtos {

    public static class UpdateConfigRequest {
        @NotNull(message = "Config value cannot be null")
        private Object value;

        public UpdateConfigRequest() {}
        public UpdateConfigRequest(Object value) { this.value = value; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class PlatformConfigResponse {
        private String key;
        private Object value;
        private String description;
        private Instant updatedAt;
        private String updatedBy;

        public PlatformConfigResponse() {}
        public PlatformConfigResponse(String key, Object value, String description, Instant updatedAt, String updatedBy) {
            this.key = key;
            this.value = value;
            this.description = description;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
        }

        public String getKey() { return key; }
        public Object getValue() { return value; }
        public String getDescription() { return description; }
        public Instant getUpdatedAt() { return updatedAt; }
        public String getUpdatedBy() { return updatedBy; }
    }

    public static class ConfigAuditHistoryResponse {
        private String key;
        private Object previousValue;
        private Object newValue;
        private String actor;
        private Instant timestamp;

        public ConfigAuditHistoryResponse() {}
        public ConfigAuditHistoryResponse(String key, Object previousValue, Object newValue, String actor, Instant timestamp) {
            this.key = key;
            this.previousValue = previousValue;
            this.newValue = newValue;
            this.actor = actor;
            this.timestamp = timestamp;
        }

        public String getKey() { return key; }
        public Object getPreviousValue() { return previousValue; }
        public Object getNewValue() { return newValue; }
        public String getActor() { return actor; }
        public Instant getTimestamp() { return timestamp; }
    }
}
