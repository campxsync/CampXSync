package com.campsync.platform.service.impl;

import com.campsync.platform.dto.SecurityComplianceDtos.*;
import com.campsync.platform.service.SecurityComplianceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SecurityComplianceServiceImpl implements SecurityComplianceService {

    private final Map<String, ComplianceCheckResultResponse> resultsStore = new ConcurrentHashMap<>();

    public SecurityComplianceServiceImpl() {
        resultsStore.put("inst-101", new ComplianceCheckResultResponse(
            "chk-101", "inst-101", true, Collections.emptyList(), Instant.now()
        ));
        resultsStore.put("inst-102", new ComplianceCheckResultResponse(
            "chk-102", "inst-102", false,
            Collections.singletonList(new ViolationDetail("pol-101", "GDPR Data Retention", "HIGH", "Data retained past 7-year threshold without archival")),
            Instant.now()
        ));
    }

    @Override
    public ComplianceCheckTriggerResponse runComplianceCheck(RunComplianceCheckRequest request) {
        String checkRef = "chk-ref-" + System.currentTimeMillis();
        String scope = request.getInstitutionId() != null ? request.getInstitutionId() : "PLATFORM_WIDE";

        return new ComplianceCheckTriggerResponse(checkRef, scope, "QUEUED", Instant.now());
    }

    @Override
    public ComplianceCheckResultResponse getLatestResultForInstitute(String institutionId) {
        return resultsStore.computeIfAbsent(institutionId, id -> new ComplianceCheckResultResponse(
            "chk-" + UUID.randomUUID().toString().substring(0, 8),
            id, true, Collections.emptyList(), Instant.now()
        ));
    }

    @Override
    public List<ComplianceCheckResultResponse> listNonCompliantInstitutes() {
        return resultsStore.values().stream()
            .filter(r -> !r.isCompliant())
            .collect(Collectors.toList());
    }
}
