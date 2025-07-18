package com.lolmeida.peahdb.interceptor;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import com.lolmeida.peahdb.service.AuditService;
import com.lolmeida.peahdb.util.RequestInfoExtractor;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class RequestInfoInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    RequestInfoExtractor requestInfoExtractor;

    @Inject
    AuditService auditService;

    private static final String REQUEST_INFO_PROPERTY = "REQUEST_INFO";
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Record start time
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(REQUEST_START_TIME, startTime);

        try {
            // Extract request information
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            // Store in context for later use
            requestContext.setProperty(REQUEST_INFO_PROPERTY, requestInfo);
            
            // Log the request (you can customize this)
            Log.infof("🔍 Request: %s %s from %s (%s) - %s %s [%s]",
                    requestInfo.getHttpMethod(),
                    requestInfo.getRequestUri(),
                    requestInfo.getUserIp(),
                    requestInfo.getBrowserName(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getDeviceType(),
                    requestInfo.getRequestId());
            
        } catch (Exception e) {
            Log.errorf("Failed to extract request info: %s", e.getMessage());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        try {
            // Calculate duration
            Long startTime = (Long) requestContext.getProperty(REQUEST_START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0L;
            
            // Get the stored request info
            RequestInfo requestInfo = (RequestInfo) requestContext.getProperty(REQUEST_INFO_PROPERTY);
            
            if (requestInfo != null) {
                // Update request info with response data
                int statusCode = responseContext.getStatus();
                long responseSize = estimateResponseSize(responseContext);
                
                RequestInfo completeInfo = requestInfoExtractor.extractRequestInfo(
                        requestContext, duration, statusCode, responseSize);
                
                // Log the response
                Log.infof("📤 Response: %s %s -> %d (%dms) [%s]",
                        completeInfo.getHttpMethod(),
                        completeInfo.getRequestUri(),
                        completeInfo.getResponseStatus(),
                        completeInfo.getDuration(),
                        completeInfo.getRequestId());
                
                // Process complete audit
                auditService.processCompleteAudit(completeInfo);
            }
        } catch (Exception e) {
            Log.errorf("Failed to process response info: %s", e.getMessage());
        }
    }

    /**
     * Estimate response size (basic implementation)
     */
    private long estimateResponseSize(ContainerResponseContext responseContext) {
        try {
            if (responseContext.getEntity() != null) {
                String entity = responseContext.getEntity().toString();
                return entity.getBytes().length;
            }
        } catch (Exception e) {
            // Ignore estimation errors
        }
        return 0L;
    }
} 