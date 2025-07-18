# RequestInfo - Comprehensive Request Information Capture

## Overview

The `RequestInfo` class provides comprehensive information about HTTP requests, including client details, browser information, performance metrics, and security-related data. This is useful for:

- **Audit Logging** - Track all requests for compliance and security
- **Performance Monitoring** - Monitor response times and identify bottlenecks
- **Security Analysis** - Detect suspicious activities and potential threats
- **Analytics** - Understand user behavior and usage patterns
- **Debugging** - Troubleshoot issues with detailed request context

## Features

### 📊 Request Information
- **IP Address**: Client IP (with proxy support)
- **User Agent**: Complete browser/client information
- **HTTP Method**: GET, POST, PUT, DELETE, etc.
- **Request URI**: Full path and query parameters
- **Headers**: Referer, Accept-Language, Content-Type, etc.
- **Timestamps**: Request time and duration

### 🌐 Browser & Device Detection
- **Browser Name**: Chrome, Firefox, Safari, Edge, etc.
- **Browser Version**: Exact version number
- **Operating System**: Windows, macOS, Linux, Android, iOS
- **Device Type**: Desktop, Mobile, Tablet, Bot

### 🔒 Security & Audit
- **Session Information**: Session ID tracking
- **Authentication**: User and role information
- **Error Tracking**: HTTP status codes and error messages
- **Suspicious Activity**: Bot detection, unusual patterns

### ⚡ Performance Metrics
- **Response Time**: Request duration in milliseconds
- **Response Size**: Size of response data
- **Success Rate**: HTTP status code analysis

## Usage Examples

### Basic Usage

```java
@Inject
RequestInfoExtractor requestInfoExtractor;

@Context
HttpServletRequest httpServletRequest;

@GET
@Path("/example")
public Response example() {
    // Extract request information
    RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(httpServletRequest);
    
    // Use the information
    Log.infof("Request from %s using %s %s", 
              requestInfo.getUserIp(), 
              requestInfo.getBrowserName(), 
              requestInfo.getBrowserVersion());
    
    return Response.ok().build();
}
```

### With Audit Service

```java
@Inject
AuditService auditService;

@POST
@Path("/sensitive-operation")
public Response sensitiveOperation() {
    RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(httpServletRequest);
    
    // Log security event
    auditService.logSecurityEvent(requestInfo, "SENSITIVE_ACCESS", "User accessed sensitive data");
    
    // Process complete audit
    auditService.processCompleteAudit(requestInfo);
    
    return Response.ok().build();
}
```

### Using the Interceptor

The `RequestInfoInterceptor` automatically captures information for all requests:

```java
@Provider
public class RequestInfoInterceptor implements ContainerRequestFilter, ContainerResponseFilter {
    // Automatically logs all requests with comprehensive information
    // Includes performance metrics, security analysis, and audit logging
}
```

## RequestInfo Properties

### Client Information
```java
String userIp;           // Client IP address
String realIp;           // Real IP (behind proxy)
String userAgent;        // User agent string
String sessionId;        // Session identifier
```

### Request Details
```java
String httpMethod;       // HTTP method (GET, POST, etc.)
String requestUri;       // Request URI
String queryString;      // Query parameters
String referer;          // Referer header
String contentType;      // Content type
LocalDateTime timestamp; // Request timestamp
```

### Browser & Device
```java
String browserName;      // Chrome, Firefox, Safari, etc.
String browserVersion;   // Browser version
String operatingSystem;  // Windows, macOS, Linux, etc.
String deviceType;       // Desktop, Mobile, Tablet, Bot
```

### Performance & Response
```java
Long duration;           // Response time in milliseconds
Integer responseStatus;  // HTTP status code
Long responseSize;       // Response size in bytes
Boolean isSuccess;       // Whether request was successful
```

### Security & Audit
```java
String authenticatedUser; // Username if authenticated
String userRoles;        // User roles/permissions
String errorMessage;     // Error details if failed
String requestId;        // Unique request identifier
```

### Geographic & Localization
```java
String country;          // Country (if geolocation enabled)
String city;             // City (if geolocation enabled)
String acceptLanguage;   // Accept-Language header
```

## Browser Detection Examples

The system automatically detects and extracts browser information:

```java
// Chrome 120.0.6099.71
browserName = "Chrome"
browserVersion = "120.0.6099.71"

// Firefox 121.0
browserName = "Firefox"
browserVersion = "121.0"

// Safari 17.2
browserName = "Safari"
browserVersion = "17.2"
```

## Operating System Detection

```java
// Windows 11
operatingSystem = "Windows 11.0"

// macOS Sonoma
operatingSystem = "macOS 14.2"

// Android 14
operatingSystem = "Android 14"

// iOS 17
operatingSystem = "iOS 17.2"
```

## Device Type Detection

```java
deviceType = "Desktop"   // Desktop computers
deviceType = "Mobile"    // Mobile phones
deviceType = "Tablet"    // Tablets
deviceType = "Bot"       // Crawlers/bots
```

## Testing Endpoints

### View Current Request Info
```bash
GET /monitoring/request-info
```

### View Request Summary
```bash
GET /monitoring/request-summary
```

### Health Check with Request Info
```bash
GET /monitoring/health
```

## Example Response

```json
{
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userIp": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
  "httpMethod": "GET",
  "requestUri": "/api/users",
  "browserName": "Chrome",
  "browserVersion": "120.0.6099.71",
  "operatingSystem": "macOS 14.2",
  "deviceType": "Desktop",
  "timestamp": "2024-01-15T10:30:00",
  "duration": 150,
  "responseStatus": 200,
  "isSuccess": true,
  "acceptLanguage": "en-US,en;q=0.9,pt;q=0.8",
  "isSecure": true,
  "serverName": "localhost",
  "serverPort": 8080
}
```

## Configuration

### Enable/Disable Interceptor

The interceptor is automatically enabled. To disable it, remove the `@Provider` annotation or configure it conditionally:

```java
@ConditionalOnProperty(name = "app.request-info.enabled", havingValue = "true")
@Provider
public class RequestInfoInterceptor { }
```

### Customize Logging

Configure logging levels in `application.properties`:

```properties
# Enable detailed request logging
quarkus.log.level=INFO

# Enable debug logging for request info
quarkus.log.category."com.lolmeida.peahdb.interceptor".level=DEBUG
quarkus.log.category."com.lolmeida.peahdb.service.AuditService".level=DEBUG
```

## Security Considerations

1. **Sensitive Data**: Be careful not to log sensitive information
2. **Performance**: Request logging adds overhead, monitor performance
3. **Privacy**: Consider GDPR/privacy regulations when logging IP addresses
4. **Storage**: Audit logs can grow large, implement retention policies

## Best Practices

1. **Use Async Processing**: Store audit information asynchronously
2. **Filter Sensitive Endpoints**: Don't log sensitive operations
3. **Implement Retention**: Set up log rotation and retention policies
4. **Monitor Performance**: Track the impact of audit logging
5. **Secure Storage**: Encrypt audit logs if they contain sensitive data

## Future Enhancements

- [ ] Geolocation support (IP to country/city mapping)
- [ ] Rate limiting based on request patterns
- [ ] Machine learning for anomaly detection
- [ ] Integration with external monitoring systems
- [ ] Database storage for audit logs
- [ ] Real-time alerting for suspicious activities 