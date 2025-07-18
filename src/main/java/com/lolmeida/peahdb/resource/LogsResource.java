package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import com.lolmeida.peahdb.service.RequestLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LogsResource {

    @Inject
    RequestLogService requestLogService;

    /**
     * Get recent request logs
     */
    @GET
    @Path("/recent")
    public Response getRecentLogs(@QueryParam("limit") @DefaultValue("50") int limit) {
        try {
            List<RequestInfo> logs = requestLogService.getRecentRequests(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("limit", limit);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch logs: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get logs by endpoint
     */
    @GET
    @Path("/endpoint/{endpoint}")
    public Response getLogsByEndpoint(@PathParam("endpoint") String endpoint) {
        try {
            List<RequestInfo> logs = requestLogService.getRequestsByEndpoint(endpoint);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("endpoint", endpoint);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch logs by endpoint: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get logs by status code
     */
    @GET
    @Path("/status/{status}")
    public Response getLogsByStatus(@PathParam("status") int status) {
        try {
            List<RequestInfo> logs = requestLogService.getRequestsByStatus(status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("status", status);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch logs by status: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get slow requests
     */
    @GET
    @Path("/slow")
    public Response getSlowRequests(@QueryParam("threshold") @DefaultValue("1000") long threshold) {
        try {
            List<RequestInfo> logs = requestLogService.getSlowRequests(threshold);
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs);
            response.put("count", logs.size());
            response.put("threshold", threshold);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch slow requests: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get comprehensive statistics
     */
    @GET
    @Path("/statistics")
    public Response getStatistics() {
        try {
            Map<String, Object> stats = requestLogService.getStatistics();
            return Response.ok(stats).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch statistics: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get performance summary
     */
    @GET
    @Path("/performance")
    public Response getPerformanceSummary() {
        try {
            Map<String, Object> summary = requestLogService.getPerformanceSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch performance summary: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get dashboard data (combined statistics)
     */
    @GET
    @Path("/dashboard")
    public Response getDashboardData() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Get basic statistics
            Map<String, Object> stats = requestLogService.getStatistics();
            dashboard.put("statistics", stats);
            
            // Get performance summary
            Map<String, Object> performance = requestLogService.getPerformanceSummary();
            dashboard.put("performance", performance);
            
            // Get recent requests (last 10)
            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(10);
            dashboard.put("recentRequests", recentRequests);
            
            // Get slow requests (above 500ms)
            List<RequestInfo> slowRequests = requestLogService.getSlowRequests(500);
            dashboard.put("slowRequests", slowRequests);
            
            return Response.ok(dashboard).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch dashboard data: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Clear all logs (for testing)
     */
    @DELETE
    @Path("/clear")
    public Response clearLogs() {
        try {
            requestLogService.clearLogs();
            return Response.ok(Map.of("message", "All logs cleared successfully")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to clear logs: " + e.getMessage()))
                    .build();
        }
    }
} 