package com.campxsync.platform.dto;

import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class InstituteDtos {

    public static class ProvisionInstituteRequest {
        @NotBlank(message = "Institute name is required")
        private String name;
        @NotBlank(message = "Subdomain is required")
        private String subdomain;
        @NotBlank(message = "Plan ID is required")
        private String planId;
        private String tenancyTier;

        public ProvisionInstituteRequest() {}
        public ProvisionInstituteRequest(String name, String subdomain, String planId, String tenancyTier) {
            this.name = name;
            this.subdomain = subdomain;
            this.planId = planId;
            this.tenancyTier = tenancyTier;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubdomain() { return subdomain; }
        public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }
        public String getTenancyTier() { return tenancyTier; }
        public void setTenancyTier(String tenancyTier) { this.tenancyTier = tenancyTier; }
    }

    public static class UpdateInstituteRequest {
        private String name;
        private String subdomain;
        private String tenancyTier;

        public UpdateInstituteRequest() {}
        public UpdateInstituteRequest(String name, String subdomain, String tenancyTier) {
            this.name = name;
            this.subdomain = subdomain;
            this.tenancyTier = tenancyTier;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubdomain() { return subdomain; }
        public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
        public String getTenancyTier() { return tenancyTier; }
        public void setTenancyTier(String tenancyTier) { this.tenancyTier = tenancyTier; }
    }

    public static class UpdateStatusRequest {
        @NotBlank(message = "Status is required")
        private String status;

        public UpdateStatusRequest() {}
        public UpdateStatusRequest(String status) { this.status = status; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class InstituteResponse {
        private String id;
        private String name;
        private String subdomain;
        private String planId;
        private String status;
        private String tenancyTier;
        private Instant createdAt;
        private Instant updatedAt;

        public InstituteResponse() {}
        public InstituteResponse(String id, String name, String subdomain, String planId, String status, String tenancyTier, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.subdomain = subdomain;
            this.planId = planId;
            this.status = status;
            this.tenancyTier = tenancyTier;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getSubdomain() { return subdomain; }
        public String getPlanId() { return planId; }
        public String getStatus() { return status; }
        public String getTenancyTier() { return tenancyTier; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }

    public static class PaginatedInstitutesResponse {
        private List<InstituteResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PaginatedInstitutesResponse() {}
        public PaginatedInstitutesResponse(List<InstituteResponse> content, int page, int size, long totalElements, int totalPages) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<InstituteResponse> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }
}
