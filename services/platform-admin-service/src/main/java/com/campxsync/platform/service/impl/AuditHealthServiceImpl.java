package com.campxsync.platform.service.impl;

import com.campxsync.platform.dto.AuditHealthDtos.*;
import com.campxsync.platform.service.AuditHealthService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class AuditHealthServiceImpl implements AuditHealthService {

    // CopyOnWriteArrayList used instead of ArrayList for thread-safety under concurrent reads/writes
    private final List<AuditLogResponse> auditLogs = new CopyOnWriteArrayList<>();

    public AuditHealthServiceImpl() {
        Map<String, Object> p1 = new HashMap<>();
        p1.put("subdomain", "oxford");
        p1.put("planId", "plan-enterprise");
        auditLogs.add(new AuditLogResponse(
            "audit-1", "InstituteOnboarded", "inst-101", "admin-super-1",
            p1, Instant.now().minusSeconds(3600)
        ));

        Map<String, Object> p2 = new HashMap<>();
        p2.put("key", "mfa_required");
        p2.put("newValue", true);
        auditLogs.add(new AuditLogResponse(
            "audit-2", "PlatformConfigChanged", "PLATFORM", "admin-super-1",
            p2, Instant.now().minusSeconds(1800)
        ));

        Map<String, Object> p3 = new HashMap<>();
        p3.put("violation", "GDPR retention policy exceeded");
        auditLogs.add(new AuditLogResponse(
            "audit-3", "ComplianceFlagRaised", "inst-102", "SYSTEM",
            p3, Instant.now().minusSeconds(600)
        ));
    }

    @Override
    public PaginatedAuditLogsResponse queryAuditLogs(String eventType, String institutionId, int page, int size) {
        List<AuditLogResponse> filtered = auditLogs.stream()
            .filter(l -> eventType == null || l.getEventType().equalsIgnoreCase(eventType))
            .filter(l -> institutionId == null || l.getInstitutionId().equalsIgnoreCase(institutionId))
            .collect(Collectors.toList());

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<AuditLogResponse> pageContent = filtered.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) total / size);
        return new PaginatedAuditLogsResponse(pageContent, page, size, total, totalPages);
    }

    @Override
    public SystemHealthResponse getSystemHealth() {
        Map<String, ServiceHealthDetail> services = new LinkedHashMap<>();

        // Self health: report only what this service actually knows
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        int auditLogCount = auditLogs.size();

        Map<String, Object> selfDetail = new LinkedHashMap<>();
        selfDetail.put("storedAuditLogCount", auditLogCount);
        selfDetail.put("usedMemoryMb", usedMemoryMb);
        selfDetail.put("maxMemoryMb", maxMemoryMb);
        selfDetail.put("reportedAt", Instant.now().toString());
        services.put("audit-system-health-service", new ServiceHealthDetail("Audit & System Health", "UP", selfDetail));

        // External dependencies: reported as UNKNOWN — this service has no active connection
        // to these services and cannot honestly report their status.
        // TODO: Integrate with Spring Boot Actuator HealthIndicator API to get real dependency health.
        Map<String, Object> unknownDetail = new LinkedHashMap<>();
        unknownDetail.put("note", "Health check not yet integrated. Status reflects no real probe.");
        services.put("institute-management-service", new ServiceHealthDetail("Institute Management", "UNKNOWN", unknownDetail));
        services.put("platform-config-service", new ServiceHealthDetail("Platform Configuration", "UNKNOWN", unknownDetail));
        services.put("platform-rbac-service", new ServiceHealthDetail("Platform RBAC", "UNKNOWN", unknownDetail));
        services.put("data-governance-service", new ServiceHealthDetail("Data Governance", "UNKNOWN", unknownDetail));
        services.put("billing-subscription-service", new ServiceHealthDetail("Billing & Subscription", "UNKNOWN", unknownDetail));
        services.put("platform-analytics-service", new ServiceHealthDetail("Platform Analytics", "UNKNOWN", unknownDetail));
        services.put("security-compliance-service", new ServiceHealthDetail("Security & Compliance", "UNKNOWN", unknownDetail));

        return new SystemHealthResponse("UP", Instant.now(), services);
    }
}
