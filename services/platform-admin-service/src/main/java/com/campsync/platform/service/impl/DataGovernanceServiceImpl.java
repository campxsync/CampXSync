package com.campsync.platform.service.impl;

import com.campsync.platform.dto.DataGovernanceDtos.*;
import com.campsync.platform.service.DataGovernanceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DataGovernanceServiceImpl implements DataGovernanceService {

    private final Map<String, PolicyEntry> policyStore = new ConcurrentHashMap<>();

    public DataGovernanceServiceImpl() {
        String defaultId = "pol-101";
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("retentionYears", 7);
        ruleMap.put("autoPurge", true);
        policyStore.put(defaultId, new PolicyEntry(
            defaultId, "GDPR 7-Year Student Data Retention", "retention", "all",
            ruleMap, "ACTIVE", Instant.now(), Instant.now()
        ));
    }

    @Override
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        String id = "pol-" + UUID.randomUUID().toString().substring(0, 8);
        Instant now = Instant.now();

        PolicyEntry entry = new PolicyEntry(
            id, request.getName(), request.getType(), request.getAppliesTo(),
            request.getRule() != null ? request.getRule() : Collections.emptyMap(), "ACTIVE", now, now
        );

        policyStore.put(id, entry);
        return mapToResponse(entry);
    }

    @Override
    public List<PolicyResponse> listPolicies(String appliesTo) {
        return policyStore.values().stream()
            .filter(p -> appliesTo == null || p.getAppliesTo().equalsIgnoreCase(appliesTo) || p.getAppliesTo().equalsIgnoreCase("all"))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public PolicyResponse updatePolicy(String id, UpdatePolicyRequest request) {
        PolicyEntry existing = policyStore.get(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy with ID '" + id + "' not found.");
        }

        PolicyEntry updated = new PolicyEntry(
            existing.getId(),
            request.getName() != null ? request.getName() : existing.getName(),
            request.getType() != null ? request.getType() : existing.getType(),
            request.getAppliesTo() != null ? request.getAppliesTo() : existing.getAppliesTo(),
            request.getRule() != null ? request.getRule() : existing.getRule(),
            existing.getStatus(),
            existing.getCreatedAt(),
            Instant.now()
        );

        policyStore.put(id, updated);
        return mapToResponse(updated);
    }

    @Override
    public PolicyResponse retirePolicy(String id) {
        PolicyEntry existing = policyStore.get(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Policy with ID '" + id + "' not found.");
        }

        PolicyEntry retired = new PolicyEntry(
            existing.getId(), existing.getName(), existing.getType(), existing.getAppliesTo(),
            existing.getRule(), "RETIRED", existing.getCreatedAt(), Instant.now()
        );

        policyStore.put(id, retired);
        return mapToResponse(retired);
    }

    private PolicyResponse mapToResponse(PolicyEntry p) {
        return new PolicyResponse(p.getId(), p.getName(), p.getType(), p.getAppliesTo(), p.getRule(), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }

    private static class PolicyEntry {
        private final String id;
        private final String name;
        private final String type;
        private final String appliesTo;
        private final Map<String, Object> rule;
        private final String status;
        private final Instant createdAt;
        private final Instant updatedAt;

        public PolicyEntry(String id, String name, String type, String appliesTo, Map<String, Object> rule, String status, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.appliesTo = appliesTo;
            this.rule = rule;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getAppliesTo() { return appliesTo; }
        public Map<String, Object> getRule() { return rule; }
        public String getStatus() { return status; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }
}
