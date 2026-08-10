package com.campsync.college.service.impl;

import com.campsync.college.dto.CollegeAuditDtos.*;
import com.campsync.college.service.CollegeAuditService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollegeAuditServiceImpl implements CollegeAuditService {

    private final List<CollegeAuditLogResponse> tenantAuditLogs = new ArrayList<>();

    public CollegeAuditServiceImpl() {
        Map<String, Object> p1 = new HashMap<>(); p1.put("userId", "usr-101"); p1.put("profileType", "faculty");
        tenantAuditLogs.add(new CollegeAuditLogResponse(
            "tenant-audit-1", "inst-101", "UserCreated", "College Identity Service", "admin-usr-1", p1, Instant.now().minusSeconds(3600)
        ));

        Map<String, Object> p2 = new HashMap<>(); p2.put("key", "theme_color"); p2.put("newValue", "#003366");
        tenantAuditLogs.add(new CollegeAuditLogResponse(
            "tenant-audit-2", "inst-101", "CollegeConfigChanged", "College Configuration Service", "admin-usr-1", p2, Instant.now().minusSeconds(1800)
        ));

        Map<String, Object> p3 = new HashMap<>(); p3.put("roleId", "role-college-admin"); p3.put("userId", "usr-101");
        tenantAuditLogs.add(new CollegeAuditLogResponse(
            "tenant-audit-3", "inst-101", "RoleGranted", "College RBAC Service", "admin-usr-1", p3, Instant.now().minusSeconds(600)
        ));
    }

    @Override
    public PaginatedCollegeAuditLogsResponse queryAuditLogs(String institutionId, String eventType, String sourceModule, int page, int size) {
        // Story 33: Implicitly scoped strictly to caller's institutionId
        List<CollegeAuditLogResponse> filtered = tenantAuditLogs.stream()
            .filter(l -> l.getInstitutionId().equalsIgnoreCase(institutionId))
            .filter(l -> eventType == null || l.getEventType().equalsIgnoreCase(eventType))
            .filter(l -> sourceModule == null || l.getSourceModule().equalsIgnoreCase(sourceModule))
            .collect(Collectors.toList());

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<CollegeAuditLogResponse> pageContent = filtered.subList(fromIndex, toIndex);

        int totalPages = (int) Math.ceil((double) total / size);
        return new PaginatedCollegeAuditLogsResponse(pageContent, page, size, total, totalPages);
    }
}
