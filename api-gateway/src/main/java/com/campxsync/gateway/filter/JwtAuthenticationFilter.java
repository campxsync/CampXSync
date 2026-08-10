package com.campxsync.gateway.filter;

import logger.constants.AuditConstants;
import logger.dto.UserPrincipal;
import logger.jwt.JwtProvider;
import logger.logging.AppLogger;
import logger.logging.AuditContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
@Order(1)
public class JwtAuthenticationFilter implements Filter {

    private static final AppLogger log = AppLogger.getLogger(JwtAuthenticationFilter.class);

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(
            @Value("${gateway.jwt.secret:super_secret_signing_key_for_campxsync_platform_2026}") String secret,
            @Value("${gateway.jwt.issuer:campxsync}") String issuer) {
        this.jwtProvider = new JwtProvider(secret, issuer);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Bypass security filters for health checks and OpenAPI docs
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            chain.doFilter(request, response);
            return;
        }

        // Trace ID Resolution
        String traceId = httpRequest.getHeader(AuditConstants.HEADER_TRACE_ID);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        String authHeader = httpRequest.getHeader("Authorization");
        UserPrincipal principal = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                principal = jwtProvider.validateAndDecode(token);
                log.debug("Validated JWT token successfully for userId: {}", principal.getUserId());
            } catch (Exception e) {
                log.warn("Invalid JWT bearer token provided for request to {}: {}", path, e.getMessage());
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"UNAUTHORIZED\", \"message\": \"Invalid or expired Bearer JWT token.\"}");
                return;
            }
        }

        // Fallback for Mock Headers if Bearer JWT is omitted (Development / Testing Mode)
        if (principal == null) {
            String userId = httpRequest.getHeader(AuditConstants.HEADER_USER_ID);
            String institutionId = httpRequest.getHeader("X-Institution-Id");
            if (userId != null) {
                principal = new UserPrincipal(userId, "System User", "user@campxsync.com", Arrays.asList("MEMBER"), institutionId);
            }
        }

        String clientIp = httpRequest.getHeader(AuditConstants.HEADER_CLIENT_IP);
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = httpRequest.getRemoteAddr();
        }

        // Initialize MDC & Audit Context Holder
        AuditContextHolder.initContext(principal, clientIp, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            AuditContextHolder.clear();
        }
    }
}
