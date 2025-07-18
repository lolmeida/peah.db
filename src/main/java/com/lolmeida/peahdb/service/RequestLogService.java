package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@ApplicationScoped
public class RequestLogService {

    // In-memory storage for request logs (in production, use database)
    private final List<RequestInfo> requestLogs = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> endpointCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> browserCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> deviceCounts = new ConcurrentHashMap<>();
    private final Map<String, Integer> osCounts = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> performanceMetrics = new ConcurrentHashMap<>();

    /**
     * Store a request log
     */
    public void storeRequest(RequestInfo requestInfo) {
        try {
            // Store the request
            requestLogs.add(requestInfo);
            
            // Update statistics
            updateStatistics(requestInfo);
            
            // Keep only last 1000 requests in memory
            if (requestLogs.size() > 1000) {
                requestLogs.remove(0);
            }
            
            Log.debugf("📊 Stored request log: %s", requestInfo.getRequestId());
        } catch (Exception e) {
            Log.errorf("Failed to store request log: %s", e.getMessage());
        }
    }

    /**
     * Get recent request logs
     */
    public List<RequestInfo> getRecentRequests(int limit) {
        return requestLogs.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get requests by endpoint
     */
    public List<RequestInfo> getRequestsByEndpoint(String endpoint) {
        return requestLogs.stream()
                .filter(req -> req.getRequestUri().contains(endpoint))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get requests by status code
     */
    public List<RequestInfo> getRequestsByStatus(Integer statusCode) {
        return requestLogs.stream()
                .filter(req -> req.getResponseStatus().equals(statusCode))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get slow requests (above threshold)
     */
    public List<RequestInfo> getSlowRequests(long thresholdMs) {
        return requestLogs.stream()
                .filter(req -> req.getDuration() != null && req.getDuration() > thresholdMs)
                .sorted((a, b) -> b.getDuration().compareTo(a.getDuration()))
                .collect(Collectors.toList());
    }

    /**
     * Get request statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalRequests", requestLogs.size());
        stats.put("endpointCounts", endpointCounts);
        stats.put("browserCounts", browserCounts);
        stats.put("deviceCounts", deviceCounts);
        stats.put("osCounts", osCounts);
        
        // Performance statistics
        Map<String, Object> perfStats = new HashMap<>();
        performanceMetrics.forEach((endpoint, durations) -> {
            if (!durations.isEmpty()) {
                double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
                long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0);
                long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0);
                
                Map<String, Object> endpointStats = new HashMap<>();
                endpointStats.put("avgDuration", Math.round(avgDuration));
                endpointStats.put("maxDuration", maxDuration);
                endpointStats.put("minDuration", minDuration);
                endpointStats.put("requestCount", durations.size());
                
                perfStats.put(endpoint, endpointStats);
            }
        });
        stats.put("performanceStats", perfStats);
        
        // Status code distribution
        Map<Integer, Long> statusCounts = requestLogs.stream()
                .collect(Collectors.groupingBy(RequestInfo::getResponseStatus, Collectors.counting()));
        stats.put("statusCounts", statusCounts);
        
        // Recent activity (last hour)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = requestLogs.stream()
                .filter(req -> req.getTimestamp().isAfter(oneHourAgo))
                .count();
        stats.put("recentRequests", recentRequests);
        
        return stats;
    }

    /**
     * Get performance summary
     */
    public Map<String, Object> getPerformanceSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        if (requestLogs.isEmpty()) {
            summary.put("message", "No requests logged yet");
            return summary;
        }
        
        // Overall performance metrics
        OptionalDouble avgDuration = requestLogs.stream()
                .filter(req -> req.getDuration() != null)
                .mapToLong(RequestInfo::getDuration)
                .average();
        
        OptionalLong maxDuration = requestLogs.stream()
                .filter(req -> req.getDuration() != null)
                .mapToLong(RequestInfo::getDuration)
                .max();
        
        // Performance categorization
        long excellentCount = requestLogs.stream()
                .filter(req -> req.getDuration() != null && req.getDuration() < 50)
                .count();
        
        long goodCount = requestLogs.stream()
                .filter(req -> req.getDuration() != null && req.getDuration() >= 50 && req.getDuration() < 200)
                .count();
        
        long averageCount = requestLogs.stream()
                .filter(req -> req.getDuration() != null && req.getDuration() >= 200 && req.getDuration() < 1000)
                .count();
        
        long slowCount = requestLogs.stream()
                .filter(req -> req.getDuration() != null && req.getDuration() >= 1000)
                .count();
        
        summary.put("averageDuration", avgDuration.isPresent() ? Math.round(avgDuration.getAsDouble()) : 0);
        summary.put("maxDuration", maxDuration.isPresent() ? maxDuration.getAsLong() : 0);
        summary.put("excellentRequests", excellentCount);
        summary.put("goodRequests", goodCount);
        summary.put("averageRequests", averageCount);
        summary.put("slowRequests", slowCount);
        summary.put("totalRequests", requestLogs.size());
        
        return summary;
    }

    /**
     * Get all logs
     */
    public List<RequestInfo> getAllLogs() {
        return new ArrayList<>(requestLogs);
    }

    /**
     * Get logs by browser
     */
    public List<RequestInfo> getLogsByBrowser(String browser) {
        return requestLogs.stream()
                .filter(req -> req.getBrowserName() != null && req.getBrowserName().equalsIgnoreCase(browser))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get logs by OS
     */
    public List<RequestInfo> getLogsByOS(String os) {
        return requestLogs.stream()
                .filter(req -> req.getOperatingSystem() != null && req.getOperatingSystem().equalsIgnoreCase(os))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get logs by device
     */
    public List<RequestInfo> getLogsByDevice(String device) {
        return requestLogs.stream()
                .filter(req -> req.getDeviceType() != null && req.getDeviceType().equalsIgnoreCase(device))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get logs by status (wrapper for existing method)
     */
    public List<RequestInfo> getLogsByStatus(int status) {
        return getRequestsByStatus(status);
    }

    /**
     * Get logs by IP
     */
    public List<RequestInfo> getLogsByIP(String ip) {
        return requestLogs.stream()
                .filter(req -> req.getUserIp() != null && req.getUserIp().equals(ip))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard data
     */
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        Map<String, Object> stats = getStatistics();
        
        dashboard.put("totalRequests", stats.get("totalRequests"));
        dashboard.put("averageResponseTime", getAverageResponseTime());
        dashboard.put("topBrowsers", getTopBrowsers());
        dashboard.put("topOS", getTopOS());
        dashboard.put("recentRequests", getRecentRequests(10));
        
        return dashboard;
    }

    /**
     * Get statistics (wrapper for existing method)
     */
    public Map<String, Object> getStats() {
        return getStatistics();
    }

    /**
     * Search logs
     */
    public List<RequestInfo> searchLogs(String query) {
        return requestLogs.stream()
                .filter(req -> 
                    (req.getRequestUri() != null && req.getRequestUri().contains(query)) ||
                    (req.getHttpMethod() != null && req.getHttpMethod().contains(query)) ||
                    (req.getUserIp() != null && req.getUserIp().contains(query)) ||
                    (req.getBrowserName() != null && req.getBrowserName().contains(query))
                )
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Clear all logs (for testing)
     */
    public void clearLogs() {
        requestLogs.clear();
        endpointCounts.clear();
        browserCounts.clear();
        deviceCounts.clear();
        osCounts.clear();
        performanceMetrics.clear();
        Log.info("🧹 Cleared all request logs");
    }

    /**
     * Get average response time
     */
    private Double getAverageResponseTime() {
        return requestLogs.stream()
                .filter(req -> req.getDuration() != null)
                .mapToLong(RequestInfo::getDuration)
                .average()
                .orElse(0.0);
    }

    /**
     * Get top browsers
     */
    private List<Map<String, Object>> getTopBrowsers() {
        return browserCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> browser = new HashMap<>();
                    browser.put("browser", entry.getKey());
                    browser.put("count", entry.getValue());
                    return browser;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get top operating systems
     */
    private List<Map<String, Object>> getTopOS() {
        return osCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> os = new HashMap<>();
                    os.put("os", entry.getKey());
                    os.put("count", entry.getValue());
                    return os;
                })
                .collect(Collectors.toList());
    }

    /**
     * Update statistics when a new request is stored
     */
    private void updateStatistics(RequestInfo requestInfo) {
        // Update endpoint counts
        endpointCounts.merge(requestInfo.getRequestUri(), 1, Integer::sum);
        
        // Update browser counts
        if (requestInfo.getBrowserName() != null) {
            browserCounts.merge(requestInfo.getBrowserName(), 1, Integer::sum);
        }
        
        // Update device counts
        if (requestInfo.getDeviceType() != null) {
            deviceCounts.merge(requestInfo.getDeviceType(), 1, Integer::sum);
        }
        
        // Update OS counts
        if (requestInfo.getOperatingSystem() != null) {
            osCounts.merge(requestInfo.getOperatingSystem(), 1, Integer::sum);
        }
        
        // Update performance metrics
        if (requestInfo.getDuration() != null) {
            performanceMetrics.computeIfAbsent(requestInfo.getRequestUri(), k -> new CopyOnWriteArrayList<>())
                    .add(requestInfo.getDuration());
        }
    }
} 