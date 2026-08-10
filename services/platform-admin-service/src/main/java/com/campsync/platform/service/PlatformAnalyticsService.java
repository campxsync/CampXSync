package com.campsync.platform.service;

import com.campsync.platform.dto.PlatformAnalyticsDtos.*;

public interface PlatformAnalyticsService {
    AnalyticsSnapshotResponse getLatestSnapshot(String metric, String period);
    RecomputeJobResponse triggerRecompute(RecomputeRequest request);
}
