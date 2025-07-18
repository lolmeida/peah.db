package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService")
class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private RequestLogService requestLogService;

    private RequestInfo testRequestInfo;

    @BeforeEach
    void setUp() {
        testRequestInfo = createTestRequestInfo();
    }

    @Nested
    @DisplayName("LogRequest Tests")
    class LogRequestTest {

        @Test
        @DisplayName("Should log request information successfully")
        void testLogRequestSuccess() {
            assertDoesNotThrow(() -> auditService.logRequest(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request info gracefully")
        void testLogRequestWithNull() {
            assertDoesNotThrow(() -> auditService.logRequest(null));
        }

        @Test
        @DisplayName("Should handle request with null fields")
        void testLogRequestWithNullFields() {
            RequestInfo requestWithNulls = RequestInfo.builder()
                    .requestId("req_123")
                    .httpMethod("GET")
                    .requestUri("/api/test")
                    .responseStatus(200)
                    .build();

            assertDoesNotThrow(() -> auditService.logRequest(requestWithNulls));
        }
    }

    @Nested
    @DisplayName("LogSecurityEvent Tests")
    class LogSecurityEventTest {

        @Test
        @DisplayName("Should log security event successfully")
        void testLogSecurityEventSuccess() {
            assertDoesNotThrow(() -> 
                auditService.logSecurityEvent(testRequestInfo, "SUSPICIOUS_ACTIVITY", "Test security event")
            );
        }

        @Test
        @DisplayName("Should handle null parameters gracefully")
        void testLogSecurityEventWithNulls() {
            assertDoesNotThrow(() -> 
                auditService.logSecurityEvent(null, null, null)
            );
        }

        @Test
        @DisplayName("Should handle request with null fields")
        void testLogSecurityEventWithNullRequestFields() {
            RequestInfo requestWithNulls = RequestInfo.builder()
                    .requestId("req_123")
                    .build();

            assertDoesNotThrow(() -> 
                auditService.logSecurityEvent(requestWithNulls, "TEST_EVENT", "Test description")
            );
        }
    }

    @Nested
    @DisplayName("LogPerformanceMetrics Tests")
    class LogPerformanceMetricsTest {

        @Test
        @DisplayName("Should log performance metrics for EXCELLENT performance")
        void testLogPerformanceMetricsExcellent() {
            testRequestInfo.setDuration(50L);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should log performance metrics for GOOD performance")
        void testLogPerformanceMetricsGood() {
            testRequestInfo.setDuration(300L);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should log performance metrics for ACCEPTABLE performance")
        void testLogPerformanceMetricsAcceptable() {
            testRequestInfo.setDuration(800L);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should log performance metrics for SLOW performance")
        void testLogPerformanceMetricsSlow() {
            testRequestInfo.setDuration(3000L);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should log performance metrics for VERY_SLOW performance")
        void testLogPerformanceMetricsVerySlow() {
            testRequestInfo.setDuration(10000L);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should skip logging when duration is null")
        void testLogPerformanceMetricsNullDuration() {
            testRequestInfo.setDuration(null);
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request info")
        void testLogPerformanceMetricsNullRequest() {
            assertDoesNotThrow(() -> auditService.logPerformanceMetrics(null));
        }
    }

    @Nested
    @DisplayName("StoreRequestInfoAsync Tests")
    class StoreRequestInfoAsyncTest {

        @Test
        @DisplayName("Should return completed future successfully")
        void testStoreRequestInfoAsyncSuccess() throws ExecutionException, InterruptedException {
            CompletableFuture<Void> future = auditService.storeRequestInfoAsync(testRequestInfo);
            
            assertNotNull(future);
            assertDoesNotThrow(() -> future.get());
            assertTrue(future.isDone());
            assertFalse(future.isCompletedExceptionally());
        }

        @Test
        @DisplayName("Should handle null request info")
        void testStoreRequestInfoAsyncWithNull() throws ExecutionException, InterruptedException {
            CompletableFuture<Void> future = auditService.storeRequestInfoAsync(null);
            
            assertNotNull(future);
            assertDoesNotThrow(() -> future.get());
        }
    }

    @Nested
    @DisplayName("DetectSuspiciousActivity Tests")
    class DetectSuspiciousActivityTest {

        @Test
        @DisplayName("Should detect bot activity")
        void testDetectBotActivity() {
            testRequestInfo.setDeviceType("Bot");
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
        }

        @Test
        @DisplayName("Should detect slow requests")
        void testDetectSlowRequest() {
            testRequestInfo.setDuration(15000L);
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
        }

        @Test
        @DisplayName("Should detect error responses")
        void testDetectErrorResponse() {
            testRequestInfo.setResponseStatus(404);
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
            
            testRequestInfo.setResponseStatus(500);
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
        }

        @Test
        @DisplayName("Should detect missing user agent")
        void testDetectMissingUserAgent() {
            testRequestInfo.setUserAgent(null);
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
            
            testRequestInfo.setUserAgent("");
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
        }

        @Test
        @DisplayName("Should not detect suspicious activity for normal request")
        void testDetectNormalRequest() {
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request info")
        void testDetectSuspiciousActivityWithNull() {
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(null));
        }

        @Test
        @DisplayName("Should handle all null fields")
        void testDetectSuspiciousActivityAllNullFields() {
            RequestInfo requestWithNulls = RequestInfo.builder().build();
            assertDoesNotThrow(() -> auditService.detectSuspiciousActivity(requestWithNulls));
        }
    }

    @Nested
    @DisplayName("GenerateUsageStats Tests")
    class GenerateUsageStatsTest {

        @Test
        @DisplayName("Should generate usage stats successfully")
        void testGenerateUsageStatsSuccess() {
            assertDoesNotThrow(() -> auditService.generateUsageStats(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request info")
        void testGenerateUsageStatsWithNull() {
            assertDoesNotThrow(() -> auditService.generateUsageStats(null));
        }

        @Test
        @DisplayName("Should handle request with all null fields")
        void testGenerateUsageStatsWithNullFields() {
            RequestInfo requestWithNulls = RequestInfo.builder()
                    .requestId("req_123")
                    .build();

            assertDoesNotThrow(() -> auditService.generateUsageStats(requestWithNulls));
        }
    }

    @Nested
    @DisplayName("LogApiUsage Tests")
    class LogApiUsageTest {

        @Test
        @DisplayName("Should log API usage for API endpoints")
        void testLogApiUsageForApiEndpoint() {
            testRequestInfo.setRequestUri("/api/users/1");
            assertDoesNotThrow(() -> auditService.logApiUsage(testRequestInfo));
        }

        @Test
        @DisplayName("Should not log API usage for non-API endpoints")
        void testLogApiUsageForNonApiEndpoint() {
            testRequestInfo.setRequestUri("/health");
            assertDoesNotThrow(() -> auditService.logApiUsage(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request URI")
        void testLogApiUsageWithNullUri() {
            testRequestInfo.setRequestUri(null);
            assertDoesNotThrow(() -> auditService.logApiUsage(testRequestInfo));
        }

        @Test
        @DisplayName("Should handle null request info")
        void testLogApiUsageWithNull() {
            assertDoesNotThrow(() -> auditService.logApiUsage(null));
        }
    }

    @Nested
    @DisplayName("ProcessCompleteAudit Tests")
    class ProcessCompleteAuditTest {

        @Test
        @DisplayName("Should process complete audit successfully")
        void testProcessCompleteAuditSuccess() {
            // Arrange
            ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
            
            // Act
            assertDoesNotThrow(() -> auditService.processCompleteAudit(testRequestInfo));
            
            // Assert
            verify(requestLogService, times(1)).storeRequest(requestCaptor.capture());
            RequestInfo capturedRequest = requestCaptor.getValue();
            
            assertEquals(testRequestInfo.getRequestId(), capturedRequest.getRequestId());
            assertEquals(testRequestInfo.getUserIp(), capturedRequest.getUserIp());
            assertEquals(testRequestInfo.getRequestUri(), capturedRequest.getRequestUri());
            assertEquals(testRequestInfo.getHttpMethod(), capturedRequest.getHttpMethod());
            assertEquals(testRequestInfo.getResponseStatus(), capturedRequest.getResponseStatus());
        }

        @Test
        @DisplayName("Should handle null request info")
        void testProcessCompleteAuditWithNull() {
            // Arrange
            ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
            
            // Act
            assertDoesNotThrow(() -> auditService.processCompleteAudit(null));
            
            // Assert
            verify(requestLogService, times(1)).storeRequest(requestCaptor.capture());
            assertNull(requestCaptor.getValue());
        }

        @Test
        @DisplayName("Should handle exception from requestLogService")
        void testProcessCompleteAuditWithException() {
            // Arrange
            ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
            doThrow(new RuntimeException("Test exception")).when(requestLogService).storeRequest(any());
            
            // Act
            assertDoesNotThrow(() -> auditService.processCompleteAudit(testRequestInfo));
            
            // Assert
            verify(requestLogService, times(1)).storeRequest(requestCaptor.capture());
            assertEquals(testRequestInfo, requestCaptor.getValue());
        }

        @Test
        @DisplayName("Should process all audit steps for complete request")
        void testProcessCompleteAuditAllSteps() {
            // Arrange
            ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
            testRequestInfo.setRequestUri("/api/test");
            testRequestInfo.setDuration(5000L);
            testRequestInfo.setResponseStatus(500);
            testRequestInfo.setDeviceType("Bot");
            
            // Act
            assertDoesNotThrow(() -> auditService.processCompleteAudit(testRequestInfo));
            
            // Assert
            verify(requestLogService, times(1)).storeRequest(requestCaptor.capture());
            RequestInfo capturedRequest = requestCaptor.getValue();
            
            assertEquals("/api/test", capturedRequest.getRequestUri());
            assertEquals(5000L, capturedRequest.getDuration());
            assertEquals(500, capturedRequest.getResponseStatus());
            assertEquals("Bot", capturedRequest.getDeviceType());
        }
    }

    @Nested
    @DisplayName("GetPerformanceLevel Tests")
    class GetPerformanceLevelTest {

        @Test
        @DisplayName("Should categorize performance levels correctly")
        void testPerformanceLevels() {
            // Arrange
            AuditService auditServiceSpy = spy(auditService);
            ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
            
            // Act - Test different performance levels with separate request objects
            RequestInfo excellentRequest = createTestRequestInfo();
            excellentRequest.setDuration(50L);   // EXCELLENT
            auditServiceSpy.logPerformanceMetrics(excellentRequest);
            
            RequestInfo goodRequest = createTestRequestInfo();
            goodRequest.setDuration(250L);  // GOOD
            auditServiceSpy.logPerformanceMetrics(goodRequest);
            
            RequestInfo acceptableRequest = createTestRequestInfo();
            acceptableRequest.setDuration(750L);  // ACCEPTABLE
            auditServiceSpy.logPerformanceMetrics(acceptableRequest);
            
            RequestInfo slowRequest = createTestRequestInfo();
            slowRequest.setDuration(2500L); // SLOW
            auditServiceSpy.logPerformanceMetrics(slowRequest);
            
            RequestInfo verySlowRequest = createTestRequestInfo();
            verySlowRequest.setDuration(7500L); // VERY_SLOW
            auditServiceSpy.logPerformanceMetrics(verySlowRequest);
            
            // Assert
            verify(auditServiceSpy, times(5)).logPerformanceMetrics(requestCaptor.capture());
            
            // Verify all captured durations
            var capturedRequests = requestCaptor.getAllValues();
            assertEquals(5, capturedRequests.size());
            assertEquals(50L, capturedRequests.get(0).getDuration());
            assertEquals(250L, capturedRequests.get(1).getDuration());
            assertEquals(750L, capturedRequests.get(2).getDuration());
            assertEquals(2500L, capturedRequests.get(3).getDuration());
            assertEquals(7500L, capturedRequests.get(4).getDuration());
        }
    }

    private RequestInfo createTestRequestInfo() {
        return RequestInfo.builder()
                .requestId("req_123")
                .userIp("127.0.0.1")
                .realIp("127.0.0.1")
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/138.0.0.0")
                .httpMethod("GET")
                .requestUri("/api/users/1")
                .queryString("debug=true")
                .referer("https://example.com")
                .acceptLanguage("en-US")
                .contentType("application/json")
                .sessionId("sess_123")
                .timestamp(LocalDateTime.now())
                .duration(100L)
                .responseStatus(200)
                .responseSize(1024L)
                .browserName("Chrome")
                .browserVersion("138.0.0.0")
                .operatingSystem("macOS")
                .deviceType("Desktop")
                .country("USA")
                .city("New York")
                .isSecure(true)
                .serverName("localhost")
                .serverPort(8080)
                .authenticatedUser("testuser")
                .userRoles("USER")
                .isSuccess(true)
                .build();
    }
}