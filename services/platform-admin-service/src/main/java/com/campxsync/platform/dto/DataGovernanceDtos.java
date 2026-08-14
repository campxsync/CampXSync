package com.campxsync.platform.dto;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

public class DataGovernanceDtos {

    public static class CreatePolicyRequest {
        @NotBlank(message = "Policy name is required")
        private String name;
        @NotBlank(message = "Policy type is required")
        private String type;
        @NotBlank(message = "Applies to is required")
        private String appliesTo;
        private Map<String, Object> rule;

        public CreatePolicyRequest() {}
        public CreatePolicyRequest(String name, String type, String appliesTo, Map<String, Object> rule) {
            this.name = name;
            this.type = type;
            this.appliesTo = appliesTo;
            this.rule = rule;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAppliesTo() { return appliesTo; }
        public void setAppliesTo(String appliesTo) { this.appliesTo = appliesTo; }
        public Map<String, Object> getRule() { return rule; }
        public void setRule(Map<String, Object> rule) { this.rule = rule; }
    }

    public static class UpdatePolicyRequest {
        private String name;
        private String type;
        private String appliesTo;
        private Map<String, Object> rule;

        public UpdatePolicyRequest() {}
        public UpdatePolicyRequest(String name, String type, String appliesTo, Map<String, Object> rule) {
            this.name = name;
            this.type = type;
            this.appliesTo = appliesTo;
            this.rule = rule;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAppliesTo() { return appliesTo; }
        public void setAppliesTo(String appliesTo) { this.appliesTo = appliesTo; }
        public Map<String, Object> getRule() { return rule; }
        public void setRule(Map<String, Object> rule) { this.rule = rule; }
    }

    public static class PolicyResponse {
        private String id;
        private String name;
        private String type;
        private String appliesTo;
        private Map<String, Object> rule;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;

        public PolicyResponse() {}
        public PolicyResponse(String id, String name, String type, String appliesTo, Map<String, Object> rule, String status, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.appliesTo = appliesTo;
            this.rule = rule;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getAppliesTo() { return appliesTo; }
        public Map<String, Object> getRule() { return rule; }
        public String getStatus() { return status; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }
}
