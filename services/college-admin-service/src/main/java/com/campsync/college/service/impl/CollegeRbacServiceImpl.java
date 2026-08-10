package com.campsync.college.service.impl;

import com.campsync.college.dto.CollegeRbacDtos.*;
import com.campsync.college.service.CollegeRbacService;
import logger.constants.AuditConstants;
import logger.logging.AppLogger;
import logger.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CollegeRbacServiceImpl implements CollegeRbacService {

    private static final AppLogger log = AppLogger.getLogger(CollegeRbacServiceImpl.class);

    private final Map<String, RoleEntry> roleStore = new ConcurrentHashMap<>();
    private final Map<String, AssignmentEntry> assignmentStore = new ConcurrentHashMap<>();
    private final Set<String> globalPermissionCatalog = Collections.synchronizedSet(new HashSet<>());

    public CollegeRbacServiceImpl() {
        // Global catalog permissions for College Admin / Tenant tier
        globalPermissionCatalog.addAll(Arrays.asList(
            "college:users:read", "college:users:write", "college:configs:read", "college:configs:write",
            "college:roles:write", "college:analytics:read", "college:audit:read"
        ));

        // Seed default College Admin role
        Set<String> defaultAdminPermissions = new HashSet<>(globalPermissionCatalog);
        RoleEntry adminRole = new RoleEntry(
            "role-college-admin", "inst-101", "College Admin", "Default administrative role for institution",
            defaultAdminPermissions, Instant.now()
        );
        roleStore.put(adminRole.getId(), adminRole);
        log.info("Initialized CollegeRbacServiceImpl with default tenant role-college-admin");
    }

    @Override
    public CollegeRoleResponse createRole(String institutionId, CreateCustomRoleRequest request) {
        log.info("Creating custom tenant role for institutionId: {}, name={}", institutionId, request.getName());
        // Validate against global permission catalog (Story 23)
        for (String perm : request.getPermissions()) {
            if (!globalPermissionCatalog.contains(perm)) {
                log.warn("Permission validation failed for role '{}': permission '{}' not in global catalog", request.getName(), perm);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission '" + perm + "' is invalid and does not exist in the global catalog.");
            }
        }

        String id = "role-" + UUID.randomUUID().toString().substring(0, 8);
        RoleEntry role = new RoleEntry(id, institutionId, request.getName(), request.getDescription(), request.getPermissions(), Instant.now());
        roleStore.put(id, role);

        log.info("Successfully created custom role id={} for institutionId={}", id, institutionId);
        AuditLogger.builder()
                .action(AuditConstants.ACTION_CREATE)
                .entity("COLLEGE_ROLE", id)
                .success()
                .message("Tenant custom role created")
                .detail("roleName", request.getName())
                .detail("institutionId", institutionId)
                .log();

        return new CollegeRoleResponse(role.getId(), role.getInstitutionId(), role.getName(), role.getDescription(), role.getPermissions(), role.getCreatedAt());
    }

    @Override
    public List<CollegeRoleResponse> listRoles(String institutionId) {
        return roleStore.values().stream()
            .filter(r -> r.getInstitutionId().equalsIgnoreCase(institutionId))
            .map(r -> new CollegeRoleResponse(r.getId(), r.getInstitutionId(), r.getName(), r.getDescription(), r.getPermissions(), r.getCreatedAt()))
            .collect(Collectors.toList());
    }

    @Override
    public UserRoleAssignmentResponse grantRole(String institutionId, GrantUserRoleRequest request) {
        RoleEntry role = roleStore.get(request.getRoleId());
        if (role == null || !role.getInstitutionId().equalsIgnoreCase(institutionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role with ID '" + request.getRoleId() + "' not found.");
        }

        String assignmentId = "assign-" + UUID.randomUUID().toString().substring(0, 8);
        String scope = request.getScope() != null ? request.getScope() : "INSTITUTION_WIDE";
        AssignmentEntry assignment = new AssignmentEntry(assignmentId, institutionId, request.getUserId(), role.getId(), role.getName(), scope, Instant.now());
        assignmentStore.put(assignmentId, assignment);

        return new UserRoleAssignmentResponse(assignment.getId(), assignment.getInstitutionId(), assignment.getUserId(), assignment.getRoleId(), assignment.getRoleName(), assignment.getScope(), assignment.getGrantedAt());
    }

    @Override
    public void revokeRole(String institutionId, String assignmentId) {
        AssignmentEntry assignment = assignmentStore.get(assignmentId);
        if (assignment == null || !assignment.getInstitutionId().equalsIgnoreCase(institutionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role assignment with ID '" + assignmentId + "' not found.");
        }
        assignmentStore.remove(assignmentId);
    }

    @Override
    public EffectiveTenantPermissionsResponse getEffectivePermissions(String institutionId, String userId) {
        List<AssignmentEntry> userAssignments = assignmentStore.values().stream()
            .filter(a -> a.getInstitutionId().equalsIgnoreCase(institutionId))
            .filter(a -> a.getUserId().equalsIgnoreCase(userId))
            .collect(Collectors.toList());

        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        for (AssignmentEntry a : userAssignments) {
            roles.add(a.getRoleName());
            RoleEntry role = roleStore.get(a.getRoleId());
            if (role != null) {
                permissions.addAll(role.getPermissions());
            }
        }

        // Default admin permissions if unassigned demo user
        if ("usr-admin-1".equalsIgnoreCase(userId) || userAssignments.isEmpty()) {
            roles.add("College Admin");
            RoleEntry defaultAdmin = roleStore.get("role-college-admin");
            if (defaultAdmin != null) {
                permissions.addAll(defaultAdmin.getPermissions());
            }
        }

        // Story 36 Isolation Check: Ensure platform-tier permissions never leak
        permissions.removeIf(p -> p.startsWith("platform:"));

        return new EffectiveTenantPermissionsResponse(userId, institutionId, roles, permissions, Instant.now());
    }

    private static class RoleEntry {
        private final String id;
        private final String institutionId;
        private final String name;
        private final String description;
        private final Set<String> permissions;
        private final Instant createdAt;

        public RoleEntry(String id, String institutionId, String name, String description, Set<String> permissions, Instant createdAt) {
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

    private static class AssignmentEntry {
        private final String id;
        private final String institutionId;
        private final String userId;
        private final String roleId;
        private final String roleName;
        private final String scope;
        private final Instant grantedAt;

        public AssignmentEntry(String id, String institutionId, String userId, String roleId, String roleName, String scope, Instant grantedAt) {
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
}
