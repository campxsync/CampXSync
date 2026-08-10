package com.campsync.platform.controller;

import com.campsync.platform.dto.SecurityComplianceDtos.*;
import com.campsync.platform.service.SecurityComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/compliance-checks")
@Tag(name = "Security & Compliance", description = "Automated compliance policy evaluation and violation reporting")
public class SecurityComplianceController {

    private final SecurityComplianceService complianceService;

    public SecurityComplianceController(SecurityComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping("/run")
    @Operation(summary = "Trigger a compliance check", description = "Asynchronously runs policy compliance check against single institute or platform-wide")
    public ResponseEntity<ComplianceCheckTriggerResponse> runComplianceCheck(@RequestBody(required = false) RunComplianceCheckRequest request) {
        ComplianceCheckTriggerResponse response = complianceService.runComplianceCheck(request != null ? request : new RunComplianceCheckRequest(null, Collections.emptyList()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{institution_id}")
    @Operation(summary = "View an institute's latest compliance results", description = "Fetches recent policy compliance evaluation results")
    public ResponseEntity<ComplianceCheckResultResponse> getLatestResultForInstitute(@PathVariable("institution_id") String institutionId) {
        ComplianceCheckResultResponse response = complianceService.getLatestResultForInstitute(institutionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all non-compliant institutes", description = "Filters and returns all institutes currently flagged with policy violations")
    public ResponseEntity<List<ComplianceCheckResultResponse>> listNonCompliantInstitutes(@RequestParam(name = "flagged", defaultValue = "true") boolean flagged) {
        if (!flagged) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(complianceService.listNonCompliantInstitutes());
    }
}
