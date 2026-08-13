package com.campxsync.platform.controller;

import com.campxsync.platform.dto.InstituteDtos.*;
import com.campxsync.platform.service.InstituteManagementService;
import logger.logging.AppLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/institutes")
@Tag(name = "Institute Management", description = "Provisioning, lifecycle management, and institute profile administration")
public class InstituteManagementController {

    private static final AppLogger log = AppLogger.getLogger(InstituteManagementController.class);

    private final InstituteManagementService instituteService;

    public InstituteManagementController(InstituteManagementService instituteService) {
        this.instituteService = instituteService;
    }

    @PostMapping
    @Operation(summary = "Provision a new institute", description = "Onboards a new institute with unique subdomain, plan, and initial onboarding state")
    public ResponseEntity<InstituteResponse> provisionInstitute(@Valid @RequestBody ProvisionInstituteRequest request) {
        log.info("REST POST /v1/institutes - Provisioning institute subdomain={}", request.getSubdomain());
        InstituteResponse response = instituteService.provisionInstitute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "View institute details", description = "Fetches complete detail record for a specific institution ID")
    public ResponseEntity<InstituteResponse> getInstituteById(@PathVariable String id) {
        InstituteResponse response = instituteService.getInstituteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List and filter institutes", description = "Returns a paginated list of institutes filtered by status or tenancy tier")
    public ResponseEntity<PaginatedInstitutesResponse> listInstitutes(
            @RequestParam(required = false) String status,
            @RequestParam(name = "tenancy_tier", required = false) String tenancyTier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedInstitutesResponse response = instituteService.listInstitutes(status, tenancyTier, page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition an institute's lifecycle status", description = "Enforces state machine transitions (onboarding->active, active<->suspended, active/suspended->offboarded)")
    public ResponseEntity<InstituteResponse> updateInstituteStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request) {
        InstituteResponse response = instituteService.updateInstituteStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update institute profile details", description = "Updates institute name, subdomain, or tenancy tier with uniqueness revalidation")
    public ResponseEntity<InstituteResponse> updateInstituteDetails(
            @PathVariable String id,
            @RequestBody UpdateInstituteRequest request) {
        InstituteResponse response = instituteService.updateInstituteDetails(id, request);
        return ResponseEntity.ok(response);
    }
}
