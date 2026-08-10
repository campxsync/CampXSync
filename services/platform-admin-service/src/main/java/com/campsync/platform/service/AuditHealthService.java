package com.campsync.platform.service;

import com.campsync.platform.dto.AuditHealthDtos.*;

public interface AuditHealthService {
    PaginatedAuditLogsResponse queryAuditLogs(String eventType, String institutionId, int page, int size);
    SystemHealthResponse getSystemHealth();
}
