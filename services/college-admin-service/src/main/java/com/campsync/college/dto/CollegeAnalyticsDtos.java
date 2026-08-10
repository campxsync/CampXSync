package com.campsync.college.dto;

import java.time.Instant;
import java.util.Map;

public class CollegeAnalyticsDtos {

    public static class CollegeAnalyticsDashboardResponse {
        private String snapshotId;
        private String institutionId;
        private String metric;
        private String period;
        private Map<String, Object> data;
        private Instant computedAt;

        public CollegeAnalyticsDashboardResponse() {}
        public CollegeAnalyticsDashboardResponse(String snapshotId, String institutionId, String metric, String period, Map<String, Object> data, Instant computedAt) {
            this.snapshotId = snapshotId;
            this.institutionId = institutionId;
            this.metric = metric;
            this.period = period;
            this.data = data;
            this.computedAt = computedAt;
        }

        public String getSnapshotId() { return snapshotId; }
        public String getInstitutionId() { return institutionId; }
        public String getMetric() { return metric; }
        public String getPeriod() { return period; }
        public Map<String, Object> getData() { return data; }
        public Instant getComputedAt() { return computedAt; }
    }

    public static class TriggerRecomputeRequest {
        private String metric;
        private String period;

        public TriggerRecomputeRequest() {}
        public TriggerRecomputeRequest(String metric, String period) {
            this.metric = metric;
            this.period = period;
        }

        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
    }

    public static class RecomputeResponse {
        private String jobId;
        private String institutionId;
        private String status;
        private String message;
        private Instant triggeredAt;

        public RecomputeResponse() {}
        public RecomputeResponse(String jobId, String institutionId, String status, String message, Instant triggeredAt) {
            this.jobId = jobId;
            this.institutionId = institutionId;
            this.status = status;
            this.message = message;
            this.triggeredAt = triggeredAt;
        }

        public String getJobId() { return jobId; }
        public String getInstitutionId() { return institutionId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Instant getTriggeredAt() { return triggeredAt; }
    }
}
