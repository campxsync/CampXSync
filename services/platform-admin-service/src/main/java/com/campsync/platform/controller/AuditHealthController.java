package com.campsync.platform.controller;

import com.campsync.platform.dto.AuditHealthDtos.*;
import com.campsync.platform.service.AuditHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@Tag(name = "Audit & System Health", description = "Platform audit trail querying and aggregated platform health monitoring")
public class AuditHealthController {

    private final AuditHealthService auditHealthService;

    public AuditHealthController(AuditHealthService auditHealthService) {
        this.auditHealthService = auditHealthService;
    }

    @GetMapping("/platform-audit-logs")
    @Operation(summary = "Query the platform audit trail", description = "Fetches immutable append-only audit trail entries filtered by event_type or institution_id")
    public ResponseEntity<PaginatedAuditLogsResponse> queryAuditLogs(
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(name = "institution_id", required = false) String institutionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedAuditLogsResponse response = auditHealthService.queryAuditLogs(eventType, institutionId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-health")
    @Operation(summary = "View aggregated system health", description = "Provides liveness and readiness status aggregated across all platform-tier microservices")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        SystemHealthResponse response = auditHealthService.getSystemHealth();
        return ResponseEntity.ok(response);
    }
}
