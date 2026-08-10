package com.campxsync.gateway;

import logger.jwt.JwtProvider;
import logger.dto.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiGatewayApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private final JwtProvider jwtProvider = new JwtProvider("super_secret_signing_key_for_campxsync_platform_2026", "campxsync");

    @Test
    @DisplayName("Test 01: Gateway Health Actuator Endpoint Check")
    public void test01_HealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Test 02: CORS Preflight Request Check")
    public void test02_CorsPreflight() throws Exception {
        mockMvc.perform(options("/v1/institutes")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("Test 03: Invalid Bearer Token Rejection")
    public void test03_InvalidJwtToken() throws Exception {
        mockMvc.perform(get("/v1/institutes")
                        .header("Authorization", "Bearer invalid_malformed_token_123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Test 04: Valid JWT Token Ingress Context Injection")
    public void test04_ValidJwtTokenIngress() throws Exception {
        UserPrincipal principal = new UserPrincipal("usr-admin-1", "Super Admin", "admin@campx.com", Arrays.asList("SUPER_ADMIN"), "inst-101");
        String token = jwtProvider.createToken(principal, 3600000);

        mockMvc.perform(get("/v1/unrouted-test-endpoint")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(header().exists("X-Response-Time"));
    }

    @Test
    @DisplayName("Test 05: Unrouted Path Handling (HTTP 404)")
    public void test05_UnroutedEndpoint() throws Exception {
        mockMvc.perform(get("/v1/nonexistent-service/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No downstream route matching requested endpoint."));
    }
}
