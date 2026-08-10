package com.campsync.platform.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.Set;

public class PlatformRbacDtos {

    public static class CreatePlatformRoleRequest {
        @NotBlank(message = "Role name is required")
        private String name;
        private String description;
        @NotEmpty(message = "Permissions set cannot be empty")
        private Set<String> permissions;

        public CreatePlatformRoleRequest() {}
        public CreatePlatformRoleRequest(String name, String description, Set<String> permissions) {
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

    public static class PlatformRoleResponse {
        private String id;
        private String name;
        private String description;
        private Set<String> permissions;
        private Instant createdAt;

        public PlatformRoleResponse() {}
        public PlatformRoleResponse(String id, String name, String description, Set<String> permissions, Instant createdAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.permissions = permissions;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Set<String> getPermissions() { return permissions; }
        public Instant getCreatedAt() { return createdAt; }
    }

    public static class GrantRoleRequest {
        @NotBlank(message = "Staff ID is required")
        private String staffId;
        @NotBlank(message = "Role ID is required")
        private String roleId;

        public GrantRoleRequest() {}
        public GrantRoleRequest(String staffId, String roleId) {
            this.staffId = staffId;
            this.roleId = roleId;
        }

        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }
    }

    public static class RoleAssignmentResponse {
        private String id;
        private String staffId;
        private String roleId;
        private String roleName;
        private Instant grantedAt;
        private String grantedBy;

        public RoleAssignmentResponse() {}
        public RoleAssignmentResponse(String id, String staffId, String roleId, String roleName, Instant grantedAt, String grantedBy) {
            this.id = id;
            this.staffId = staffId;
            this.roleId = roleId;
            this.roleName = roleName;
            this.grantedAt = grantedAt;
            this.grantedBy = grantedBy;
        }

        public String getId() { return id; }
        public String getStaffId() { return staffId; }
        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
        public Instant getGrantedAt() { return grantedAt; }
        public String getGrantedBy() { return grantedBy; }
    }

    public static class EffectivePermissionsResponse {
        private String staffId;
        private Set<String> roles;
        private Set<String> effectivePermissions;
        private Instant resolvedAt;

        public EffectivePermissionsResponse() {}
        public EffectivePermissionsResponse(String staffId, Set<String> roles, Set<String> effectivePermissions, Instant resolvedAt) {
            this.staffId = staffId;
            this.roles = roles;
            this.effectivePermissions = effectivePermissions;
            this.resolvedAt = resolvedAt;
        }

        public String getStaffId() { return staffId; }
        public Set<String> getRoles() { return roles; }
        public Set<String> getEffectivePermissions() { return effectivePermissions; }
        public Instant getResolvedAt() { return resolvedAt; }
    }
}
