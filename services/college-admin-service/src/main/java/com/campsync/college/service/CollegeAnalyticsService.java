package com.campxsync.college.service;

import com.campxsync.college.dto.CollegeAnalyticsDtos.*;

public interface CollegeAnalyticsService {
    CollegeAnalyticsDashboardResponse getDashboard(String institutionId, String metric, String period);
    RecomputeResponse triggerRecompute(String institutionId, TriggerRecomputeRequest request);
}
