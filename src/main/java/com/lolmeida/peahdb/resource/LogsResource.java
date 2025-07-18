package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import com.lolmeida.peahdb.service.RequestLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/logs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Logs & Analytics", description = "Request logging, analytics and monitoring dashboard endpoints")
public class LogsResource {

    @Inject
    RequestLogService requestLogService;

    @GET
    @Operation(
        summary = "Get all request logs",
        description = "Retrieve all request logs stored in the system"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "All logs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.ARRAY)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public Response getAllLogs() {
        try {
            List<RequestInfo> logs = requestLogService.getAllLogs();
            return Response.ok(logs).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Failed to fetch logs: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/recent")
    @Operation(
        summary = "Get recent request logs",
        description = "Retrieve the most recent request logs with optional limit"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Recent logs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Recent Logs Response",
                    description = "Example of recent logs response",
                    value = """
                    {
                      "logs": [
                        {
                          "requestId": "req_abc123",
                          "httpMethod": "GET",
                          "requestUri": "/users/1",
                          "userIp": "127.0.0.1",
                          "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
                          "browserName": "Chrome",
                          "browserVersion": "138.0.0.0",
                          "operatingSystem": "macOS 10.15.7",
                          "deviceType": "Desktop",
                          "responseStatus": 200,
                          "duration": 25,
                          "timestamp": "2025-07-18T10:30:00"
                        }
                      ],
                      "count": 1,
                      "limit": 50
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch logs: Database connection failed"
                    }
                    """
                )
            )
        )
    })
    public Response getRecentLogs(
        @Parameter(
            description = "Maximum number of logs to return",
            example = "50",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "1000")
        )
        @QueryParam("limit") @DefaultValue("50") int limit
    ) {
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

    @GET
    @Path("/endpoint/{endpoint}")
    @Operation(
        summary = "Get logs by endpoint",
        description = "Retrieve logs for a specific endpoint pattern"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Endpoint logs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Endpoint Logs Response",
                    description = "Example of endpoint-specific logs",
                    value = """
                    {
                      "logs": [
                        {
                          "requestId": "req_def456",
                          "httpMethod": "GET",
                          "requestUri": "/users",
                          "userIp": "127.0.0.1",
                          "responseStatus": 200,
                          "duration": 15,
                          "timestamp": "2025-07-18T10:29:00"
                        }
                      ],
                      "count": 1,
                      "endpoint": "users"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch logs by endpoint: Invalid endpoint pattern"
                    }
                    """
                )
            )
        )
    })
    public Response getLogsByEndpoint(
        @Parameter(
            description = "Endpoint pattern to filter logs (e.g., 'users', 'monitoring')",
            required = true,
            example = "users",
            schema = @Schema(type = SchemaType.STRING, minLength = 1)
        )
        @PathParam("endpoint") String endpoint
    ) {
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

    @GET
    @Path("/status/{status}")
    @Operation(
        summary = "Get logs by status code",
        description = "Retrieve logs filtered by HTTP status code"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Status logs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Status Logs Response",
                    description = "Example of status-filtered logs",
                    value = """
                    {
                      "logs": [
                        {
                          "requestId": "req_ghi789",
                          "httpMethod": "GET",
                          "requestUri": "/users/999",
                          "userIp": "127.0.0.1",
                          "responseStatus": 404,
                          "duration": 5,
                          "timestamp": "2025-07-18T10:28:00"
                        }
                      ],
                      "count": 1,
                      "status": 404
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch logs by status: Invalid status code"
                    }
                    """
                )
            )
        )
    })
    public Response getLogsByStatus(
        @Parameter(
            description = "HTTP status code to filter logs (e.g., 200, 404, 500)",
            required = true,
            example = "404",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "100", maximum = "599")
        )
        @PathParam("status") int status
    ) {
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

    @GET
    @Path("/slow")
    @Operation(
        summary = "Get slow requests",
        description = "Retrieve requests that took longer than the specified threshold"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Slow requests retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Slow Requests Response",
                    description = "Example of slow requests",
                    value = """
                    {
                      "logs": [
                        {
                          "requestId": "req_jkl012",
                          "httpMethod": "POST",
                          "requestUri": "/users",
                          "userIp": "127.0.0.1",
                          "responseStatus": 201,
                          "duration": 1250,
                          "timestamp": "2025-07-18T10:27:00"
                        }
                      ],
                      "count": 1,
                      "threshold": 1000
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch slow requests: Database timeout"
                    }
                    """
                )
            )
        )
    })
    public Response getSlowRequests(
        @Parameter(
            description = "Minimum duration threshold in milliseconds",
            example = "1000",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")
        )
        @QueryParam("threshold") @DefaultValue("1000") long threshold
    ) {
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

    @GET
    @Path("/statistics")
    @Operation(
        summary = "Get comprehensive statistics",
        description = "Retrieve comprehensive usage statistics including request counts, browser distribution, and performance metrics"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Statistics Response",
                    description = "Example of comprehensive statistics",
                    value = """
                    {
                      "totalRequests": 1247,
                      "uniqueIPs": 23,
                      "averageResponseTime": 45.2,
                      "mostPopularEndpoints": {
                        "/users": 456,
                        "/users/1": 234,
                        "/monitoring/health": 123
                      },
                      "browserDistribution": {
                        "Chrome": 67.3,
                        "Firefox": 18.2,
                        "Safari": 14.5
                      },
                      "deviceDistribution": {
                        "Desktop": 78.9,
                        "Mobile": 18.7,
                        "Tablet": 2.4
                      },
                      "statusCodeDistribution": {
                        "200": 89.4,
                        "404": 8.7,
                        "500": 1.9
                      }
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch statistics: Memory allocation failed"
                    }
                    """
                )
            )
        )
    })
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

    @GET
    @Path("/performance")
    @Operation(
        summary = "Get performance summary",
        description = "Retrieve performance metrics including response times, throughput, and performance distribution"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Performance summary retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Performance Summary Response",
                    description = "Example of performance summary",
                    value = """
                    {
                      "averageResponseTime": 45.2,
                      "minResponseTime": 5,
                      "maxResponseTime": 1250,
                      "requestsPerSecond": 12.5,
                      "performanceDistribution": {
                        "excellent": 89.4,
                        "good": 8.7,
                        "slow": 1.9
                      },
                      "slowestEndpoints": [
                        {
                          "endpoint": "/users",
                          "averageTime": 125.5
                        },
                        {
                          "endpoint": "/monitoring/request-info",
                          "averageTime": 85.2
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch performance summary: Calculation error"
                    }
                    """
                )
            )
        )
    })
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

    @GET
    @Path("/dashboard")
    @Operation(
        summary = "Get dashboard data",
        description = "Retrieve combined dashboard data including statistics, performance metrics, recent requests, and slow requests"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Dashboard data retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Dashboard Response",
                    description = "Example of complete dashboard data",
                    value = """
                    {
                      "statistics": {
                        "totalRequests": 1247,
                        "uniqueIPs": 23,
                        "averageResponseTime": 45.2
                      },
                      "performance": {
                        "averageResponseTime": 45.2,
                        "requestsPerSecond": 12.5,
                        "performanceDistribution": {
                          "excellent": 89.4,
                          "good": 8.7,
                          "slow": 1.9
                        }
                      },
                      "recentRequests": [
                        {
                          "requestId": "req_abc123",
                          "httpMethod": "GET",
                          "requestUri": "/users/1",
                          "responseStatus": 200,
                          "duration": 25,
                          "timestamp": "2025-07-18T10:30:00"
                        }
                      ],
                      "slowRequests": [
                        {
                          "requestId": "req_def456",
                          "httpMethod": "POST",
                          "requestUri": "/users",
                          "responseStatus": 201,
                          "duration": 750,
                          "timestamp": "2025-07-18T10:29:00"
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to fetch dashboard data: Service unavailable"
                    }
                    """
                )
            )
        )
    })
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

    @DELETE
    @Path("/clear")
    @Operation(
        summary = "Clear all logs",
        description = "Clear all stored request logs from memory (primarily for testing purposes)"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Logs cleared successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Clear Success Response",
                    value = """
                    {
                      "message": "All logs cleared successfully"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                      "error": "Failed to clear logs: Access denied"
                    }
                    """
                )
            )
        )
    })
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