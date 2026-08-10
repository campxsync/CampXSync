package com.campsync.platform.service;

import com.campsync.platform.dto.PlatformRbacDtos.*;
import java.util.List;

public interface PlatformRbacService {
    PlatformRoleResponse createRole(CreatePlatformRoleRequest request);
    List<PlatformRoleResponse> listRoles();
    RoleAssignmentResponse grantRole(GrantRoleRequest request, String actor);
    void revokeRole(String assignmentId);
    EffectivePermissionsResponse getEffectivePermissions(String staffId);
}
