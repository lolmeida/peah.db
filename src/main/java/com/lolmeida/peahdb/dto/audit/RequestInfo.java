package com.lolmeida.peahdb.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "RequestInfo",
    description = "Comprehensive request information including device detection, performance metrics, and client details",
    example = """
    {
      "userIp": "127.0.0.1",
      "realIp": "127.0.0.1",
      "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
      "httpMethod": "GET",
      "requestUri": "/users/1",
      "queryString": "debug=true",
      "referer": "https://example.com/dashboard",
      "contentType": "application/json",
      "acceptLanguage": "pt-PT,pt;q=0.9,en-US;q=0.8,en;q=0.7",
      "sessionId": "sess_abc123",
      "timestamp": "2025-07-18T10:30:00",
      "requestId": "req_abc123",
      "serverName": "localhost",
      "serverPort": 8080,
      "duration": 25,
      "responseStatus": 200,
      "responseSize": 1024,
      "isSuccess": true,
      "browserName": "Chrome",
      "browserVersion": "138.0.0.0",
      "operatingSystem": "macOS 10.15.7",
      "deviceType": "Desktop",
      "country": "Portugal",
      "city": "Lisbon",
      "authenticatedUser": "john_doe",
      "userRoles": ["USER"],
      "errorMessage": null,
      "customHeaders": {
        "X-Custom-Header": "custom-value"
      }
    }
    """
)
public class RequestInfo {
    
    @Schema(
        description = "IP address of the client making the request",
        example = "127.0.0.1"
    )
    private String userIp;
    
    @Schema(
        description = "Real IP address when behind proxy/load balancer",
        example = "127.0.0.1"
    )
    private String realIp;
    
    @Schema(
        description = "User agent string from the client",
        example = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"
    )
    private String userAgent;
    
    @Schema(
        description = "HTTP method (GET, POST, PUT, PATCH, DELETE, etc.)",
        example = "GET"
    )
    private String httpMethod;
    
    @Schema(
        description = "Complete request URI",
        example = "/users/1"
    )
    private String requestUri;
    
    @Schema(
        description = "Query parameters if any",
        example = "debug=true&format=json"
    )
    private String queryString;
    
    @Schema(
        description = "Referer header - where the request came from",
        example = "https://example.com/dashboard"
    )
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