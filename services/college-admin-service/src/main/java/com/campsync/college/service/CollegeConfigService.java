package com.campxsync.college.service;

import com.campxsync.college.dto.CollegeConfigDtos.*;
import java.util.List;

public interface CollegeConfigService {
    List<CollegeConfigResponse> getInstitutionConfigs(String institutionId);
    CollegeConfigResponse updateConfig(String institutionId, String key, Object value, String actor);
    List<ConfigAuditHistoryResponse> getConfigHistory(String institutionId, String key);
}
