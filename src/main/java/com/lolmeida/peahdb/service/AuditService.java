package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class AuditService {

    /**
     * Log request information for audit purposes
     */
    public void logRequest(RequestInfo requestInfo) {
        try {
            // Log comprehensive request information
            Log.infof("🔍 AUDIT - Request Details: " +
                    "ID=%s, Method=%s, URI=%s, IP=%s, UserAgent=%s, " +
                    "Browser=%s %s, OS=%s, Device=%s, Status=%d, Duration=%dms",
                    requestInfo.getRequestId(),
                    requestInfo.getHttpMethod(),
                    requestInfo.getRequestUri(),
                    requestInfo.getUserIp(),
                    requestInfo.getUserAgent(),
                    requestInfo.getBrowserName(),
                    requestInfo.getBrowserVersion(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getDeviceType(),
                    requestInfo.getResponseStatus(),
                    requestInfo.getDuration()
            );
        } catch (Exception e) {
            Log.warnf("Failed to log request audit: %s", e.getMessage());
        }
    }

    /**
     * Log security-related events
     */
    public void logSecurityEvent(RequestInfo requestInfo, String eventType, String description) {
        try {
            Log.warnf("🔐 SECURITY - %s: %s | IP=%s, UserAgent=%s, URI=%s, RequestID=%s",
                    eventType,
                    description,
                    requestInfo.getUserIp(),
                    requestInfo.getUserAgent(),
                    requestInfo.getRequestUri(),
                    requestInfo.getRequestId()
            );
        } catch (Exception e) {
            Log.errorf("Failed to log security event: %s", e.getMessage());
        }
    }

    /**
     * Log performance metrics
     */
    public void logPerformanceMetrics(RequestInfo requestInfo) {
        try {
            if (requestInfo.getDuration() != null) {
                String performanceLevel = getPerformanceLevel(requestInfo.getDuration());
                
                Log.infof("⚡ PERFORMANCE - %s: %s %s took %dms | IP=%s, Device=%s",
                        performanceLevel,
                        requestInfo.getHttpMethod(),
                        requestInfo.getRequestUri(),
                        requestInfo.getDuration(),
                        requestInfo.getUserIp(),
                        requestInfo.getDeviceType()
                );
            }
        } catch (Exception e) {
            Log.warnf("Failed to log performance metrics: %s", e.getMessage());
        }
    }

    /**
     * Store request information asynchronously (example for database storage)
     */
    public CompletableFuture<Void> storeRequestInfoAsync(RequestInfo requestInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Here you would store in database
                // Example: requestInfoRepository.save(requestInfo);
                
                Log.debugf("📊 Stored request info: %s", requestInfo.getRequestId());
            } catch (Exception e) {
                Log.errorf("Failed to store request info asynchronously: %s", e.getMessage());
            }
        });
    }

    /**
     * Detect and log suspicious activities
     */
    public void detectSuspiciousActivity(RequestInfo requestInfo) {
        try {
            // Check for potential bot activity
            if ("Bot".equals(requestInfo.getDeviceType())) {
                logSecurityEvent(requestInfo, "BOT_DETECTED", "Bot user agent detected");
            }
            
            // Check for unusual response times
            if (requestInfo.getDuration() != null && requestInfo.getDuration() > 10000) {
                logSecurityEvent(requestInfo, "SLOW_REQUEST", "Request took more than 10 seconds");
            }
            
            // Check for error responses
            if (requestInfo.getResponseStatus() != null && requestInfo.getResponseStatus() >= 400) {
                logSecurityEvent(requestInfo, "ERROR_RESPONSE", 
                        "Error response: " + requestInfo.getResponseStatus());
            }
            
            // Check for missing User-Agent (potential programmatic access)
            if (requestInfo.getUserAgent() == null || requestInfo.getUserAgent().isEmpty()) {
                logSecurityEvent(requestInfo, "MISSING_USER_AGENT", "Request without User-Agent header");
            }
            
        } catch (Exception e) {
            Log.warnf("Failed to detect suspicious activity: %s", e.getMessage());
        }
    }

    /**
     * Generate usage statistics
     */
    public void generateUsageStats(RequestInfo requestInfo) {
        try {
            // Log usage statistics for analytics
            Log.infof("📈 USAGE - Browser: %s, OS: %s, Device: %s, Country: %s, Language: %s",
                    requestInfo.getBrowserName(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getDeviceType(),
                    requestInfo.getCountry(),
                    requestInfo.getAcceptLanguage()
            );
        } catch (Exception e) {
            Log.warnf("Failed to generate usage stats: %s", e.getMessage());
        }
    }

    /**
     * Log API usage patterns
     */
    public void logApiUsage(RequestInfo requestInfo) {
        try {
            if (requestInfo.getRequestUri() != null && requestInfo.getRequestUri().startsWith("/api/")) {
                Log.infof("🔗 API - %s %s | Status: %d | Duration: %dms | IP: %s",
                        requestInfo.getHttpMethod(),
                        requestInfo.getRequestUri(),
                        requestInfo.getResponseStatus(),
                        requestInfo.getDuration(),
                        requestInfo.getUserIp()
                );
            }
        } catch (Exception e) {
            Log.warnf("Failed to log API usage: %s", e.getMessage());
        }
    }

    /**
     * Process complete request audit
     */
    public void processCompleteAudit(RequestInfo requestInfo) {
        try {
            // Log basic request information
            logRequest(requestInfo);
            
            // Log performance metrics
            logPerformanceMetrics(requestInfo);
            
            // Detect suspicious activities
            detectSuspiciousActivity(requestInfo);
            
            // Generate usage statistics
            generateUsageStats(requestInfo);
            
            // Log API usage if it's an API call
            logApiUsage(requestInfo);
            
            // Store information asynchronously
            storeRequestInfoAsync(requestInfo);
            
        } catch (Exception e) {
            Log.errorf("Failed to process complete audit: %s", e.getMessage());
        }
    }

    /**
     * Helper method to determine performance level
     */
    private String getPerformanceLevel(long duration) {
        if (duration < 100) {
            return "EXCELLENT";
        } else if (duration < 500) {
            return "GOOD";
        } else if (duration < 1000) {
            return "ACCEPTABLE";
        } else if (duration < 5000) {
            return "SLOW";
        } else {
            return "VERY_SLOW";
        }
    }
} 