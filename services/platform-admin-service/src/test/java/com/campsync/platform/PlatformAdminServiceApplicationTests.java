package com.campsync.platform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class PlatformAdminServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    // --- 1. INSTITUTE MANAGEMENT SERVICE ---

    @Test
    @DisplayName("API 01: Provision Institute (POST /v1/institutes)")
    public void test01_ProvisionInstitute() throws Exception {
        String body = "{\"name\":\"Stanford University\",\"subdomain\":\"stanford\",\"planId\":\"plan-enterprise-plus\",\"tenancyTier\":\"DEDICATED_TENANT\"}";
        MvcResult res = mockMvc.perform(post("/v1/institutes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Stanford University"))
                .andReturn();
        printTestOutput("API 01: POST /v1/institutes", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 02: View Institute Details (GET /v1/institutes/{id})")
    public void test02_GetInstituteById() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/institutes/inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("inst-101"))
                .andReturn();
        printTestOutput("API 02: GET /v1/institutes/inst-101", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 03: List & Filter Institutes (GET /v1/institutes)")
    public void test03_ListInstitutes() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/institutes").param("status", "active").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        printTestOutput("API 03: GET /v1/institutes?status=active", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 04: Transition Institute Status (PATCH /v1/institutes/{id}/status)")
    public void test04_UpdateInstituteStatus() throws Exception {
        String body = "{\"status\":\"suspended\"}";
        MvcResult res = mockMvc.perform(patch("/v1/institutes/inst-101/status").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("suspended"))
                .andReturn();
        printTestOutput("API 04: PATCH /v1/institutes/inst-101/status", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 05: Update Institute Profile (PATCH /v1/institutes/{id})")
    public void test05_UpdateInstituteProfile() throws Exception {
        String body = "{\"name\":\"Oxford International Academy\",\"tenancyTier\":\"HIGH_AVAILABILITY\"}";
        MvcResult res = mockMvc.perform(patch("/v1/institutes/inst-101").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oxford International Academy"))
                .andReturn();
        printTestOutput("API 05: PATCH /v1/institutes/inst-101", body, "200 OK", res.getResponse().getContentAsString());
    }

    // --- 2. PLATFORM CONFIGURATION SERVICE ---

    @Test
    @DisplayName("API 06: View Current Platform Configs (GET /v1/platform-configs)")
    public void test06_GetAllConfigs() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/platform-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 06: GET /v1/platform-configs", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 07: Update Global Config Setting (PATCH /v1/platform-configs/{key})")
    public void test07_UpdateConfig() throws Exception {
        String body = "{\"value\": false}";
        MvcResult res = mockMvc.perform(patch("/v1/platform-configs/mfa_required").header("X-Actor-Id", "admin-john").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(false))
                .andReturn();
        printTestOutput("API 07: PATCH /v1/platform-configs/mfa_required", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 08: View Config Change History (GET /v1/platform-configs/history)")
    public void test08_GetConfigHistory() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/platform-configs/history").param("key", "mfa_required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 08: GET /v1/platform-configs/history?key=mfa_required", "None", "200 OK", res.getResponse().getContentAsString());
    }

    // --- 3. PLATFORM RBAC SERVICE ---

    @Test
    @DisplayName("API 09: Define Platform Role (POST /v1/platform-roles)")
    public void test09_CreateRole() throws Exception {
        String body = "{\"name\":\"Support Ops Manager\",\"description\":\"Handles tenant support escalation\",\"permissions\":[\"platform:institutes:read\"]}";
        MvcResult res = mockMvc.perform(post("/v1/platform-roles").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Support Ops Manager"))
                .andReturn();
        printTestOutput("API 09: POST /v1/platform-roles", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 10: List Platform Roles (GET /v1/platform-roles)")
    public void test10_ListRoles() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/platform-roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 10: GET /v1/platform-roles", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 11: Grant Role to Staff (POST /v1/platform-role-assignments)")
    public void test11_GrantRole() throws Exception {
        String body = "{\"staffId\":\"staff-99\",\"roleId\":\"role-super-admin\"}";
        MvcResult res = mockMvc.perform(post("/v1/platform-role-assignments").header("X-Actor-Id", "admin-super-1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.staffId").value("staff-99"))
                .andReturn();
        printTestOutput("API 11: POST /v1/platform-role-assignments", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 12: Resolve Effective Permissions (GET /v1/platform-role-assignments/{staff_id}/effective)")
    public void test12_GetEffectivePermissions() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/platform-role-assignments/staff-99/effective"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectivePermissions").isArray())
                .andReturn();
        printTestOutput("API 12: GET /v1/platform-role-assignments/staff-99/effective", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 13: Revoke Role Assignment (DELETE /v1/platform-role-assignments/{id})")
    public void test13_RevokeRole() throws Exception {
        // First grant a role to get a valid assignment ID
        String grantBody = "{\"staffId\":\"staff-revoke-test\",\"roleId\":\"role-super-admin\"}";
        MvcResult grantRes = mockMvc.perform(post("/v1/platform-role-assignments").contentType(MediaType.APPLICATION_JSON).content(grantBody)).andReturn();
        String assignId = grantRes.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        MvcResult res = mockMvc.perform(delete("/v1/platform-role-assignments/" + assignId))
                .andExpect(status().isNoContent())
                .andReturn();
        printTestOutput("API 13: DELETE /v1/platform-role-assignments/" + assignId, "None", "204 No Content", "No Content (HTTP 204)");
    }

    // --- 4. DATA GOVERNANCE SERVICE ---

    @Test
    @DisplayName("API 14: Create Governance Policy (POST /v1/policies)")
    public void test14_CreatePolicy() throws Exception {
        String body = "{\"name\":\"Data Residency EU-Central\",\"type\":\"residency\",\"appliesTo\":\"all\",\"rule\":{\"primaryRegion\":\"eu-central-1\"}}";
        MvcResult res = mockMvc.perform(post("/v1/policies").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Data Residency EU-Central"))
                .andReturn();
        printTestOutput("API 14: POST /v1/policies", body, "201 Created", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 15: List Governance Policies (GET /v1/policies)")
    public void test15_ListPolicies() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/policies").param("applies_to", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 15: GET /v1/policies?applies_to=all", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 16: Update Policy (PATCH /v1/policies/{id})")
    public void test16_UpdatePolicy() throws Exception {
        String body = "{\"name\":\"Updated Retention Policy\"}";
        MvcResult res = mockMvc.perform(patch("/v1/policies/pol-101").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Retention Policy"))
                .andReturn();
        printTestOutput("API 16: PATCH /v1/policies/pol-101", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 17: Retire Policy (DELETE /v1/policies/{id})")
    public void test17_RetirePolicy() throws Exception {
        MvcResult res = mockMvc.perform(delete("/v1/policies/pol-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRED"))
                .andReturn();
        printTestOutput("API 17: DELETE /v1/policies/pol-101", "None", "200 OK", res.getResponse().getContentAsString());
    }

    // --- 5. BILLING & SUBSCRIPTION SERVICE ---

    @Test
    @DisplayName("API 18: View Billing Account (GET /v1/billing-accounts/{institution_id})")
    public void test18_GetBillingAccount() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/billing-accounts/inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionId").value("inst-101"))
                .andReturn();
        printTestOutput("API 18: GET /v1/billing-accounts/inst-101", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 19: Change Subscription Plan (POST /v1/billing-accounts/{institution_id}/change-plan)")
    public void test19_ChangePlan() throws Exception {
        String body = "{\"newPlanId\":\"plan-enterprise-v2\"}";
        MvcResult res = mockMvc.perform(post("/v1/billing-accounts/inst-101/change-plan").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId").value("plan-enterprise-v2"))
                .andReturn();
        printTestOutput("API 19: POST /v1/billing-accounts/inst-101/change-plan", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 20: Trigger Manual Settlement (POST /v1/billing-accounts/{institution_id}/charge)")
    public void test20_TriggerCharge() throws Exception {
        String body = "{\"amount\":1250.00,\"description\":\"Ad-hoc storage overage charge\"}";
        MvcResult res = mockMvc.perform(post("/v1/billing-accounts/inst-101/charge").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andReturn();
        printTestOutput("API 20: POST /v1/billing-accounts/inst-101/charge", body, "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Story 32: Settlement Failure during Plan Change (HTTP 402 Payment Required)")
    public void test32_FailedPlanChangeSettlement() throws Exception {
        String body = "{\"newPlanId\":\"plan-invalid\"}";
        MvcResult res = mockMvc.perform(post("/v1/billing-accounts/inst-101/change-plan").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isPaymentRequired())
                .andReturn();
        printTestOutput("Story 32: Failed Plan Settlement Check", body, "402 Payment Required", res.getResponse().getErrorMessage() != null ? res.getResponse().getErrorMessage() : "402 Payment Required");
    }

    // --- 6. PLATFORM ANALYTICS SERVICE ---

    @Test
    @DisplayName("API 21: View Analytics Snapshots (GET /v1/analytics/snapshots)")
    public void test21_GetAnalyticsSnapshot() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/analytics/snapshots").param("metric", "active_institutions_count").param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotId").exists())
                .andReturn();
        printTestOutput("API 21: GET /v1/analytics/snapshots?metric=active_institutions_count&period=monthly", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 22: Trigger Manual Recompute (POST /v1/analytics/snapshots/recompute)")
    public void test22_TriggerRecompute() throws Exception {
        String body = "{\"metric\":\"active_institutions_count\",\"period\":\"monthly\"}";
        MvcResult res = mockMvc.perform(post("/v1/analytics/snapshots/recompute").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andReturn();
        printTestOutput("API 22: POST /v1/analytics/snapshots/recompute", body, "202 Accepted", res.getResponse().getContentAsString());
    }

    // --- 7. SECURITY & COMPLIANCE SERVICE ---

    @Test
    @DisplayName("API 23: Trigger Compliance Check (POST /v1/compliance-checks/run)")
    public void test23_RunComplianceCheck() throws Exception {
        String body = "{\"institutionId\":\"inst-101\",\"policyIds\":[\"pol-101\"]}";
        MvcResult res = mockMvc.perform(post("/v1/compliance-checks/run").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andReturn();
        printTestOutput("API 23: POST /v1/compliance-checks/run", body, "202 Accepted", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 24: View Institute Compliance Results (GET /v1/compliance-checks/{institution_id})")
    public void test24_GetComplianceResults() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/compliance-checks/inst-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionId").value("inst-101"))
                .andReturn();
        printTestOutput("API 24: GET /v1/compliance-checks/inst-101", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 25: List Non-Compliant Institutes (GET /v1/compliance-checks)")
    public void test25_ListNonCompliant() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/compliance-checks").param("flagged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        printTestOutput("API 25: GET /v1/compliance-checks?flagged=true", "None", "200 OK", res.getResponse().getContentAsString());
    }

    // --- 8. AUDIT & SYSTEM HEALTH SERVICE ---

    @Test
    @DisplayName("API 26: Query Platform Audit Trail (GET /v1/platform-audit-logs)")
    public void test26_QueryAuditLogs() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/platform-audit-logs").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        printTestOutput("API 26: GET /v1/platform-audit-logs?page=0&size=5", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("API 27: View System Health (GET /v1/system-health)")
    public void test27_GetSystemHealth() throws Exception {
        MvcResult res = mockMvc.perform(get("/v1/system-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andReturn();
        printTestOutput("API 27: GET /v1/system-health", "None", "200 OK", res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Story 53: Boundary Isolation Check")
    public void testBoundaryIsolation() throws Exception {
        mockMvc.perform(get("/v1/platform-audit-logs").param("event_type", "TenantInternalCourseEnrollment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
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
