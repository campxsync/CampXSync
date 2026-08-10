package com.campsync.college.service;

import com.campsync.college.dto.CollegeRbacDtos.*;
import java.util.List;

public interface CollegeRbacService {
    CollegeRoleResponse createRole(String institutionId, CreateCustomRoleRequest request);
    List<CollegeRoleResponse> listRoles(String institutionId);
    UserRoleAssignmentResponse grantRole(String institutionId, GrantUserRoleRequest request);
    void revokeRole(String institutionId, String assignmentId);
    EffectiveTenantPermissionsResponse getEffectivePermissions(String institutionId, String userId);
}
