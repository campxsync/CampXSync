package com.campsync.platform.dto;

import java.time.Instant;
import java.util.Map;

public class PlatformAnalyticsDtos {

    public static class AnalyticsSnapshotResponse {
        private String snapshotId;
        private String metric;
        private String period;
        private Map<String, Object> data;
        private Instant computedAt;

        public AnalyticsSnapshotResponse() {}
        public AnalyticsSnapshotResponse(String snapshotId, String metric, String period, Map<String, Object> data, Instant computedAt) {
            this.snapshotId = snapshotId;
            this.metric = metric;
            this.period = period;
            this.data = data;
            this.computedAt = computedAt;
        }

        public String getSnapshotId() { return snapshotId; }
        public String getMetric() { return metric; }
        public String getPeriod() { return period; }
        public Map<String, Object> getData() { return data; }
        public Instant getComputedAt() { return computedAt; }
    }

    public static class RecomputeRequest {
        private String metric;
        private String period;

        public RecomputeRequest() {}
        public RecomputeRequest(String metric, String period) {
            this.metric = metric;
            this.period = period;
        }

        public String getMetric() { return metric; }
        public void setMetric(String metric) { this.metric = metric; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
    }

    public static class RecomputeJobResponse {
        private String jobId;
        private String status;
        private String message;
        private Instant triggeredAt;

        public RecomputeJobResponse() {}
        public RecomputeJobResponse(String jobId, String status, String message, Instant triggeredAt) {
            this.jobId = jobId;
            this.status = status;
            this.message = message;
            this.triggeredAt = triggeredAt;
        }

        public String getJobId() { return jobId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Instant getTriggeredAt() { return triggeredAt; }
    }
}
