package com.campsync.college.controller;

import com.campsync.college.dto.CollegeAnalyticsDtos.*;
import com.campsync.college.service.CollegeAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/college-analytics")
@Tag(name = "College Reports & Analytics", description = "Institution analytics dashboard rollups and snapshot recomputation")
public class CollegeAnalyticsController {

    private final CollegeAnalyticsService analyticsService;

    public CollegeAnalyticsController(CollegeAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "View an institution's analytics dashboard", description = "Fetches precomputed analytics rollups (enrollments, fee collections, exam results) for institution")
    public ResponseEntity<CollegeAnalyticsDashboardResponse> getDashboard(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) String period) {
        CollegeAnalyticsDashboardResponse response = analyticsService.getDashboard(institutionId, metric, period);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recompute")
    @Operation(summary = "Manually trigger an analytics recompute", description = "Asynchronously triggers recomputation of analytics rollups for the caller's institution")
    public ResponseEntity<RecomputeResponse> triggerRecompute(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestBody(required = false) TriggerRecomputeRequest request) {
        RecomputeResponse response = analyticsService.triggerRecompute(institutionId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
