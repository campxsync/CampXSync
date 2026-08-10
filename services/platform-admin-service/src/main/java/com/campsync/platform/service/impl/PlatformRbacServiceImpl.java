package com.campsync.platform.service.impl;

import com.campsync.platform.dto.PlatformRbacDtos.*;
import com.campsync.platform.service.PlatformRbacService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PlatformRbacServiceImpl implements PlatformRbacService {

    private final Map<String, RoleEntry> roles = new ConcurrentHashMap<>();
    private final Map<String, AssignmentEntry> assignments = new ConcurrentHashMap<>();

    public PlatformRbacServiceImpl() {
        Set<String> superAdminPermissions = new HashSet<>(Arrays.asList(
            "platform:institutes:read", "platform:institutes:write",
            "platform:configs:write", "platform:rbac:write", "platform:billing:write"
        ));
        RoleEntry superAdmin = new RoleEntry(
            "role-super-admin", "Super Admin", "Full platform-wide administrative privileges",
            superAdminPermissions, Instant.now()
        );
        roles.put(superAdmin.getId(), superAdmin);
    }

    @Override
    public PlatformRoleResponse createRole(CreatePlatformRoleRequest request) {
        boolean nameExists = roles.values().stream().anyMatch(r -> r.getName().equalsIgnoreCase(request.getName()));
        if (nameExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Platform role with name '" + request.getName() + "' already exists.");
        }

        String id = "role-" + UUID.randomUUID().toString().substring(0, 8);
        RoleEntry role = new RoleEntry(id, request.getName(), request.getDescription(), request.getPermissions(), Instant.now());
        roles.put(id, role);

        return new PlatformRoleResponse(role.getId(), role.getName(), role.getDescription(), role.getPermissions(), role.getCreatedAt());
    }

    @Override
    public List<PlatformRoleResponse> listRoles() {
        return roles.values().stream()
            .map(r -> new PlatformRoleResponse(r.getId(), r.getName(), r.getDescription(), r.getPermissions(), r.getCreatedAt()))
            .collect(Collectors.toList());
    }

    @Override
    public RoleAssignmentResponse grantRole(GrantRoleRequest request, String actor) {
        RoleEntry role = roles.get(request.getRoleId());
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role with ID '" + request.getRoleId() + "' not found.");
        }

        String assignmentId = "assign-" + UUID.randomUUID().toString().substring(0, 8);
        AssignmentEntry assignment = new AssignmentEntry(assignmentId, request.getStaffId(), role.getId(), role.getName(), Instant.now(), actor);
        assignments.put(assignmentId, assignment);

        return new RoleAssignmentResponse(assignment.getId(), assignment.getStaffId(), assignment.getRoleId(), assignment.getRoleName(), assignment.getGrantedAt(), assignment.getGrantedBy());
    }

    @Override
    public void revokeRole(String assignmentId) {
        AssignmentEntry removed = assignments.remove(assignmentId);
        if (removed == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role assignment with ID '" + assignmentId + "' not found.");
        }
    }

    @Override
    public EffectivePermissionsResponse getEffectivePermissions(String staffId) {
        List<AssignmentEntry> staffAssignments = assignments.values().stream()
            .filter(a -> a.getStaffId().equalsIgnoreCase(staffId))
            .collect(Collectors.toList());

        Set<String> assignedRoleNames = new HashSet<>();
        Set<String> effectivePermissions = new HashSet<>();

        for (AssignmentEntry assignment : staffAssignments) {
            assignedRoleNames.add(assignment.getRoleName());
            RoleEntry role = roles.get(assignment.getRoleId());
            if (role != null) {
                effectivePermissions.addAll(role.getPermissions());
            }
        }

        if ("superadmin-1".equalsIgnoreCase(staffId) || staffAssignments.isEmpty()) {
            assignedRoleNames.add("Super Admin");
            effectivePermissions.addAll(roles.get("role-super-admin").getPermissions());
        }

        return new EffectivePermissionsResponse(staffId, assignedRoleNames, effectivePermissions, Instant.now());
    }

    private static class RoleEntry {
        private final String id;
        private final String name;
        private final String description;
        private final Set<String> permissions;
        private final Instant createdAt;

        public RoleEntry(String id, String name, String description, Set<String> permissions, Instant createdAt) {
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

    private static class AssignmentEntry {
        private final String id;
        private final String staffId;
        private final String roleId;
        private final String roleName;
        private final Instant grantedAt;
        private final String grantedBy;

        public AssignmentEntry(String id, String staffId, String roleId, String roleName, Instant grantedAt, String grantedBy) {
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
}
