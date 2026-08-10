package com.campsync.platform.controller;

import com.campsync.platform.dto.PlatformConfigDtos.*;
import com.campsync.platform.service.PlatformConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/platform-configs")
@Tag(name = "Platform Configuration", description = "Global feature flags, platform-wide settings, and configuration change history")
public class PlatformConfigController {

    private final PlatformConfigService configService;

    public PlatformConfigController(PlatformConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    @Operation(summary = "View current platform-wide configuration", description = "Returns all global feature flags and active settings")
    public ResponseEntity<List<PlatformConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @PatchMapping("/{key}")
    @Operation(summary = "Update a global configuration setting", description = "Updates a specific feature flag or configuration setting by key")
    public ResponseEntity<PlatformConfigResponse> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody UpdateConfigRequest request,
            @RequestHeader(name = "X-Actor-Id", defaultValue = "super-admin") String actor) {
        PlatformConfigResponse response = configService.updateConfig(key, request.getValue(), actor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "View configuration change history", description = "Returns audit log history of platform configuration changes")
    public ResponseEntity<List<ConfigAuditHistoryResponse>> getConfigHistory(
            @RequestParam(required = false) String key) {
        return ResponseEntity.ok(configService.getConfigHistory(key));
    }
}
