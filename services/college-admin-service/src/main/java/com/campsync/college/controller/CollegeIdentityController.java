package com.campsync.college.controller;

import com.campsync.college.dto.CollegeIdentityDtos.*;
import com.campsync.college.service.CollegeIdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "College Identity & Profile", description = "User account creation, profile management, and account lifecycle transitions")
public class CollegeIdentityController {

    private final CollegeIdentityService identityService;

    public CollegeIdentityController(CollegeIdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping
    @Operation(summary = "Create a user", description = "Creates a user record of any profile_type (student, faculty, staff, parent, alumni, admin) with status=active")
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = identityService.createUser(institutionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "View a user's identity and profile record", description = "Fetches a single user's identity and profile sub-document")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @PathVariable String id) {
        UserResponse response = identityService.getUserById(institutionId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List and filter users", description = "Returns a paginated list of users filtered by profile_type and status for the caller's institution")
    public ResponseEntity<PaginatedUsersResponse> listUsers(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @RequestParam(name = "profile_type", required = false) String profileType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedUsersResponse response = identityService.listUsers(institutionId, profileType, status, page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition a user's account status", description = "Enforces state machine status changes (active<->suspended, active/suspended->deactivated)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponse response = identityService.updateUserStatus(institutionId, id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/profile")
    @Operation(summary = "Update a user's type-specific profile", description = "Updates profile sub-document fields matching the user's profile type")
    public ResponseEntity<UserResponse> updateUserProfile(
            @RequestHeader(name = "X-Institution-Id", defaultValue = "inst-101") String institutionId,
            @PathVariable String id,
            @RequestBody UpdateUserProfileRequest request) {
        UserResponse response = identityService.updateUserProfile(institutionId, id, request);
        return ResponseEntity.ok(response);
    }
}
