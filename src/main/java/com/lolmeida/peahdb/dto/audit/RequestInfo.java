package com.lolmeida.peahdb.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {
    
    /**
     * IP address of the client making the request
     */
    private String userIp;
    
    /**
     * Real IP address when behind proxy/load balancer
     */
    private String realIp;
    
    /**
     * User agent string from the client
     */
    private String userAgent;
    
    /**
     * HTTP method (GET, POST, PUT, PATCH, DELETE, etc.)
     */
    private String httpMethod;
    
    /**
     * Complete request URI
     */
    private String requestUri;
    
    /**
     * Query parameters if any
     */
    private String queryString;
    
    /**
     * Referer header - where the request came from
     */
    private String referer;
    
    /**
     * Accept-Language header
     */
    private String acceptLanguage;
    
    /**
     * Content-Type header
     */
    private String contentType;
    
    /**
     * Session ID if available
     */
    private String sessionId;
    
    /**
     * Timestamp when the request was made
     */
    private LocalDateTime timestamp;
    
    /**
     * Request duration in milliseconds
     */
    private Long duration;
    
    /**
     * HTTP response status code
     */
    private Integer responseStatus;
    
    /**
     * Size of the response in bytes
     */
    private Long responseSize;
    
    /**
     * Browser name extracted from User-Agent
     */
    private String browserName;
    
    /**
     * Browser version extracted from User-Agent
     */
    private String browserVersion;
    
    /**
     * Operating system extracted from User-Agent
     */
    private String operatingSystem;
    
    /**
     * Device type (Desktop, Mobile, Tablet, Bot)
     */
    private String deviceType;
    
    /**
     * Country extracted from IP (if geolocation is enabled)
     */
    private String country;
    
    /**
     * City extracted from IP (if geolocation is enabled)
     */
    private String city;
    
    /**
     * Additional custom headers that might be relevant
     */
    private Map<String, String> customHeaders;
    
    /**
     * Whether the request was made over HTTPS
     */
    private Boolean isSecure;
    
    /**
     * Server name/hostname
     */
    private String serverName;
    
    /**
     * Server port
     */
    private Integer serverPort;
    
    /**
     * Authentication information if available
     */
    private String authenticatedUser;
    
    /**
     * Role/authorities of the authenticated user
     */
    private String userRoles;
    
    /**
     * Whether the request was successful (2xx status codes)
     */
    private Boolean isSuccess;
    
    /**
     * Error message if the request failed
     */
    private String errorMessage;
    
    /**
     * Request ID for tracing purposes
     */
    private String requestId;
    
    /**
     * API version if versioned API
     */
    private String apiVersion;
} 