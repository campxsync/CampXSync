package com.campxsync.platform.service;

import com.campxsync.platform.dto.PlatformAnalyticsDtos.*;

public interface PlatformAnalyticsService {
    AnalyticsSnapshotResponse getLatestSnapshot(String metric, String period);
    RecomputeJobResponse triggerRecompute(RecomputeRequest request);
}
