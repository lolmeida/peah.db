package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import com.lolmeida.peahdb.util.RequestInfoExtractor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@Path("/monitoring")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Monitoring", description = "System monitoring and request information endpoints")
public class MonitoringResource {

    @Context
    ContainerRequestContext requestContext;

    @Context
    UriInfo uriInfo;

    @Inject
    RequestInfoExtractor requestInfoExtractor;

    @GET
    @Path("/request-info")
    @Operation(
        summary = "Get current request information",
        description = "Retrieve comprehensive information about the current HTTP request including device detection, performance metrics, and client details"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Request information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RequestInfo.class),
                examples = @ExampleObject(
                    name = "Complete Request Info",
                    description = "Example of complete request information",
                    value = """
                    {
                      "requestId": "req_abc123",
                      "httpMethod": "GET",
                      "requestUri": "/monitoring/request-info",
                      "queryString": null,
                      "userIp": "127.0.0.1",
                      "realIp": "127.0.0.1",
                      "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
                      "sessionId": null,
                      "referer": null,
                      "contentType": null,
                      "timestamp": "2025-07-18T10:30:00",
                      "browserName": "Chrome",
                      "browserVersion": "138.0.0.0",
                      "operatingSystem": "macOS 10.15.7",
                      "deviceType": "Desktop",
                      "acceptLanguage": "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7",
                      "serverName": "localhost",
                      "serverPort": 8080,
                      "customHeaders": {
                        "Accept": "application/json",
                        "User-Agent": "Mozilla/5.0..."
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
                      "error": "Internal server error",
                      "message": "Failed to extract request info: Connection timeout"
                    }
                    """
                )
            )
        )
    })
    public Response getCurrentRequestInfo() {
        try {
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            return Response.ok(requestInfo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to extract request info: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/request-summary")
    @Operation(
        summary = "Get simplified request information",
        description = "Retrieve only the most relevant request information fields for quick monitoring"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Request summary retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Request Summary",
                    description = "Example of simplified request information",
                    value = """
                    {
                      "requestId": "req_abc123",
                      "method": "GET",
                      "uri": "/monitoring/request-summary",
                      "userIp": "127.0.0.1",
                      "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
                      "browser": "Chrome",
                      "os": "macOS 10.15.7",
                      "deviceType": "Desktop",
                      "timestamp": "2025-07-18T10:30:00"
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
                      "error": "Internal server error",
                      "message": "Failed to extract request summary: Connection timeout"
                    }
                    """
                )
            )
        )
    })
    public Response getRequestSummary() {
        try {
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            // Create a simplified version
            Map<String, Object> summary = new HashMap<>();
            summary.put("requestId", requestInfo.getRequestId());
            summary.put("method", requestInfo.getHttpMethod());
            summary.put("uri", requestInfo.getRequestUri());
            summary.put("userIp", requestInfo.getUserIp());
            summary.put("userAgent", requestInfo.getUserAgent());
            summary.put("browser", requestInfo.getBrowserName());
            summary.put("os", requestInfo.getOperatingSystem());
            summary.put("deviceType", requestInfo.getDeviceType());
            summary.put("timestamp", requestInfo.getTimestamp());
            
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to extract request summary: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/health")
    @Operation(
        summary = "Health check with request information",
        description = "Perform a health check of the monitoring system and return basic system and request information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "System is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Health Check Success",
                    description = "Example of successful health check response",
                    value = """
                    {
                      "status": "OK",
                      "timestamp": "2025-07-18T10:30:00",
                      "requestId": "req_abc123",
                      "serverInfo": {
                        "serverName": "localhost",
                        "serverPort": 8080
                      },
                      "requestInfo": {
                        "method": "GET",
                        "uri": "/monitoring/health",
                        "userIp": "127.0.0.1",
                        "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"
                      }
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "System is unhealthy",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Health Check Error",
                    value = """
                    {
                      "status": "ERROR",
                      "message": "Failed to extract health info: Database connection failed",
                      "timestamp": "2025-07-18T10:30:00"
                    }
                    """
                )
            )
        )
    })
    public Response healthCheck() {
        try {
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("timestamp", requestInfo.getTimestamp());
            health.put("requestId", requestInfo.getRequestId());
            health.put("serverInfo", Map.of(
                "serverName", requestInfo.getServerName() != null ? requestInfo.getServerName() : "unknown",
                "serverPort", requestInfo.getServerPort() != null ? requestInfo.getServerPort() : -1
            ));
            health.put("requestInfo", Map.of(
                "method", requestInfo.getHttpMethod(),
                "uri", requestInfo.getRequestUri(),
                "userIp", requestInfo.getUserIp(),
                "userAgent", requestInfo.getUserAgent() != null ? requestInfo.getUserAgent() : "unknown"
            ));
            
            return Response.ok(health).build();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Failed to extract health info: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }

    @GET
    @Path("/headers")
    @Operation(
        summary = "Get request headers",
        description = "Retrieve all HTTP headers from the current request for debugging and monitoring purposes"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Headers retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "Request Headers",
                    description = "Example of request headers",
                    value = """
                    {
                      "Accept": "application/json",
                      "Accept-Encoding": "gzip, deflate",
                      "Accept-Language": "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7",
                      "Connection": "keep-alive",
                      "Host": "localhost:8080",
                      "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
                      "X-Forwarded-For": "127.0.0.1",
                      "X-Real-IP": "127.0.0.1"
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
                      "error": "Internal server error",
                      "message": "Failed to extract headers: Connection timeout"
                    }
                    """
                )
            )
        )
    })
    public Response getHeaders() {
        try {
            Map<String, String> headers = new HashMap<>();
            requestContext.getHeaders().forEach((key, values) -> {
                headers.put(key, values.get(0)); // Take the first value
            });
            
            return Response.ok(headers).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to extract headers: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/uri-info")
    @Operation(
        summary = "Get URI information",
        description = "Retrieve detailed information about the current request URI including path, query parameters, and base URI"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "URI information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.OBJECT),
                examples = @ExampleObject(
                    name = "URI Information",
                    description = "Example of detailed URI information",
                    value = """
                    {
                      "path": "monitoring/uri-info",
                      "absolutePath": "http://localhost:8080/monitoring/uri-info",
                      "baseUri": "http://localhost:8080/",
                      "requestUri": "http://localhost:8080/monitoring/uri-info",
                      "queryParams": {
                        "debug": ["true"],
                        "format": ["json"]
                      },
                      "pathParams": {}
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
                      "error": "Internal server error",
                      "message": "Failed to extract URI info: Connection timeout"
                    }
                    """
                )
            )
        )
    })
    public Response getUriInfo() {
        try {
            Map<String, Object> uriData = new HashMap<>();
            uriData.put("path", uriInfo.getPath());
            uriData.put("absolutePath", uriInfo.getAbsolutePath().toString());
            uriData.put("baseUri", uriInfo.getBaseUri().toString());
            uriData.put("requestUri", uriInfo.getRequestUri().toString());
            uriData.put("queryParams", uriInfo.getQueryParameters());
            uriData.put("pathParams", uriInfo.getPathParameters());
            
            return Response.ok(uriData).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to extract URI info: " + e.getMessage())
                    .build();
        }
    }
} 