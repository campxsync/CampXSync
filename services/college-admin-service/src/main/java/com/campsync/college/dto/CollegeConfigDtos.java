package com.campsync.college.dto;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CollegeConfigDtos {

    public static class UpdateCollegeConfigRequest {
        @NotNull(message = "Setting value cannot be null")
        private Object value;

        public UpdateCollegeConfigRequest() {}
        public UpdateCollegeConfigRequest(Object value) { this.value = value; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class CollegeConfigResponse {
        private String key;
        private Object value;
        private String category; // branding, feature_flag, operational
        private String institutionId;
        private Instant updatedAt;
        private String updatedBy;

        public CollegeConfigResponse() {}
        public CollegeConfigResponse(String key, Object value, String category, String institutionId, Instant updatedAt, String updatedBy) {
            this.key = key;
            this.value = value;
            this.category = category;
            this.institutionId = institutionId;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
        }

        public String getKey() { return key; }
        public Object getValue() { return value; }
        public String getCategory() { return category; }
        public String getInstitutionId() { return institutionId; }
        public Instant getUpdatedAt() { return updatedAt; }
        public String getUpdatedBy() { return updatedBy; }
    }

    public static class ConfigAuditHistoryResponse {
        private String key;
        private Object previousValue;
        private Object newValue;
        private String institutionId;
        private String actor;
        private Instant timestamp;

        public ConfigAuditHistoryResponse() {}
        public ConfigAuditHistoryResponse(String key, Object previousValue, Object newValue, String institutionId, String actor, Instant timestamp) {
            this.key = key;
            this.previousValue = previousValue;
            this.newValue = newValue;
            this.institutionId = institutionId;
            this.actor = actor;
            this.timestamp = timestamp;
        }

        public String getKey() { return key; }
        public Object getPreviousValue() { return previousValue; }
        public Object getNewValue() { return newValue; }
        public String getInstitutionId() { return institutionId; }
        public String getActor() { return actor; }
        public Instant getTimestamp() { return timestamp; }
    }
}
