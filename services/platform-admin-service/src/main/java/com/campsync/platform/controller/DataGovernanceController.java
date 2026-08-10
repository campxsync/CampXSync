package com.campsync.platform.controller;

import com.campsync.platform.dto.DataGovernanceDtos.*;
import com.campsync.platform.service.DataGovernanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/policies")
@Tag(name = "Data Governance", description = "Retention, residency, and access policy definition and enforcement")
public class DataGovernanceController {

    private final DataGovernanceService governanceService;

    public DataGovernanceController(DataGovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    @PostMapping
    @Operation(summary = "Create a data governance policy", description = "Defines a named retention, residency, or access policy")
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = governanceService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List policies for an institute or platform-wide", description = "Returns active policies matching the applies_to query parameter")
    public ResponseEntity<List<PolicyResponse>> listPolicies(@RequestParam(name = "applies_to", required = false) String appliesTo) {
        return ResponseEntity.ok(governanceService.listPolicies(appliesTo));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a policy and trigger re-evaluation", description = "Updates an existing data governance policy rule")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable String id,
            @RequestBody UpdatePolicyRequest request) {
        PolicyResponse response = governanceService.updatePolicy(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retire a policy", description = "Flips a policy status to RETIRED while preserving historical audit records")
    public ResponseEntity<PolicyResponse> retirePolicy(@PathVariable String id) {
        PolicyResponse response = governanceService.retirePolicy(id);
        return ResponseEntity.ok(response);
    }
}
