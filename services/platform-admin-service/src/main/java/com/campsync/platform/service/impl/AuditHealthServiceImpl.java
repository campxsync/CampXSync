package com.campsync.platform.service.impl;

import com.campsync.platform.dto.AuditHealthDtos.*;
import com.campsync.platform.service.AuditHealthService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditHealthServiceImpl implements AuditHealthService {

    private final List<AuditLogResponse> auditLogs = new ArrayList<>();

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

        Map<String, Object> d1 = new HashMap<>(); d1.put("latencyMs", 12);
        services.put("institute-management-service", new ServiceHealthDetail("Institute Management", "UP", d1));

        Map<String, Object> d2 = new HashMap<>(); d2.put("cacheHitRate", "99.4%");
        services.put("platform-config-service", new ServiceHealthDetail("Platform Configuration", "UP", d2));

        Map<String, Object> d3 = new HashMap<>(); d3.put("redisSessionCache", "CONNECTED");
        services.put("platform-rbac-service", new ServiceHealthDetail("Platform RBAC", "UP", d3));

        Map<String, Object> d4 = new HashMap<>(); d4.put("activePolicies", 12);
        services.put("data-governance-service", new ServiceHealthDetail("Data Governance", "UP", d4));

        Map<String, Object> d5 = new HashMap<>(); d5.put("gatewayStatus", "ONLINE");
        services.put("billing-subscription-service", new ServiceHealthDetail("Billing & Subscription", "UP", d5));

        Map<String, Object> d6 = new HashMap<>(); d6.put("kafkaConsumerState", "STREAMING");
        services.put("platform-analytics-service", new ServiceHealthDetail("Platform Analytics", "UP", d6));

        Map<String, Object> d7 = new HashMap<>(); d7.put("lastCheckTime", Instant.now().toString());
        services.put("security-compliance-service", new ServiceHealthDetail("Security & Compliance", "UP", d7));

        Map<String, Object> d8 = new HashMap<>(); d8.put("storageType", "AppendOnlyStorage");
        services.put("audit-system-health-service", new ServiceHealthDetail("Audit & System Health", "UP", d8));

        return new SystemHealthResponse("UP", Instant.now(), services);
    }
}
