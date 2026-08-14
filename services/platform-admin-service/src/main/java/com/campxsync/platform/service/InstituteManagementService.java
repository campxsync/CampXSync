package com.campxsync.platform.service;

import com.campxsync.platform.dto.InstituteDtos.*;

public interface InstituteManagementService {
    InstituteResponse provisionInstitute(ProvisionInstituteRequest request);
    InstituteResponse getInstituteById(String id);
    PaginatedInstitutesResponse listInstitutes(String status, String tenancyTier, int page, int size);
    InstituteResponse updateInstituteStatus(String id, String newStatus);
    InstituteResponse updateInstituteDetails(String id, UpdateInstituteRequest request);
}
