package com.campsync.platform.service;

import com.campsync.platform.dto.PlatformConfigDtos.*;
import java.util.List;

public interface PlatformConfigService {
    List<PlatformConfigResponse> getAllConfigs();
    PlatformConfigResponse updateConfig(String key, Object value, String actor);
    List<ConfigAuditHistoryResponse> getConfigHistory(String key);
}
