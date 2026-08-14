package com.campxsync.college.controller;

import com.campxsync.college.dto.CollegeRbacDtos.*;
import com.campxsync.college.service.CollegeRbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@Tag(name = "College RBAC", description = "Tenant role definitions, user role assignments, and effective permission resolution")
public class CollegeRbacController {

    private final CollegeRbacService rbacService;

    public CollegeRbacController(CollegeRbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/roles")
    @Operation(summary = "Define a custom role", description = "Creates a custom role scoped to institution after validating permissions against global catalog")
    public ResponseEntity<CollegeRoleResponse> createRole(
            @RequestHeader(name = "X-Institution-Id", required = true) String institutionId,
            @Valid @RequestBody CreateCustomRoleRequest request) {
        CollegeRoleResponse response = rbacService.createRole(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/roles")
    @Operation(summary = "List custom roles for institution", description = "Returns all custom roles defined for the caller's institution")
    public ResponseEntity<List<CollegeRoleResponse>> listRoles(
            @RequestHeader(name = "X-Institution-Id", required = true) String institutionId) {
        return ResponseEntity.ok(rbacService.listRoles(institutionId));
    }

    @PostMapping("/role-assignments")
    @Operation(summary = "Grant a role to a user", description = "Assigns a role to a user optionally scoped to department/hostel/program")
    public ResponseEntity<UserRoleAssignmentResponse> grantRole(
            @RequestHeader(name = "X-Institution-Id", required = true) String institutionId,
            @Valid @RequestBody GrantUserRoleRequest request) {
        UserRoleAssignmentResponse response = rbacService.grantRole(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/role-assignments/{user_id}/effective")
    @Operation(summary = "Resolve a user's effective permissions", description = "Resolves effective permissions for a user within their institution scope")
    public ResponseEntity<EffectiveTenantPermissionsResponse> getEffectivePermissions(
            @RequestHeader(name = "X-Institution-Id", required = true) String institutionId,
            @PathVariable("user_id") String userId) {
        EffectiveTenantPermissionsResponse response = rbacService.getEffectivePermissions(institutionId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/role-assignments/{id}")
    @Operation(summary = "Revoke a role assignment", description = "Revokes a user's role assignment immediately")
    public ResponseEntity<Void> revokeRole(
            @RequestHeader(name = "X-Institution-Id", required = true) String institutionId,
            @PathVariable String id) {
        rbacService.revokeRole(institutionId, id);
        return ResponseEntity.noContent().build();
    }
}
