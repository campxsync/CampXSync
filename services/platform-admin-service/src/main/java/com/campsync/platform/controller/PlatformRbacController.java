package com.campsync.platform.controller;

import com.campsync.platform.dto.PlatformRbacDtos.*;
import com.campsync.platform.service.PlatformRbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@Tag(name = "Platform RBAC", description = "Platform role management, staff role assignment, and permission resolution")
public class PlatformRbacController {

    private final PlatformRbacService rbacService;

    public PlatformRbacController(PlatformRbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/platform-roles")
    @Operation(summary = "Define a platform role", description = "Creates a new platform-level role with specified permission set")
    public ResponseEntity<PlatformRoleResponse> createRole(@Valid @RequestBody CreatePlatformRoleRequest request) {
        PlatformRoleResponse response = rbacService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/platform-roles")
    @Operation(summary = "List all platform roles", description = "Retrieves all defined platform roles")
    public ResponseEntity<List<PlatformRoleResponse>> listRoles() {
        return ResponseEntity.ok(rbacService.listRoles());
    }

    @PostMapping("/platform-role-assignments")
    @Operation(summary = "Grant a platform role to a staff member", description = "Assigns a platform role to an internal staff member")
    public ResponseEntity<RoleAssignmentResponse> grantRole(
            @Valid @RequestBody GrantRoleRequest request,
            @RequestHeader(name = "X-Actor-Id", defaultValue = "super-admin") String actor) {
        RoleAssignmentResponse response = rbacService.grantRole(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/platform-role-assignments/{staff_id}/effective")
    @Operation(summary = "Resolve a staff member's effective permissions", description = "Resolves effective aggregated permissions for API gateway authorization")
    public ResponseEntity<EffectivePermissionsResponse> getEffectivePermissions(@PathVariable("staff_id") String staffId) {
        EffectivePermissionsResponse response = rbacService.getEffectivePermissions(staffId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/platform-role-assignments/{id}")
    @Operation(summary = "Revoke a platform role assignment", description = "Removes a staff member's role assignment immediately")
    public ResponseEntity<Void> revokeRole(@PathVariable String id) {
        rbacService.revokeRole(id);
        return ResponseEntity.noContent().build();
    }
}
