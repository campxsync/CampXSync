package com.campxsync.platform.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AuditHealthDtos {

    public static class AuditLogResponse {
        private String id;
        private String eventType;
        private String institutionId;
        private String actorId;
        private Map<String, Object> payload;
        private Instant timestamp;

        public AuditLogResponse() {}
        public AuditLogResponse(String id, String eventType, String institutionId, String actorId, Map<String, Object> payload, Instant timestamp) {
            this.id = id;
            this.eventType = eventType;
            this.institutionId = institutionId;
            this.actorId = actorId;
            this.payload = payload;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public String getEventType() { return eventType; }
        public String getInstitutionId() { return institutionId; }
        public String getActorId() { return actorId; }
        public Map<String, Object> getPayload() { return payload; }
        public Instant getTimestamp() { return timestamp; }
    }

    public static class PaginatedAuditLogsResponse {
        private List<AuditLogResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PaginatedAuditLogsResponse() {}
        public PaginatedAuditLogsResponse(List<AuditLogResponse> content, int page, int size, long totalElements, int totalPages) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<AuditLogResponse> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }

    public static class SystemHealthResponse {
        private String status;
        private Instant timestamp;
        private Map<String, ServiceHealthDetail> services;

        public SystemHealthResponse() {}
        public SystemHealthResponse(String status, Instant timestamp, Map<String, ServiceHealthDetail> services) {
            this.status = status;
            this.timestamp = timestamp;
            this.services = services;
        }

        public String getStatus() { return status; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, ServiceHealthDetail> getServices() { return services; }
    }

    public static class ServiceHealthDetail {
        private String serviceName;
        private String status;
        private Map<String, Object> details;

        public ServiceHealthDetail() {}
        public ServiceHealthDetail(String serviceName, String status, Map<String, Object> details) {
            this.serviceName = serviceName;
            this.status = status;
            this.details = details;
        }

        public String getServiceName() { return serviceName; }
        public String getStatus() { return status; }
        public Map<String, Object> getDetails() { return details; }
    }
}
