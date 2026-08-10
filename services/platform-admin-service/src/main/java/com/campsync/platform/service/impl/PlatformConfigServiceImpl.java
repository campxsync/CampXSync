package com.campsync.platform.service.impl;

import com.campsync.platform.dto.PlatformConfigDtos.*;
import com.campsync.platform.service.PlatformConfigService;
import logger.constants.AuditConstants;
import logger.logging.AppLogger;
import logger.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class PlatformConfigServiceImpl implements PlatformConfigService {

    private static final AppLogger log = AppLogger.getLogger(PlatformConfigServiceImpl.class);

    private final Map<String, ConfigEntry> configStore = new ConcurrentHashMap<>();
    private final List<ConfigAuditHistoryResponse> auditHistory = new CopyOnWriteArrayList<>();

    public PlatformConfigServiceImpl() {
        initConfig("maintenance_mode", false, "Global platform maintenance toggle");
        initConfig("file_upload_limit_mb", 50, "Max file upload size allowed per request in MB");
        initConfig("jwt_ttl_seconds", 86400, "Global JWT session token expiration time in seconds");
        initConfig("mfa_required", true, "Platform-wide mandatory multi-factor authentication requirement");
        log.info("Initialized PlatformConfigServiceImpl with global default configuration keys");
    }

    private void initConfig(String key, Object value, String description) {
        configStore.put(key, new ConfigEntry(key, value, description, Instant.now(), "SYSTEM"));
    }

    @Override
    public List<PlatformConfigResponse> getAllConfigs() {
        log.debug("Fetching all global platform configuration settings");
        return configStore.values().stream()
            .map(c -> new PlatformConfigResponse(c.getKey(), c.getValue(), c.getDescription(), c.getUpdatedAt(), c.getUpdatedBy()))
            .collect(Collectors.toList());
    }

    @Override
    public PlatformConfigResponse updateConfig(String key, Object value, String actor) {
        log.info("Updating platform config key='{}' to value='{}' by actor='{}'", key, value, actor);
        ConfigEntry existing = configStore.get(key);
        if (existing == null) {
            log.warn("Platform config key '{}' not found for update", key);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Platform config with key '" + key + "' not found.");
        }

        Object previousValue = existing.getValue();
        ConfigEntry updated = new ConfigEntry(key, value, existing.getDescription(), Instant.now(), actor);
        configStore.put(key, updated);

        auditHistory.add(new ConfigAuditHistoryResponse(key, previousValue, value, actor, Instant.now()));

        log.info("Successfully updated config key='{}'", key);
        AuditLogger.builder()
                .action(AuditConstants.ACTION_UPDATE)
                .entity("PLATFORM_CONFIG", key)
                .success()
                .message("Global platform config updated")
                .detail("previousValue", previousValue)
                .detail("newValue", value)
                .log();

        return new PlatformConfigResponse(updated.getKey(), updated.getValue(), updated.getDescription(), updated.getUpdatedAt(), updated.getUpdatedBy());
    }

    @Override
    public List<ConfigAuditHistoryResponse> getConfigHistory(String key) {
        return auditHistory.stream()
            .filter(h -> key == null || h.getKey().equalsIgnoreCase(key))
            .collect(Collectors.toList());
    }

    private static class ConfigEntry {
        private final String key;
        private final Object value;
        private final String description;
        private final Instant updatedAt;
        private final String updatedBy;

        public ConfigEntry(String key, Object value, String description, Instant updatedAt, String updatedBy) {
            this.key = key;
            this.value = value;
            this.description = description;
            this.updatedAt = updatedAt;
            this.updatedBy = updatedBy;
        }

        public String getKey() { return key; }
        public Object getValue() { return value; }
        public String getDescription() { return description; }
        public Instant getUpdatedAt() { return updatedAt; }
        public String getUpdatedBy() { return updatedBy; }
    }
}
