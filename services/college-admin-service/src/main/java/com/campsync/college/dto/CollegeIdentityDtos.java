package com.campxsync.college.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CollegeIdentityDtos {

    public static class CreateUserRequest {
        @NotBlank(message = "Profile type is required")
        private String profileType; // student, faculty, staff, parent, alumni, admin
        @NotBlank(message = "Full name is required")
        private String name;
        @NotBlank(message = "Email is required")
        @Email(message = "Valid email address is required")
        private String email;
        private Map<String, Object> profile;

        public CreateUserRequest() {}
        public CreateUserRequest(String profileType, String name, String email, Map<String, Object> profile) {
            this.profileType = profileType;
            this.name = name;
            this.email = email;
            this.profile = profile;
        }

        public String getProfileType() { return profileType; }
        public void setProfileType(String profileType) { this.profileType = profileType; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Map<String, Object> getProfile() { return profile; }
        public void setProfile(Map<String, Object> profile) { this.profile = profile; }
    }

    public static class UpdateUserStatusRequest {
        @NotBlank(message = "Status is required")
        private String status;

        public UpdateUserStatusRequest() {}
        public UpdateUserStatusRequest(String status) { this.status = status; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class UpdateUserProfileRequest {
        private Map<String, Object> profile;

        public UpdateUserProfileRequest() {}
        public UpdateUserProfileRequest(Map<String, Object> profile) { this.profile = profile; }
        public Map<String, Object> getProfile() { return profile; }
        public void setProfile(Map<String, Object> profile) { this.profile = profile; }
    }

    public static class UserResponse {
        private String id;
        private String institutionId;
        private String profileType;
        private String name;
        private String email;
        private String status;
        private Map<String, Object> profile;
        private Instant createdAt;
        private Instant updatedAt;

        public UserResponse() {}
        public UserResponse(String id, String institutionId, String profileType, String name, String email, String status, Map<String, Object> profile, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.institutionId = institutionId;
            this.profileType = profileType;
            this.name = name;
            this.email = email;
            this.status = status;
            this.profile = profile;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getProfileType() { return profileType; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public Map<String, Object> getProfile() { return profile; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }

    public static class PaginatedUsersResponse {
        private List<UserResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PaginatedUsersResponse() {}
        public PaginatedUsersResponse(List<UserResponse> content, int page, int size, long totalElements, int totalPages) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<UserResponse> getContent() { return content; }
        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }
}
