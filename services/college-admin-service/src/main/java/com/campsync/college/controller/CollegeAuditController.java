package com.campsync.college.controller;

import com.campsync.college.dto.CollegeAuditDtos.*;
import com.campsync.college.service.CollegeAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/audit-logs")
@Tag(name = "College Audit & Compliance", description = "Institution-scoped audit trail querying")
public class CollegeAuditController {

    private final CollegeAuditService auditService;

    public CollegeAuditController(CollegeAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Query the institution's audit trail", description = "Fetches paginated audit logs implicitly scoped to the caller's institution")
    public ResponseEntity<PaginatedCollegeAuditLogsResponse> queryAuditLogs(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedCollegeAuditLogsResponse response = auditService.queryAuditLogs(institutionId, eventType, source, page, size);
        return ResponseEntity.ok(response);
    }
}
