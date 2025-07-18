package com.lolmeida.peahdb.util;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class RequestInfoExtractor {
    
    // Patterns for User-Agent parsing
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/([\\d\\.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/([\\d\\.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/([\\d\\.]+).*Safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg/([\\d\\.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/([\\d\\.]+)");
    
    // OS Patterns
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([\\d\\.]+)");
    private static final Pattern MACOS_PATTERN = Pattern.compile("Mac OS X ([\\d_]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([\\d\\.]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("OS ([\\d_]+)");
    
    // Device type patterns
    private static final Pattern MOBILE_PATTERN = Pattern.compile("Mobile|Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini");
    private static final Pattern TABLET_PATTERN = Pattern.compile("iPad|Android.*Tablet");
    private static final Pattern BOT_PATTERN = Pattern.compile("bot|crawler|spider|scraper", Pattern.CASE_INSENSITIVE);
    
    /**
     * Extract complete request information from ContainerRequestContext
     */
    public RequestInfo extractRequestInfo(ContainerRequestContext requestContext) {
        LocalDateTime timestamp = LocalDateTime.now();
        
        return RequestInfo.builder()
                .userIp(extractUserIp(requestContext))
                .realIp(extractRealIp(requestContext))
                .userAgent(getHeader(requestContext, "User-Agent"))
                .httpMethod(requestContext.getMethod())
                .requestUri(requestContext.getUriInfo().getPath())
                .queryString(requestContext.getUriInfo().getRequestUri().getQuery())
                .referer(getHeader(requestContext, "Referer"))
                .acceptLanguage(getHeader(requestContext, "Accept-Language"))
                .contentType(getHeader(requestContext, "Content-Type"))
                .sessionId(generateSessionId()) // Generate a unique session ID
                .requestId(generateRequestId())
                .timestamp(timestamp)
                .browserName(extractBrowserName(getHeader(requestContext, "User-Agent")))
                .browserVersion(extractBrowserVersion(getHeader(requestContext, "User-Agent")))
                .operatingSystem(extractOperatingSystem(getHeader(requestContext, "User-Agent")))
                .deviceType(extractDeviceType(getHeader(requestContext, "User-Agent")))
                .customHeaders(extractCustomHeaders(requestContext))
                .build();
    }
    
    /**
     * Extract user IP address from request headers
     */
    private String extractUserIp(ContainerRequestContext requestContext) {
        // Check for forwarded headers first
        String ip = getHeader(requestContext, "X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return ip.split(",")[0].trim();
        }
        
        ip = getHeader(requestContext, "X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = getHeader(requestContext, "X-Forwarded");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = getHeader(requestContext, "X-Cluster-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        // Fallback to remote address (not available in JAX-RS context)
        return "unknown";
    }
    
    /**
     * Extract real IP address (same as user IP for now)
     */
    private String extractRealIp(ContainerRequestContext requestContext) {
        return extractUserIp(requestContext);
    }
    
    /**
     * Extract browser name from User-Agent
     */
    private String extractBrowserName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        if (CHROME_PATTERN.matcher(userAgent).find()) {
            return "Chrome";
        } else if (FIREFOX_PATTERN.matcher(userAgent).find()) {
            return "Firefox";
        } else if (SAFARI_PATTERN.matcher(userAgent).find()) {
            return "Safari";
        } else if (EDGE_PATTERN.matcher(userAgent).find()) {
            return "Edge";
        } else if (OPERA_PATTERN.matcher(userAgent).find()) {
            return "Opera";
        }
        
        return "Unknown";
    }
    
    /**
     * Extract browser version from User-Agent
     */
    private String extractBrowserVersion(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        Pattern[] patterns = {CHROME_PATTERN, FIREFOX_PATTERN, SAFARI_PATTERN, EDGE_PATTERN, OPERA_PATTERN};
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return "Unknown";
    }
    
    /**
     * Extract operating system from User-Agent
     */
    private String extractOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        Matcher matcher = WINDOWS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Windows " + matcher.group(1);
        }
        
        matcher = MACOS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "macOS " + matcher.group(1).replace("_", ".");
        }
        
        matcher = ANDROID_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Android " + matcher.group(1);
        }
        
        matcher = IOS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "iOS " + matcher.group(1).replace("_", ".");
        }
        
        if (LINUX_PATTERN.matcher(userAgent).find()) {
            return "Linux";
        }
        
        return "Unknown";
    }
    
    /**
     * Extract device type from User-Agent
     */
    private String extractDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        if (BOT_PATTERN.matcher(userAgent).find()) {
            return "Bot";
        }
        
        if (TABLET_PATTERN.matcher(userAgent).find()) {
            return "Tablet";
        }
        
        if (MOBILE_PATTERN.matcher(userAgent).find()) {
            return "Mobile";
        }
        
        return "Desktop";
    }
    
    /**
     * Check if request is from a bot
     */
    private boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return false;
        }
        
        return BOT_PATTERN.matcher(userAgent).find();
    }
    
    /**
     * Extract custom headers (non-standard headers)
     */
    private Map<String, String> extractCustomHeaders(ContainerRequestContext requestContext) {
        Map<String, String> customHeaders = new HashMap<>();
        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        
        for (String headerName : headers.keySet()) {
            // Include custom headers (those starting with X- or custom app headers)
            if (headerName.toLowerCase().startsWith("x-") || 
                headerName.toLowerCase().startsWith("custom-") ||
                headerName.toLowerCase().startsWith("app-")) {
                customHeaders.put(headerName, headers.getFirst(headerName));
            }
        }
        
        return customHeaders;
    }
    
    /**
     * Generate a unique session ID
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generate a unique request ID
     */
    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Get header value safely
     */
    private String getHeader(ContainerRequestContext requestContext, String headerName) {
        return requestContext.getHeaders().getFirst(headerName);
    }
    
    /**
     * Extract request information with additional context
     */
    public RequestInfo extractRequestInfo(ContainerRequestContext requestContext, Long duration, Integer responseStatus, Long responseSize) {
        RequestInfo baseInfo = extractRequestInfo(requestContext);
        
        return RequestInfo.builder()
                .userIp(baseInfo.getUserIp())
                .realIp(baseInfo.getRealIp())
                .userAgent(baseInfo.getUserAgent())
                .httpMethod(baseInfo.getHttpMethod())
                .requestUri(baseInfo.getRequestUri())
                .queryString(baseInfo.getQueryString())
                .referer(baseInfo.getReferer())
                .acceptLanguage(baseInfo.getAcceptLanguage())
                .contentType(baseInfo.getContentType())
                .sessionId(baseInfo.getSessionId())
                .requestId(baseInfo.getRequestId())
                .timestamp(baseInfo.getTimestamp())
                .browserName(baseInfo.getBrowserName())
                .browserVersion(baseInfo.getBrowserVersion())
                .operatingSystem(baseInfo.getOperatingSystem())
                .deviceType(baseInfo.getDeviceType())
                .customHeaders(baseInfo.getCustomHeaders())
                .duration(duration)
                .responseStatus(responseStatus)
                .responseSize(responseSize)
                .isSuccess(responseStatus != null && responseStatus >= 200 && responseStatus < 300)
                .build();
    }
} 