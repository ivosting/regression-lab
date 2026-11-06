package dev.regressionlab.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Emits exactly one structured "request completed" log line per HTTP request,
 * using Datadog standard attribute names (http.*, duration in nanoseconds).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("http.access");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = headerOrRandom(request.getHeader("X-Request-Id"));
        response.setHeader("X-Request-Id", requestId);
        MDC.put("request_id", requestId);
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long nanos = System.nanoTime() - start;
            long millis = Math.round(nanos / 1_000_000.0);
            log.atInfo()
                    .addKeyValue("http.method", request.getMethod())
                    .addKeyValue("http.url_details.path", request.getRequestURI())
                    .addKeyValue("http.status_code", response.getStatus())
                    .addKeyValue("duration", nanos)
                    .addKeyValue("duration_ms", millis)
                    .log("{} {} -> {} in {} ms", request.getMethod(), request.getRequestURI(), response.getStatus(), millis);
            MDC.remove("request_id");
        }
    }

    private static String headerOrRandom(String value) {
        return (value == null || value.isBlank() || value.length() > 64) ? UUID.randomUUID().toString() : value;
    }
}
