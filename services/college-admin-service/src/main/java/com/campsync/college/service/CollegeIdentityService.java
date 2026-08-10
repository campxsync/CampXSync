package com.campsync.college.service;

import com.campsync.college.dto.CollegeIdentityDtos.*;

public interface CollegeIdentityService {
    UserResponse createUser(String institutionId, CreateUserRequest request);
    UserResponse getUserById(String institutionId, String id);
    PaginatedUsersResponse listUsers(String institutionId, String profileType, String status, int page, int size);
    UserResponse updateUserStatus(String institutionId, String id, String status);
    UserResponse updateUserProfile(String institutionId, String id, UpdateUserProfileRequest request);
}
