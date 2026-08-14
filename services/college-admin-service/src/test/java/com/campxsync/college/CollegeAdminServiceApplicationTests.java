package com.campxsync.college;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
public class CollegeAdminServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 1. COLLEGE CONFIGURATION SERVICE ---

    @Test
    @DisplayName("API 01: View College Configs (GET /v1/college-configs)")
    public void test01_GetInstitutionConfigs() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/college-configs").header("X-Institution-Id", "inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 01: GET /v1/college-configs", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 02: Update College Config Setting (PATCH /v1/college-configs/{key})")
    public void test02_UpdateConfig() throws Exception {
        String body = "{\"value\": \"#0055A5\"}";
        MvcResult res = mockMvc.perform(patch("/v1/college-configs/theme_color")
                        .header("X-Institution-Id", "inst-101")
                        .header("X-Actor-Id", "college-admin-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("#0055A5"))
                .andReturn();
        printTestOutput("API 02: PATCH /v1/college-configs/theme_color", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 03: View Config History (GET /v1/college-configs/history)")
    public void test03_GetConfigHistory() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/college-configs/history")
                        .header("X-Institution-Id", "inst-101")
                        .param("key", "theme_color"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 03: GET /v1/college-configs/history", "None", "200 OK", res.getResponse().getContentAsString());
    }

    // --- 2. COLLEGE IDENTITY & PROFILE SERVICE ---

    @Test
    @DisplayName("API 04: Create User (POST /v1/users)")
    public void test04_CreateUser() throws Exception {
        String body = "{\n" +
                "  \"profileType\": \"student\",\n" +
                "  \"name\": \"Jane Doe\",\n" +
                "  \"email\": \"jane.doe@oxford.edu\",\n" +
                "  \"profile\": {\"department\": \"Computer Science\", \"rollNo\": \"CS-2026-01\"}\n" +
                "}";

        MvcResult res = mockMvc.perform(post("/v1/users")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("active"))
                .andReturn();
        printTestOutput("API 04: POST /v1/users", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 05: View User Details (GET /v1/users/{id})")
    public void test05_GetUserById() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/users/usr-101").header("X-Institution-Id", "inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-101"))
                .andReturn();
        printTestOutput("API 05: GET /v1/users/usr-101", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 06: List & Filter Users (GET /v1/users)")
    public void test06_ListUsers() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/users")
                        .header("X-Institution-Id", "inst-101")
                        .param("profile_type", "faculty")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        printTestOutput("API 06: GET /v1/users?profile_type=faculty", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 07: Transition User Status (PATCH /v1/users/{id}/status)")
    public void test07_UpdateUserStatus() throws Exception {
        String body = "{\"status\": \"suspended\"}";

        MvcResult res = mockMvc.perform(patch("/v1/users/usr-101/status")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("suspended"))
                .andReturn();
        printTestOutput("API 07: PATCH /v1/users/usr-101/status", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 08: Update User Profile (PATCH /v1/users/{id}/profile)")
    public void test08_UpdateUserProfile() throws Exception {
        String body = "{\"profile\": {\"designation\": \"Senior Professor\"}}";

        MvcResult res = mockMvc.perform(patch("/v1/users/usr-101/profile")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-101"))
                .andReturn();
        printTestOutput("API 08: PATCH /v1/users/usr-101/profile", body, "200 OK", res.getResponse().getContentAsString());
    }

    // --- 3. COLLEGE RBAC SERVICE ---

    @Test
    @DisplayName("API 09: Define Custom Role (POST /v1/roles)")
    public void test09_CreateCustomRole() throws Exception {
        String body = "{\n" +
                "  \"name\": \"Department Head\",\n" +
                "  \"description\": \"Head of Academic Department\",\n" +
                "  \"permissions\": [\"college:users:read\", \"college:users:write\"]\n" +
                "}";

        MvcResult res = mockMvc.perform(post("/v1/roles")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Department Head"))
                .andReturn();
        printTestOutput("API 09: POST /v1/roles", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 10: List Custom Roles (GET /v1/roles)")
    public void test10_ListRoles() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/roles").header("X-Institution-Id", "inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 10: GET /v1/roles", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 11: Grant Role to User (POST /v1/role-assignments)")
    public void test11_GrantRole() throws Exception {
        String body = "{\"userId\":\"usr-101\",\"roleId\":\"role-college-admin\",\"scope\":\"DEPARTMENT_CS\"}";

        MvcResult res = mockMvc.perform(post("/v1/role-assignments")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("usr-101"))
                .andReturn();
        printTestOutput("API 11: POST /v1/role-assignments", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 12: Resolve Effective Permissions (GET /v1/role-assignments/{user_id}/effective)")
    public void test12_GetEffectivePermissions() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/role-assignments/usr-101/effective").header("X-Institution-Id", "inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectivePermissions").isArray())
                .andReturn();
        printTestOutput("API 12: GET /v1/role-assignments/usr-101/effective", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 13: Revoke Role Assignment (DELETE /v1/role-assignments/{id})")
    public void test13_RevokeRole() throws Exception {
        String grantBody = "{\"userId\":\"usr-revoke-test\",\"roleId\":\"role-college-admin\"}";
        MvcResult grantRes = mockMvc.perform(post("/v1/role-assignments").header("X-Institution-Id", "inst-101").contentType(MediaType.APPLICATION_JSON).content(grantBody)).andReturn();
        String assignId = grantRes.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        MvcResult res = mockMvc.perform(delete("/v1/role-assignments/" + assignId).header("X-Institution-Id", "inst-101"))
                .andExpect(status().isNoContent())
                .andReturn();
        printTestOutput("API 13: DELETE /v1/role-assignments/" + assignId, "None", "204 No Content", "No Content (HTTP 204)");
    }

    // --- 4. COLLEGE REPORTS & ANALYTICS SERVICE ---

    @Test
    @DisplayName("API 14: View Analytics Dashboard (GET /v1/college-analytics/dashboard)")
    public void test14_GetDashboard() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/college-analytics/dashboard").header("X-Institution-Id", "inst-101").param("metric", "enrollment_summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotId").exists())
                .andReturn();
        printTestOutput("API 14: GET /v1/college-analytics/dashboard?metric=enrollment_summary", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 15: Trigger Analytics Recompute (POST /v1/college-analytics/recompute)")
    public void test15_TriggerRecompute() throws Exception {
        String body = "{\"metric\":\"enrollment_summary\",\"period\":\"semester\"}";
        MvcResult res = mockMvc.perform(post("/v1/college-analytics/recompute").header("X-Institution-Id", "inst-101").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andReturn();
        printTestOutput("API 15: POST /v1/college-analytics/recompute", body, "202 Accepted", res.getResponse().getContentAsString());
    }

    // --- 5. COLLEGE AUDIT & COMPLIANCE SERVICE ---

    @Test
    @DisplayName("API 16: Query Institution Audit Trail (GET /v1/audit-logs)")
    public void test16_QueryAuditLogs() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/audit-logs").header("X-Institution-Id", "inst-101").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        printTestOutput("API 16: GET /v1/audit-logs?page=0&size=5", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Story 36: Tenant vs Platform RBAC Isolation Check")
    public void testTenantRbacIsolation() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/role-assignments/usr-101/effective").header("X-Institution-Id", "inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectivePermissions").isArray())
                .andReturn();

        String responseStr = res.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertFalse(responseStr.contains("platform:"), "Tenant effective permissions must never contain platform-tier permissions!");
    }

    // --- NEGATIVE TEST CASES ---

    @Test
    @DisplayName("NEG 01: Missing X-Institution-Id Header (HTTP 400 Bad Request)")
    public void testNeg01_MissingInstitutionHeader() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/users"))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 01: GET /v1/users (No X-Institution-Id)", "None", "400 Bad Request", "400 Bad Request (Header Missing)");
    }

    @Test
    @DisplayName("NEG 02: Create User with Invalid Email Format (HTTP 400 Bad Request)")
    public void testNeg02_CreateUserInvalidEmail() throws Exception {
        String body = "{\"profileType\":\"student\",\"name\":\"Bad Email User\",\"email\":\"not-an-email-address\"}";
        MvcResult res = mockMvc.perform(post("/v1/users")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 02: POST /v1/users (Invalid Email)", body, "400 Bad Request", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("NEG 03: Create User with Blank Name (HTTP 400 Bad Request)")
    public void testNeg03_CreateUserBlankName() throws Exception {
        String body = "{\"profileType\":\"student\",\"name\":\"\",\"email\":\"valid@oxford.edu\"}";
        MvcResult res = mockMvc.perform(post("/v1/users")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 03: POST /v1/users (Blank Name)", body, "400 Bad Request", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("NEG 04: Get Non-Existent User (HTTP 404 Not Found)")
    public void testNeg04_GetUserNotFound() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/users/usr-nonexistent-999")
                        .header("X-Institution-Id", "inst-101"))
                .andExpect(status().isNotFound())
                .andReturn();
        printTestOutput("NEG 04: GET /v1/users/usr-nonexistent-999", "None", "404 Not Found", res.getResponse().getErrorMessage() != null ? res.getResponse().getErrorMessage() : "404 Not Found");
    }

    @Test
    @DisplayName("NEG 05: Cross-Tenant User Access Rejection (HTTP 404 Not Found)")
    public void testNeg05_CrossTenantAccess() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/users/usr-101")
                        .header("X-Institution-Id", "inst-wrong-tenant-999"))
                .andExpect(status().isNotFound())
                .andReturn();
        printTestOutput("NEG 05: GET /v1/users/usr-101 (Wrong Tenant)", "None", "404 Not Found", "404 User not found for institution inst-wrong-tenant-999");
    }

    @Test
    @DisplayName("NEG 06: Invalid User Status Transition (HTTP 400 Bad Request)")
    public void testNeg06_InvalidUserStatusTransition() throws Exception {
        String body = "{\"status\": \"invalid_status_value\"}";
        MvcResult res = mockMvc.perform(patch("/v1/users/usr-101/status")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 06: PATCH /v1/users/usr-101/status (Invalid Status)", body, "400 Bad Request", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("NEG 07: Define Custom Role with Blank Name (HTTP 400 Bad Request)")
    public void testNeg07_CreateRoleBlankName() throws Exception {
        String body = "{\"name\":\"\",\"permissions\":[\"college:users:read\"]}";
        MvcResult res = mockMvc.perform(post("/v1/roles")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 07: POST /v1/roles (Blank Name)", body, "400 Bad Request", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("NEG 08: Grant Role with Blank User ID (HTTP 400 Bad Request)")
    public void testNeg08_GrantRoleBlankUserId() throws Exception {
        String body = "{\"userId\":\"\",\"roleId\":\"role-college-admin\"}";
        MvcResult res = mockMvc.perform(post("/v1/role-assignments")
                        .header("X-Institution-Id", "inst-101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();
        printTestOutput("NEG 08: POST /v1/role-assignments (Blank UserId)", body, "400 Bad Request", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("NEG 09: Revoke Non-Existent Role Assignment (HTTP 404 Not Found)")
    public void testNeg09_RevokeNonExistentRole() throws Exception {
        MvcResult res = mockMvc.perform(delete("/v1/role-assignments/assign-nonexistent-999")
                        .header("X-Institution-Id", "inst-101"))
                .andExpect(status().isNotFound())
                .andReturn();
        printTestOutput("NEG 09: DELETE /v1/role-assignments/assign-nonexistent-999", "None", "404 Not Found", res.getResponse().getErrorMessage() != null ? res.getResponse().getErrorMessage() : "404 Not Found");
    }

    private void printTestOutput(String api, String input, String status, String output) {
        System.out.println("--------------------------------------------------");
        System.out.println("TESTED API: " + api);
        System.out.println("INPUT: " + input);
        System.out.println("STATUS: " + status);
        System.out.println("OUTPUT: " + output);
        System.out.println("--------------------------------------------------");
    }
}
