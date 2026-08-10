package com.campsync.college.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CollegeAuditDtos {

    public static class CollegeAuditLogResponse {
        private String id;
        private String institutionId;
        private String eventType;
        private String sourceModule;
        private String actorId;
        private Map<String, Object> payload;
        private Instant timestamp;

        public CollegeAuditLogResponse() {}
        public CollegeAuditLogResponse(String id, String institutionId, String eventType, String sourceModule, String actorId, Map<String, Object> payload, Instant timestamp) {
            this.id = id;
            this.institutionId = institutionId;
            this.eventType = eventType;
            this.sourceModule = sourceModule;
            this.actorId = actorId;
            this.payload = payload;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getEventType() { return eventType; }
        public String getSourceModule() { return sourceModule; }
        public String getActorId() { return actorId; }
        public Map<String, Object> getPayload() { return payload; }
        public Instant getTimestamp() { return timestamp; }
    }

    public static class PaginatedCollegeAuditLogsResponse {
        private List<CollegeAuditLogResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PaginatedCollegeAuditLogsResponse() {}
        public PaginatedCollegeAuditLogsResponse(List<CollegeAuditLogResponse> content, int page, int size, long totalElements, int totalPages) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<CollegeAuditLogResponse> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }
}
