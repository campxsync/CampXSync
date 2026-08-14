package com.campxsync.platform.service;

import com.campxsync.platform.dto.PlatformRbacDtos.*;
import java.util.List;

public interface PlatformRbacService {
    PlatformRoleResponse createRole(CreatePlatformRoleRequest request);
    List<PlatformRoleResponse> listRoles();
    RoleAssignmentResponse grantRole(GrantRoleRequest request, String actor);
    void revokeRole(String assignmentId);
    EffectivePermissionsResponse getEffectivePermissions(String staffId);
}
