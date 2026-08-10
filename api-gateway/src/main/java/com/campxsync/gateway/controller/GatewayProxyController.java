package com.campxsync.gateway.controller;

import logger.constants.AuditConstants;
import logger.logging.AppLogger;
import logger.logging.AuditContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;

@RestController
public class GatewayProxyController {

    private static final AppLogger log = AppLogger.getLogger(GatewayProxyController.class);

    private final RestTemplate restTemplate;
    private final String platformServiceUrl;
    private final String collegeServiceUrl;

    public GatewayProxyController(
            RestTemplate restTemplate,
            @Value("${gateway.services.platform:http://localhost:8088}") String platformServiceUrl,
            @Value("${gateway.services.college:http://localhost:8089}") String collegeServiceUrl) {
        this.restTemplate = restTemplate;
        this.platformServiceUrl = platformServiceUrl;
        this.collegeServiceUrl = collegeServiceUrl;
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxyRequest(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) {

        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = requestUri + (queryString != null ? "?" + queryString : "");

        String targetBaseUrl;
        if (requestUri.startsWith("/v1/institutes") ||
            requestUri.startsWith("/v1/platform-configs") ||
            requestUri.startsWith("/v1/platform-roles") ||
            requestUri.startsWith("/v1/platform-role-assignments") ||
            requestUri.startsWith("/v1/policies") ||
            requestUri.startsWith("/v1/billing-accounts") ||
            requestUri.startsWith("/v1/analytics") ||
            requestUri.startsWith("/v1/compliance-checks") ||
            requestUri.startsWith("/v1/platform-audit-logs") ||
            requestUri.startsWith("/v1/system-health")) {
            targetBaseUrl = platformServiceUrl;
        } else if (requestUri.startsWith("/v1/college-configs") ||
                   requestUri.startsWith("/v1/users") ||
                   requestUri.startsWith("/v1/roles") ||
                   requestUri.startsWith("/v1/role-assignments") ||
                   requestUri.startsWith("/v1/college-analytics") ||
                   requestUri.startsWith("/v1/audit-logs")) {
            targetBaseUrl = collegeServiceUrl;
        } else {
            log.warn("Unrouted API request received for path: {}", requestUri);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"NOT_FOUND\",\"message\":\"No downstream route matching requested endpoint.\"}".getBytes());
        }

        String targetUrl = targetBaseUrl + fullPath;
        log.info("Proxying request [{}] {} -> {}", method, fullPath, targetUrl);

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        // Context Header Injection
        String traceId = AuditContextHolder.getTraceId();
        if (traceId != null && !headers.containsKey(AuditConstants.HEADER_TRACE_ID)) {
            headers.add(AuditConstants.HEADER_TRACE_ID, traceId);
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
            return new ResponseEntity<>(response.getBody(), response.getHeaders(), response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.warn("Downstream service returned HTTP error status {}: {}", e.getStatusCode(), e.getMessage());
            return new ResponseEntity<>(e.getResponseBodyAsByteArray(), e.getResponseHeaders(), e.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to proxy request to downstream service {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"BAD_GATEWAY\",\"message\":\"Downstream service unavailable: " + e.getMessage() + "\"}").getBytes());
        }
    }
}
