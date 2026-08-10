package com.campsync.college.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.Set;

public class CollegeRbacDtos {

    public static class CreateCustomRoleRequest {
        @NotBlank(message = "Role name is required")
        private String name;
        private String description;
        @NotEmpty(message = "Permissions set cannot be empty")
        private Set<String> permissions;

        public CreateCustomRoleRequest() {}
        public CreateCustomRoleRequest(String name, String description, Set<String> permissions) {
            this.name = name;
            this.description = description;
            this.permissions = permissions;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    }

    public static class CollegeRoleResponse {
        private String id;
        private String institutionId;
        private String name;
        private String description;
        private Set<String> permissions;
        private Instant createdAt;

        public CollegeRoleResponse() {}
        public CollegeRoleResponse(String id, String institutionId, String name, String description, Set<String> permissions, Instant createdAt) {
            this.id = id;
            this.institutionId = institutionId;
            this.name = name;
            this.description = description;
            this.permissions = permissions;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Set<String> getPermissions() { return permissions; }
        public Instant getCreatedAt() { return createdAt; }
    }

    public static class GrantUserRoleRequest {
        @NotBlank(message = "User ID is required")
        private String userId;
        @NotBlank(message = "Role ID is required")
        private String roleId;
        private String scope; // department, hostel, program, or institution-wide

        public GrantUserRoleRequest() {}
        public GrantUserRoleRequest(String userId, String roleId, String scope) {
            this.userId = userId;
            this.roleId = roleId;
            this.scope = scope;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }

    public static class UserRoleAssignmentResponse {
        private String id;
        private String institutionId;
        private String userId;
        private String roleId;
        private String roleName;
        private String scope;
        private Instant grantedAt;

        public UserRoleAssignmentResponse() {}
        public UserRoleAssignmentResponse(String id, String institutionId, String userId, String roleId, String roleName, String scope, Instant grantedAt) {
            this.id = id;
            this.institutionId = institutionId;
            this.userId = userId;
            this.roleId = roleId;
            this.roleName = roleName;
            this.scope = scope;
            this.grantedAt = grantedAt;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getUserId() { return userId; }
        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
        public String getScope() { return scope; }
        public Instant getGrantedAt() { return grantedAt; }
    }

    public static class EffectiveTenantPermissionsResponse {
        private String userId;
        private String institutionId;
        private Set<String> roles;
        private Set<String> effectivePermissions;
        private Instant resolvedAt;

        public EffectiveTenantPermissionsResponse() {}
        public EffectiveTenantPermissionsResponse(String userId, String institutionId, Set<String> roles, Set<String> effectivePermissions, Instant resolvedAt) {
            this.userId = userId;
            this.institutionId = institutionId;
            this.roles = roles;
            this.effectivePermissions = effectivePermissions;
            this.resolvedAt = resolvedAt;
        }

        public String getUserId() { return userId; }
        public String getInstitutionId() { return institutionId; }
        public Set<String> getRoles() { return roles; }
        public Set<String> getEffectivePermissions() { return effectivePermissions; }
        public Instant getResolvedAt() { return resolvedAt; }
    }
}
