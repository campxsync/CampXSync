package com.campsync.college.service;

import com.campsync.college.dto.CollegeAnalyticsDtos.*;

public interface CollegeAnalyticsService {
    CollegeAnalyticsDashboardResponse getDashboard(String institutionId, String metric, String period);
    RecomputeResponse triggerRecompute(String institutionId, TriggerRecomputeRequest request);
}
