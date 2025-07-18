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

import java.util.HashMap;
import java.util.Map;

@Path("/monitoring")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MonitoringResource {

    @Context
    ContainerRequestContext requestContext;

    @Context
    UriInfo uriInfo;

    @Inject
    RequestInfoExtractor requestInfoExtractor;

    /**
     * Get current request information
     * Useful for debugging and monitoring
     */
    @GET
    @Path("/request-info")
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

    /**
     * Get simplified request information
     * Returns only the most relevant fields
     */
    @GET
    @Path("/request-summary")
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

    /**
     * Health check endpoint with request information
     * Returns 200 OK with basic system and request info
     */
    @GET
    @Path("/health")
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

    /**
     * Get headers information
     * Returns all headers from the current request
     */
    @GET
    @Path("/headers")
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

    /**
     * Get URI information
     * Returns detailed information about the current request URI
     */
    @GET
    @Path("/uri-info")
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