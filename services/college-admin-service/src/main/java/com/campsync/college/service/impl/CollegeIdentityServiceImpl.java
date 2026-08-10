package com.campsync.college.service.impl;

import com.campsync.college.dto.CollegeIdentityDtos.*;
import com.campsync.college.service.CollegeIdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CollegeIdentityServiceImpl implements CollegeIdentityService {

    private final Map<String, UserRecord> userStore = new ConcurrentHashMap<>();

    public CollegeIdentityServiceImpl() {
        Map<String, Object> prof = new HashMap<>();
        prof.put("department", "Computer Science");
        UserRecord defaultUser = new UserRecord(
            "usr-101", "inst-101", "faculty", "Prof. Alan Turing", "turing@oxford.edu", "active", prof, Instant.now(), Instant.now()
        );
        userStore.put(defaultUser.getId(), defaultUser);
    }

    @Override
    public UserResponse createUser(String institutionId, CreateUserRequest request) {
        String id = "usr-" + UUID.randomUUID().toString().substring(0, 8);
        Instant now = Instant.now();
        Map<String, Object> prof = request.getProfile() != null ? request.getProfile() : Collections.emptyMap();

        UserRecord record = new UserRecord(
            id, institutionId, request.getProfileType().toLowerCase(), request.getName(), request.getEmail(), "active", prof, now, now
        );

        userStore.put(id, record);
        return mapToResponse(record);
    }

    @Override
    public UserResponse getUserById(String institutionId, String id) {
        UserRecord user = userStore.get(id);
        if (user == null || !user.getInstitutionId().equalsIgnoreCase(institutionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID '" + id + "' not found for institution '" + institutionId + "'.");
        }
        return mapToResponse(user);
    }

    @Override
    public PaginatedUsersResponse listUsers(String institutionId, String profileType, String status, int page, int size) {
        List<UserRecord> filtered = userStore.values().stream()
            .filter(u -> u.getInstitutionId().equalsIgnoreCase(institutionId))
            .filter(u -> profileType == null || u.getProfileType().equalsIgnoreCase(profileType))
            .filter(u -> status == null || u.getStatus().equalsIgnoreCase(status))
            .collect(Collectors.toList());

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<UserResponse> pageContent = filtered.subList(fromIndex, toIndex).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / size);
        return new PaginatedUsersResponse(pageContent, page, size, total, totalPages);
    }

    @Override
    public UserResponse updateUserStatus(String institutionId, String id, String status) {
        UserRecord existing = userStore.get(id);
        if (existing == null || !existing.getInstitutionId().equalsIgnoreCase(institutionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID '" + id + "' not found.");
        }

        String currentStatus = existing.getStatus().toLowerCase();
        String targetStatus = status.toLowerCase();

        // Story 13 Lifecycle State Machine:
        // active <-> suspended
        // active/suspended -> deactivated
        boolean validTransition;
        if ("active".equals(currentStatus)) {
            validTransition = "suspended".equals(targetStatus) || "deactivated".equals(targetStatus);
        } else if ("suspended".equals(currentStatus)) {
            validTransition = "active".equals(targetStatus) || "deactivated".equals(targetStatus);
        } else {
            validTransition = false;
        }

        if (!validTransition) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid user status transition from '" + currentStatus + "' to '" + targetStatus + "'");
        }

        UserRecord updated = new UserRecord(
            existing.getId(), existing.getInstitutionId(), existing.getProfileType(), existing.getName(), existing.getEmail(),
            targetStatus, existing.getProfile(), existing.getCreatedAt(), Instant.now()
        );

        userStore.put(id, updated);
        return mapToResponse(updated);
    }

    @Override
    public UserResponse updateUserProfile(String institutionId, String id, UpdateUserProfileRequest request) {
        UserRecord existing = userStore.get(id);
        if (existing == null || !existing.getInstitutionId().equalsIgnoreCase(institutionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID '" + id + "' not found.");
        }

        Map<String, Object> updatedProfile = new HashMap<>(existing.getProfile());
        if (request.getProfile() != null) {
            updatedProfile.putAll(request.getProfile());
        }

        UserRecord updated = new UserRecord(
            existing.getId(), existing.getInstitutionId(), existing.getProfileType(), existing.getName(), existing.getEmail(),
            existing.getStatus(), updatedProfile, existing.getCreatedAt(), Instant.now()
        );

        userStore.put(id, updated);
        return mapToResponse(updated);
    }

    private UserResponse mapToResponse(UserRecord u) {
        return new UserResponse(u.getId(), u.getInstitutionId(), u.getProfileType(), u.getName(), u.getEmail(), u.getStatus(), u.getProfile(), u.getCreatedAt(), u.getUpdatedAt());
    }

    private static class UserRecord {
        private final String id;
        private final String institutionId;
        private final String profileType;
        private final String name;
        private final String email;
        private final String status;
        private final Map<String, Object> profile;
        private final Instant createdAt;
        private final Instant updatedAt;

        public UserRecord(String id, String institutionId, String profileType, String name, String email, String status, Map<String, Object> profile, Instant createdAt, Instant updatedAt) {
            this.id = id;
            this.institutionId = institutionId;
            this.profileType = profileType;
            this.name = name;
            this.email = email;
            this.status = status;
            this.profile = profile;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getId() { return id; }
        public String getInstitutionId() { return institutionId; }
        public String getProfileType() { return profileType; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public Map<String, Object> getProfile() { return profile; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }
    }
}
