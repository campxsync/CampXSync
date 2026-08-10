package com.campsync.college.service.impl;

import com.campsync.college.dto.CollegeConfigDtos.*;
import com.campsync.college.service.CollegeConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class CollegeConfigServiceImpl implements CollegeConfigService {

    private final Map<String, ConfigEntry> configStore = new ConcurrentHashMap<>();
    private final List<ConfigAuditHistoryResponse> auditHistory = new CopyOnWriteArrayList<>();

    public CollegeConfigServiceImpl() {
        initConfig("inst-101", "theme_color", "#003366", "branding");
        initConfig("inst-101", "attendance_module_enabled", true, "feature_flag");
        initConfig("inst-101", "academic_year", "2026-2027", "operational");
    }

    private void initConfig(String instId, String key, Object value, String category) {
        String storeKey = instId + ":" + key;
        configStore.put(storeKey, new ConfigEntry(instId, key, value, category, Instant.now(), "SYSTEM_SEED"));
    }

    @Override
    public List<CollegeConfigResponse> getInstitutionConfigs(String institutionId) {
        return configStore.values().stream()
            .filter(c -> c.getInstitutionId().equalsIgnoreCase(institutionId))
            .map(c -> new CollegeConfigResponse(c.getKey(), c.getValue(), c.getCategory(), c.getInstitutionId(), c.getUpdatedAt(), c.getUpdatedBy()))
            .collect(Collectors.toList());
    }

    @Override
    public CollegeConfigResponse updateConfig(String institutionId, String key, Object value, String actor) {
        String storeKey = institutionId + ":" + key;
        ConfigEntry existing = configStore.get(storeKey);
        if (existing == null) {
            // Seed dynamically if new key
            existing = new ConfigEntry(institutionId, key, value, "operational", Instant.now(), actor);
        }

        Object previousValue = existing.getValue();
        ConfigEntry updated = new ConfigEntry(institutionId, key, value, existing.getCategory(), Instant.now(), actor);
        configStore.put(storeKey, updated);

        auditHistory.add(new ConfigAuditHistoryResponse(key, previousValue, value, institutionId, actor, Instant.now()));

        return new CollegeConfigResponse(updated.getKey(), updated.getValue(), updated.getCategory(), updated.getInstitutionId(), updated.getUpdatedAt(), updated.getUpdatedBy());
    }

    @Override
    public List<ConfigAuditHistoryResponse> getConfigHistory(String institutionId, String key) {
        return auditHistory.stream()
            .filter(h -> h.getInstitutionId().equalsIgnoreCase(institutionId))
            .filter(h -> key == null || h.getKey().equalsIgnoreCase(key))
            .collect(Collectors.toList());
    }

    private static class ConfigEntry {
        private final String institutionId;
        private final String key;
        private final Object value;
        private final String category;
        private final Instant updatedAt;
        private final String updatedBy;

        public ConfigEntry(String institutionId, String key, Object value, String category, Instant updatedAt, String updatedBy) {
            this.institutionId = institutionId;
            this.key = key;
            this.value = value;
            this.category = category;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
        }

        public String getInstitutionId() { return institutionId; }
        public String getKey() { return key; }
        public Object getValue() { return value; }
        public String getCategory() { return category; }
        public Instant getUpdatedAt() { return updatedAt; }
        public String getUpdatedBy() { return updatedBy; }
    }
}
