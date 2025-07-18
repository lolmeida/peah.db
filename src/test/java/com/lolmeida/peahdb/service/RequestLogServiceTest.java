package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestLogService")
class RequestLogServiceTest {

    @InjectMocks
    private RequestLogService requestLogService;

    private RequestInfo testRequestInfo;

    @BeforeEach
    void setUp() {
        requestLogService.clearLogs();
        testRequestInfo = createTestRequestInfo();
    }

    @Nested
    @DisplayName("StoreRequest Tests")
    class StoreRequestTest {

        @Test
        @DisplayName("Should store request successfully")
        void testStoreRequestSuccess() {
            requestLogService.storeRequest(testRequestInfo);

            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(10);
            assertEquals(1, recentRequests.size());
            assertEquals(testRequestInfo.getRequestId(), recentRequests.get(0).getRequestId());
        }

        @Test
        @DisplayName("Should update statistics when storing request")
        void testStoreRequestUpdatesStatistics() {
            requestLogService.storeRequest(testRequestInfo);

            Map<String, Object> stats = requestLogService.getStatistics();
            assertEquals(1, stats.get("totalRequests"));
            
            Map<String, Integer> endpointCounts = (Map<String, Integer>) stats.get("endpointCounts");
            assertEquals(1, endpointCounts.get("/api/users/1"));
            
            Map<String, Integer> browserCounts = (Map<String, Integer>) stats.get("browserCounts");
            assertEquals(1, browserCounts.get("Chrome"));
            
            Map<String, Integer> deviceCounts = (Map<String, Integer>) stats.get("deviceCounts");
            assertEquals(1, deviceCounts.get("Desktop"));
            
            Map<String, Integer> osCounts = (Map<String, Integer>) stats.get("osCounts");
            assertEquals(1, osCounts.get("macOS"));
        }

        @Test
        @DisplayName("Should maintain maximum of 1000 requests")
        void testStoreRequestMaxLimit() {
            for (int i = 0; i < 1005; i++) {
                RequestInfo request = createTestRequestInfo();
                request.setRequestId("req_" + i);
                requestLogService.storeRequest(request);
            }

            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(2000);
            assertEquals(1000, recentRequests.size());
            assertEquals("req_1004", recentRequests.get(0).getRequestId());
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void testStoreRequestWithNulls() {
            RequestInfo requestWithNulls = RequestInfo.builder()
                    .requestId("req_null")
                    .requestUri("/api/test")
                    .httpMethod("GET")
                    .timestamp(LocalDateTime.now())
                    .responseStatus(200)
                    .build();

            requestLogService.storeRequest(requestWithNulls);

            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(10);
            assertEquals(1, recentRequests.size());
            assertEquals("req_null", recentRequests.get(0).getRequestId());
        }

        @Test
        @DisplayName("Should handle exception during store request")
        void testStoreRequestWithException() {
            // Since we can't easily inject a mock list, we'll test with a request 
            // that has properties that could potentially cause issues
            RequestInfo problematicRequest = new RequestInfo() {
                @Override
                public String getRequestUri() {
                    throw new RuntimeException("Simulated exception in getRequestUri");
                }
            };
            
            // Set other required fields
            problematicRequest.setRequestId("problematic_req");
            problematicRequest.setTimestamp(LocalDateTime.now());
            problematicRequest.setResponseStatus(200);
            
            // This should not throw an exception due to try-catch block in storeRequest
            assertDoesNotThrow(() -> requestLogService.storeRequest(problematicRequest));
            
            // The request should still be stored in the list even if updateStatistics fails
            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(10);
            assertEquals(1, recentRequests.size());
            
            // But statistics should not be updated - verify endpointCounts is empty
            Map<String, Object> stats = requestLogService.getStatistics();
            Map<String, Integer> endpointCounts = (Map<String, Integer>) stats.get("endpointCounts");
            assertTrue(endpointCounts.isEmpty(), "Endpoint counts should be empty due to exception");
        }
    }

    @Nested
    @DisplayName("GetRecentRequests Tests")
    class GetRecentRequestsTest {

        @Test
        @DisplayName("Should return recent requests in descending order")
        void testGetRecentRequestsOrder() {
            RequestInfo request1 = createTestRequestInfo();
            request1.setRequestId("req_1");
            request1.setTimestamp(LocalDateTime.now().minusMinutes(2));
            
            RequestInfo request2 = createTestRequestInfo();
            request2.setRequestId("req_2");
            request2.setTimestamp(LocalDateTime.now().minusMinutes(1));
            
            RequestInfo request3 = createTestRequestInfo();
            request3.setRequestId("req_3");
            request3.setTimestamp(LocalDateTime.now());

            requestLogService.storeRequest(request1);
            requestLogService.storeRequest(request2);
            requestLogService.storeRequest(request3);

            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(3);
            assertEquals(3, recentRequests.size());
            assertEquals("req_3", recentRequests.get(0).getRequestId());
            assertEquals("req_2", recentRequests.get(1).getRequestId());
            assertEquals("req_1", recentRequests.get(2).getRequestId());
        }

        @Test
        @DisplayName("Should limit results according to parameter")
        void testGetRecentRequestsLimit() {
            for (int i = 0; i < 10; i++) {
                RequestInfo request = createTestRequestInfo();
                request.setRequestId("req_" + i);
                requestLogService.storeRequest(request);
            }

            List<RequestInfo> recentRequests = requestLogService.getRecentRequests(5);
            assertEquals(5, recentRequests.size());
        }
    }

    @Nested
    @DisplayName("GetRequestsByEndpoint Tests")
    class GetRequestsByEndpointTest {

        @Test
        @DisplayName("Should filter requests by endpoint")
        void testGetRequestsByEndpoint() {
            RequestInfo request1 = createTestRequestInfo();
            request1.setRequestUri("/api/users/1");
            
            RequestInfo request2 = createTestRequestInfo();
            request2.setRequestUri("/api/products/1");
            
            RequestInfo request3 = createTestRequestInfo();
            request3.setRequestUri("/api/users/2");

            requestLogService.storeRequest(request1);
            requestLogService.storeRequest(request2);
            requestLogService.storeRequest(request3);

            List<RequestInfo> userRequests = requestLogService.getRequestsByEndpoint("/users");
            assertEquals(2, userRequests.size());
            assertTrue(userRequests.stream().allMatch(req -> req.getRequestUri().contains("/users")));
        }

        @Test
        @DisplayName("Should return empty list when no matching endpoint")
        void testGetRequestsByEndpointNoMatch() {
            requestLogService.storeRequest(testRequestInfo);

            List<RequestInfo> requests = requestLogService.getRequestsByEndpoint("/nonexistent");
            assertTrue(requests.isEmpty());
        }
    }

    @Nested
    @DisplayName("GetRequestsByStatus Tests")
    class GetRequestsByStatusTest {

        @Test
        @DisplayName("Should filter requests by status code")
        void testGetRequestsByStatus() {
            RequestInfo request200 = createTestRequestInfo();
            request200.setResponseStatus(200);
            
            RequestInfo request404 = createTestRequestInfo();
            request404.setResponseStatus(404);
            
            RequestInfo request500 = createTestRequestInfo();
            request500.setResponseStatus(500);

            requestLogService.storeRequest(request200);
            requestLogService.storeRequest(request404);
            requestLogService.storeRequest(request500);

            List<RequestInfo> successRequests = requestLogService.getRequestsByStatus(200);
            assertEquals(1, successRequests.size());
            assertEquals(200, successRequests.get(0).getResponseStatus());

            List<RequestInfo> errorRequests = requestLogService.getRequestsByStatus(500);
            assertEquals(1, errorRequests.size());
            assertEquals(500, errorRequests.get(0).getResponseStatus());
        }
    }

    @Nested
    @DisplayName("GetSlowRequests Tests")
    class GetSlowRequestsTest {

        @Test
        @DisplayName("Should filter requests above duration threshold")
        void testGetSlowRequests() {
            RequestInfo fastRequest = createTestRequestInfo();
            fastRequest.setDuration(50L);
            
            RequestInfo mediumRequest = createTestRequestInfo();
            mediumRequest.setDuration(500L);
            
            RequestInfo slowRequest = createTestRequestInfo();
            slowRequest.setDuration(2000L);

            requestLogService.storeRequest(fastRequest);
            requestLogService.storeRequest(mediumRequest);
            requestLogService.storeRequest(slowRequest);

            List<RequestInfo> slowRequests = requestLogService.getSlowRequests(1000L);
            assertEquals(1, slowRequests.size());
            assertEquals(2000L, slowRequests.get(0).getDuration());
        }

        @Test
        @DisplayName("Should order slow requests by duration descending")
        void testGetSlowRequestsOrder() {
            RequestInfo request1 = createTestRequestInfo();
            request1.setDuration(1500L);
            
            RequestInfo request2 = createTestRequestInfo();
            request2.setDuration(3000L);
            
            RequestInfo request3 = createTestRequestInfo();
            request3.setDuration(2000L);

            requestLogService.storeRequest(request1);
            requestLogService.storeRequest(request2);
            requestLogService.storeRequest(request3);

            List<RequestInfo> slowRequests = requestLogService.getSlowRequests(1000L);
            assertEquals(3, slowRequests.size());
            assertEquals(3000L, slowRequests.get(0).getDuration());
            assertEquals(2000L, slowRequests.get(1).getDuration());
            assertEquals(1500L, slowRequests.get(2).getDuration());
        }

        @Test
        @DisplayName("Should handle null durations")
        void testGetSlowRequestsWithNullDuration() {
            RequestInfo requestWithDuration = createTestRequestInfo();
            requestWithDuration.setDuration(2000L);
            
            RequestInfo requestNullDuration = createTestRequestInfo();
            requestNullDuration.setDuration(null);

            requestLogService.storeRequest(requestWithDuration);
            requestLogService.storeRequest(requestNullDuration);

            List<RequestInfo> slowRequests = requestLogService.getSlowRequests(1000L);
            assertEquals(1, slowRequests.size());
            assertEquals(2000L, slowRequests.get(0).getDuration());
        }
    }

    @Nested
    @DisplayName("GetStatistics Tests")
    class GetStatisticsTest {

        @Test
        @DisplayName("Should return comprehensive statistics")
        void testGetStatistics() {
            for (int i = 0; i < 5; i++) {
                RequestInfo request = createTestRequestInfo();
                request.setResponseStatus(i < 3 ? 200 : 404);
                request.setDuration(100L * (i + 1));
                requestLogService.storeRequest(request);
            }

            Map<String, Object> stats = requestLogService.getStatistics();
            
            assertEquals(5, stats.get("totalRequests"));
            assertNotNull(stats.get("endpointCounts"));
            assertNotNull(stats.get("browserCounts"));
            assertNotNull(stats.get("deviceCounts"));
            assertNotNull(stats.get("osCounts"));
            assertNotNull(stats.get("performanceStats"));
            assertNotNull(stats.get("statusCounts"));
            assertNotNull(stats.get("recentRequests"));
            
            Map<Integer, Long> statusCounts = (Map<Integer, Long>) stats.get("statusCounts");
            assertEquals(3L, statusCounts.get(200));
            assertEquals(2L, statusCounts.get(404));
        }

        @Test
        @DisplayName("Should calculate performance statistics per endpoint")
        void testGetStatisticsPerformance() {
            RequestInfo request1 = createTestRequestInfo();
            request1.setRequestUri("/api/users");
            request1.setDuration(100L);
            
            RequestInfo request2 = createTestRequestInfo();
            request2.setRequestUri("/api/users");
            request2.setDuration(200L);
            
            RequestInfo request3 = createTestRequestInfo();
            request3.setRequestUri("/api/users");
            request3.setDuration(300L);

            requestLogService.storeRequest(request1);
            requestLogService.storeRequest(request2);
            requestLogService.storeRequest(request3);

            Map<String, Object> stats = requestLogService.getStatistics();
            Map<String, Object> perfStats = (Map<String, Object>) stats.get("performanceStats");
            Map<String, Object> userEndpointStats = (Map<String, Object>) perfStats.get("/api/users");
            
            assertEquals(200L, userEndpointStats.get("avgDuration"));
            assertEquals(300L, userEndpointStats.get("maxDuration"));
            assertEquals(100L, userEndpointStats.get("minDuration"));
            assertEquals(3, userEndpointStats.get("requestCount"));
        }
    }

    @Nested
    @DisplayName("GetPerformanceSummary Tests")
    class GetPerformanceSummaryTest {

        @Test
        @DisplayName("Should categorize requests by performance level")
        void testGetPerformanceSummary() {
            RequestInfo excellent = createTestRequestInfo();
            excellent.setDuration(30L);
            
            RequestInfo good = createTestRequestInfo();
            good.setDuration(150L);
            
            RequestInfo average = createTestRequestInfo();
            average.setDuration(500L);
            
            RequestInfo slow = createTestRequestInfo();
            slow.setDuration(2000L);

            requestLogService.storeRequest(excellent);
            requestLogService.storeRequest(good);
            requestLogService.storeRequest(average);
            requestLogService.storeRequest(slow);

            Map<String, Object> summary = requestLogService.getPerformanceSummary();
            
            assertEquals(1L, (long) summary.get("excellentRequests"));
            assertEquals(1L, (long) summary.get("goodRequests"));
            assertEquals(1L, (long) summary.get("averageRequests"));
            assertEquals(1L, (long) summary.get("slowRequests"));
            assertEquals(4, summary.get("totalRequests"));
            assertEquals(670L, (long) summary.get("averageDuration"));
            assertEquals(2000L, (long) summary.get("maxDuration"));
        }

        @Test
        @DisplayName("Should handle empty logs")
        void testGetPerformanceSummaryEmpty() {
            Map<String, Object> summary = requestLogService.getPerformanceSummary();
            
            assertEquals("No requests logged yet", summary.get("message"));
        }

        @Test
        @DisplayName("Should handle requests without duration")
        void testGetPerformanceSummaryNullDurations() {
            RequestInfo withDuration = createTestRequestInfo();
            withDuration.setDuration(100L);
            
            RequestInfo withoutDuration = createTestRequestInfo();
            withoutDuration.setDuration(null);

            requestLogService.storeRequest(withDuration);
            requestLogService.storeRequest(withoutDuration);

            Map<String, Object> summary = requestLogService.getPerformanceSummary();
            
            assertEquals(100L, (long) summary.get("averageDuration"));
            assertEquals(100L, (long) summary.get("maxDuration"));
            assertEquals(2, summary.get("totalRequests"));
        }
    }

    @Nested
    @DisplayName("ClearLogs Tests")
    class ClearLogsTest {

        @Test
        @DisplayName("Should clear all logs and statistics")
        void testClearLogs() {
            for (int i = 0; i < 5; i++) {
                requestLogService.storeRequest(createTestRequestInfo());
            }

            Map<String, Object> statsBeforeClear = requestLogService.getStatistics();
            assertEquals(5, statsBeforeClear.get("totalRequests"));

            requestLogService.clearLogs();

            List<RequestInfo> requests = requestLogService.getRecentRequests(10);
            assertTrue(requests.isEmpty());

            Map<String, Object> statsAfterClear = requestLogService.getStatistics();
            assertEquals(0, statsAfterClear.get("totalRequests"));
            assertTrue(((Map) statsAfterClear.get("endpointCounts")).isEmpty());
            assertTrue(((Map) statsAfterClear.get("browserCounts")).isEmpty());
            assertTrue(((Map) statsAfterClear.get("deviceCounts")).isEmpty());
            assertTrue(((Map) statsAfterClear.get("osCounts")).isEmpty());
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