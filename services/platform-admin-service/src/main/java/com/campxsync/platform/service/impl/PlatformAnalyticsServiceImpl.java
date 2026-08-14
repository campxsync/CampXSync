package com.campxsync.platform.service.impl;

import com.campxsync.platform.dto.PlatformAnalyticsDtos.*;
import com.campxsync.platform.service.PlatformAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class PlatformAnalyticsServiceImpl implements PlatformAnalyticsService {

    @Override
    public AnalyticsSnapshotResponse getLatestSnapshot(String metric, String period) {
        String selectedMetric = metric != null ? metric : "active_institutions_count";
        String selectedPeriod = period != null ? period : "monthly";

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("activeInstitutions", 42);
        mockData.put("totalStudentsOnboarded", 125000);
        mockData.put("totalFacultyMembers", 8500);
        mockData.put("systemUptimePercentage", 99.98);

        return new AnalyticsSnapshotResponse(
            "snap-" + System.currentTimeMillis(),
            selectedMetric,
            selectedPeriod,
            mockData,
            Instant.now()
        );
    }

    @Override
    public RecomputeJobResponse triggerRecompute(RecomputeRequest request) {
        String jobId = "job-" + System.currentTimeMillis();
        return new RecomputeJobResponse(
            jobId,
            "ACCEPTED",
            "Analytics snapshot recompute job queued successfully.",
            Instant.now()
        );
    }
}
