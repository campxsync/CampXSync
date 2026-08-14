package com.campxsync.platform.service.impl;

import com.campxsync.platform.dto.InstituteDtos.*;
import com.campxsync.platform.service.InstituteManagementService;
import logger.constants.AuditConstants;
import logger.logging.AppLogger;
import logger.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import logger.enums.InstituteStatus;

@Service
public class InstituteManagementServiceImpl implements InstituteManagementService {

    private static final AppLogger log = AppLogger.getLogger(InstituteManagementServiceImpl.class);

    private final Map<String, InstituteRecord> instituteStore = new ConcurrentHashMap<>();
    private final Set<String> registeredSubdomains = Collections.synchronizedSet(new HashSet<>());
    private final List<String> publishedEvents = Collections.synchronizedList(new ArrayList<>());

    public InstituteManagementServiceImpl() {
        // Seed sample institute data
        InstituteRecord sample = new InstituteRecord(
            "inst-101", "Oxford Academy", "oxford", "plan-enterprise", "active", "STANDARD_TENANT", Instant.now(), Instant.now()
        );
        instituteStore.put(sample.getId(), sample);
        registeredSubdomains.add("oxford");
        log.info("Initialized InstituteManagementServiceImpl with sample institute inst-101");
    }

    @Override
    public InstituteResponse provisionInstitute(ProvisionInstituteRequest request) {
        log.info("Request received to provision institute: name={}, subdomain={}", request.getName(), request.getSubdomain());
        if (registeredSubdomains.contains(request.getSubdomain().toLowerCase())) {
            log.warn("Provisioning rejected due to duplicate subdomain: {}", request.getSubdomain());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Subdomain '" + request.getSubdomain() + "' is already in use.");
        }

        String id = "inst-" + UUID.randomUUID().toString().replace("-", "");
        String tenancyTier = request.getTenancyTier() != null ? request.getTenancyTier() : "STANDARD_TENANT";
        Instant now = Instant.now();

        InstituteRecord record = new InstituteRecord(
            id, request.getName(), request.getSubdomain().toLowerCase(), request.getPlanId(), "onboarding", tenancyTier, now, now
        );

        registeredSubdomains.add(request.getSubdomain().toLowerCase());
        instituteStore.put(id, record);
        publishedEvents.add("InstituteOnboarded:" + id);

        log.info("Successfully provisioned institute id={} with status=onboarding", id);
        AuditLogger.builder()
                .action(AuditConstants.ACTION_CREATE)
                .entity("INSTITUTE", id)
                .success()
                .message("Institute provisioned successfully")
                .detail("subdomain", request.getSubdomain())
                .detail("planId", request.getPlanId())
                .log();

        return mapToResponse(record);
    }

    @Override
    public InstituteResponse getInstituteById(String id) {
        InstituteRecord record = instituteStore.get(id);
        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute with ID '" + id + "' not found.");
        }
        return mapToResponse(record);
    }

    @Override
    public PaginatedInstitutesResponse listInstitutes(String status, String tenancyTier, int page, int size) {
        List<InstituteRecord> filtered = instituteStore.values().stream()
            .filter(i -> status == null || i.getStatus().equalsIgnoreCase(status))
            .filter(i -> tenancyTier == null || i.getTenancyTier().equalsIgnoreCase(tenancyTier))
            .collect(Collectors.toList());

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<InstituteResponse> pageContent = filtered.subList(fromIndex, toIndex).stream()
            .sorted(java.util.Comparator.comparing(InstituteRecord::getCreatedAt))
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / size);
        return new PaginatedInstitutesResponse(pageContent, page, size, total, totalPages);
    }

    @Override
    public InstituteResponse updateInstituteStatus(String id, String newStatus) {
        InstituteRecord existing = instituteStore.get(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute with ID '" + id + "' not found.");
        }

        InstituteStatus currentStatusEnum;
        try {
            currentStatusEnum = InstituteStatus.fromApiValue(existing.getStatus());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Stored institute has unrecognised status: '" + existing.getStatus() + "'");
        }

        InstituteStatus targetStatusEnum;
        try {
            targetStatusEnum = InstituteStatus.fromApiValue(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        if (!currentStatusEnum.canTransitionTo(targetStatusEnum)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid lifecycle status transition from '" + existing.getStatus() + "' to '" + newStatus + "'");
        }

        InstituteRecord updated = new InstituteRecord(
            existing.getId(), existing.getName(), existing.getSubdomain(), existing.getPlanId(),
            targetStatusEnum.toApiValue(), existing.getTenancyTier(), existing.getCreatedAt(), Instant.now()
        );

        instituteStore.put(id, updated);

        // Story 6 Event Publishing Acceptance Criteria:
        // Publish InstituteSuspended on target=suspended, InstituteOffboarded on target=offboarded,
        // and InstituteStatusChanged on every successful transition regardless of target state.
        if (targetStatusEnum == InstituteStatus.SUSPENDED) {
            publishedEvents.add("InstituteSuspended:" + id);
        } else if (targetStatusEnum == InstituteStatus.OFFBOARDED) {
            publishedEvents.add("InstituteOffboarded:" + id);
        }
        publishedEvents.add("InstituteStatusChanged:" + id + ":" + currentStatusEnum.toApiValue() + "->" + targetStatusEnum.toApiValue());

        return mapToResponse(updated);
    }

    @Override
    public InstituteResponse updateInstituteDetails(String id, UpdateInstituteRequest request) {
        InstituteRecord existing = instituteStore.get(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Institute with ID '" + id + "' not found.");
        }

        String newSubdomain = existing.getSubdomain();
        if (request.getSubdomain() != null && !request.getSubdomain().equalsIgnoreCase(existing.getSubdomain())) {
            if (registeredSubdomains.contains(request.getSubdomain().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Subdomain '" + request.getSubdomain() + "' is already in use.");
            }
            registeredSubdomains.remove(existing.getSubdomain());
            registeredSubdomains.add(request.getSubdomain().toLowerCase());
            newSubdomain = request.getSubdomain().toLowerCase();
        }

        String newName = request.getName() != null ? request.getName() : existing.getName();
        String newTier = request.getTenancyTier() != null ? request.getTenancyTier() : existing.getTenancyTier();

        InstituteRecord updated = new InstituteRecord(
            existing.getId(), newName, newSubdomain, existing.getPlanId(),
            existing.getStatus(), newTier, existing.getCreatedAt(), Instant.now()
        );

        instituteStore.put(id, updated);
        return mapToResponse(updated);
    }

    private InstituteResponse mapToResponse(InstituteRecord r) {
        return new InstituteResponse(
            r.getId(), r.getName(), r.getSubdomain(), r.getPlanId(), r.getStatus(), r.getTenancyTier(), r.getCreatedAt(), r.getUpdatedAt()
        );
    }

    private static class InstituteRecord {
        private final String id;
        private final String name;
        private final String subdomain;
        private final String planId;
        private final String status;
        private final String tenancyTier;
        private final Instant createdAt;
        private final Instant updatedAt;

        public InstituteRecord(String id, String name, String subdomain, String planId, String status, String tenancyTier, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.name = name;
            this.subdomain = subdomain;
            this.planId = planId;
            this.status = status;
            this.tenancyTier = tenancyTier;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getSubdomain() { return subdomain; }
        public String getPlanId() { return planId; }
        public String getStatus() { return status; }
        public String getTenancyTier() { return tenancyTier; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }
}
