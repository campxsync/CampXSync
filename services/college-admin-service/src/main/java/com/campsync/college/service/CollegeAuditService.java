package com.campxsync.college.service;

import com.campxsync.college.dto.CollegeAuditDtos.*;

public interface CollegeAuditService {
    PaginatedCollegeAuditLogsResponse queryAuditLogs(String institutionId, String eventType, String sourceModule, int page, int size);
}
