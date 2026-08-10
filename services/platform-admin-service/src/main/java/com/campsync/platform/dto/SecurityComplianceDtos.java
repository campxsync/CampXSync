package com.campsync.platform.dto;

import java.time.Instant;
import java.util.List;

public class SecurityComplianceDtos {

    public static class RunComplianceCheckRequest {
        private String institutionId;
        private List<String> policyIds;

        public RunComplianceCheckRequest() {}
        public RunComplianceCheckRequest(String institutionId, List<String> policyIds) {
            this.institutionId = institutionId;
            this.policyIds = policyIds;
        }

        public String getInstitutionId() { return institutionId; }
        public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
        public List<String> getPolicyIds() { return policyIds; }
        public void setPolicyIds(List<String> policyIds) { this.policyIds = policyIds; }
    }

    public static class ComplianceCheckTriggerResponse {
        private String checkReferenceId;
        private String scope;
        private String status;
        private Instant triggeredAt;

        public ComplianceCheckTriggerResponse() {}
        public ComplianceCheckTriggerResponse(String checkReferenceId, String scope, String status, Instant triggeredAt) {
            this.checkReferenceId = checkReferenceId;
            this.scope = scope;
            this.status = status;
            this.triggeredAt = triggeredAt;
        }

        public String getCheckReferenceId() { return checkReferenceId; }
        public String getScope() { return scope; }
        public String getStatus() { return status; }
        public Instant getTriggeredAt() { return triggeredAt; }
    }

    public static class ComplianceCheckResultResponse {
        private String checkId;
        private String institutionId;
        private boolean compliant;
        private List<ViolationDetail> violations;
        private Instant evaluatedAt;

        public ComplianceCheckResultResponse() {}
        public ComplianceCheckResultResponse(String checkId, String institutionId, boolean compliant, List<ViolationDetail> violations, Instant evaluatedAt) {
            this.checkId = checkId;
            this.institutionId = institutionId;
            this.compliant = compliant;
            this.violations = violations;
            this.evaluatedAt = evaluatedAt;
        }

        public String getCheckId() { return checkId; }
        public String getInstitutionId() { return institutionId; }
        public boolean isCompliant() { return compliant; }
        public List<ViolationDetail> getViolations() { return violations; }
        public Instant getEvaluatedAt() { return evaluatedAt; }
    }

    public static class ViolationDetail {
        private String policyId;
        private String policyName;
        private String severity;
        private String detail;

        public ViolationDetail() {}
        public ViolationDetail(String policyId, String policyName, String severity, String detail) {
            this.policyId = policyId;
            this.policyName = policyName;
            this.severity = severity;
            this.detail = detail;
        }

        public String getPolicyId() { return policyId; }
        public String getPolicyName() { return policyName; }
        public String getSeverity() { return severity; }
        public String getDetail() { return detail; }
    }
}
