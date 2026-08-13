package com.campxsync.platform.service;

import com.campxsync.platform.dto.AuditHealthDtos.*;

public interface AuditHealthService {
    PaginatedAuditLogsResponse queryAuditLogs(String eventType, String institutionId, int page, int size);
    SystemHealthResponse getSystemHealth();
}
