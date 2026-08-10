package com.campsync.college.service;

import com.campsync.college.dto.CollegeAuditDtos.*;

public interface CollegeAuditService {
    PaginatedCollegeAuditLogsResponse queryAuditLogs(String institutionId, String eventType, String sourceModule, int page, int size);
}
