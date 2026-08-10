package com.campxsync.gateway.filter;

import logger.logging.AppLogger;
import logger.logging.AuditContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2)
public class RequestLoggingFilter implements Filter {

    private static final AppLogger log = AppLogger.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String traceId = AuditContextHolder.getTraceId();

        log.info("API Ingress [{}] Method: {} URI: {} TraceId: {}", httpRequest.getRemoteAddr(), method, uri, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();
            httpResponse.setHeader("X-Trace-Id", traceId != null ? traceId : "");
            httpResponse.setHeader("X-Response-Time", duration + "ms");

            log.info("API Egress [{}] Method: {} URI: {} Status: {} Latency: {}ms TraceId: {}",
                    httpRequest.getRemoteAddr(), method, uri, status, duration, traceId);
        }
    }
}
