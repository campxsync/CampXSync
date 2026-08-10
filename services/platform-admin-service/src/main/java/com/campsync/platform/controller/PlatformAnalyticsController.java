package com.campsync.platform.controller;

import com.campsync.platform.dto.PlatformAnalyticsDtos.*;
import com.campsync.platform.service.PlatformAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/analytics")
@Tag(name = "Platform Analytics", description = "Cross-institution rollups and manual analytics snapshot recomputation")
public class PlatformAnalyticsController {

    private final PlatformAnalyticsService analyticsService;

    public PlatformAnalyticsController(PlatformAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/snapshots")
    @Operation(summary = "View a cross-institution analytics rollup", description = "Fetches precomputed metric snapshots across all active institutes")
    public ResponseEntity<AnalyticsSnapshotResponse> getLatestSnapshot(
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) String period) {
        AnalyticsSnapshotResponse response = analyticsService.getLatestSnapshot(metric, period);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/snapshots/recompute")
    @Operation(summary = "Manually trigger an analytics recompute", description = "Asynchronously triggers snapshot recomputation outside normal schedule")
    public ResponseEntity<RecomputeJobResponse> triggerRecompute(@RequestBody(required = false) RecomputeRequest request) {
        RecomputeJobResponse response = analyticsService.triggerRecompute(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
