package com.campsync.college.controller;

import com.campsync.college.dto.CollegeConfigDtos.*;
import com.campsync.college.service.CollegeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/college-configs")
@Tag(name = "College Configuration", description = "Institution feature flags, branding, and operational settings")
public class CollegeConfigController {

    private final CollegeConfigService configService;

    public CollegeConfigController(CollegeConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @Operation(summary = "View this institution's configuration", description = "Returns active feature flags, branding, and operational settings for the caller's institution")
    public ResponseEntity<List<CollegeConfigResponse>> getInstitutionConfigs(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId) {
        return ResponseEntity.ok(configService.getInstitutionConfigs(institutionId));
    }

    @PatchMapping("/{key}")
    @Operation(summary = "Update a college configuration setting", description = "Updates a setting (branding, feature flag, operational) scoped to the caller's institution")
    public ResponseEntity<CollegeConfigResponse> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody UpdateCollegeConfigRequest request,
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestHeader(name = "X-Actor-Id", defaultValue = "college-admin") String actor) {
        CollegeConfigResponse response = configService.updateConfig(institutionId, key, request.getValue(), actor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "View configuration change history", description = "Returns an ordered audit history of config setting changes for this institution")
    public ResponseEntity<List<ConfigAuditHistoryResponse>> getConfigHistory(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestParam(required = false) String key) {
        return ResponseEntity.ok(configService.getConfigHistory(institutionId, key));
    }
}
