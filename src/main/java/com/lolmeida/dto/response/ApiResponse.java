package com.lolmeida.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private T data;
    private ResponseMetadata metadata;
    private Boolean success;
    private String message;
    private Integer status;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {
        private String requestId;
        private LocalDateTime timestamp;
        private String responseTime;
        private DeviceInfo deviceInfo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeviceInfo {
        private String deviceType;
        private String browser;
        private String operatingSystem;
        private String userAgent;
        private String userIp;
    }
    
    /**
     * Create a successful response with data and metadata
     */
    public static <T> ApiResponse<T> success(T data, ResponseMetadata metadata) {
        return ApiResponse.<T>builder()
                .data(data)
                .metadata(metadata)
                .success(true)
                .status(200)
                .build();
    }
    
    /**
     * Create a successful response with data only
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(true)
                .status(200)
                .build();
    }
    
    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
                .message(message)
                .success(false)
                .status(status)
                .build();
    }
    
    /**
     * Create metadata from request info
     */
    public static ResponseMetadata createMetadata(String requestId, String responseTime, 
                                                 String deviceType, String browser, 
                                                 String operatingSystem, String userAgent, 
                                                 String userIp) {
        DeviceInfo deviceInfo = DeviceInfo.builder()
                .deviceType(deviceType)
                .browser(browser)
                .operatingSystem(operatingSystem)
                .userAgent(userAgent)
                .userIp(userIp)
                .build();
        
        return ResponseMetadata.builder()
                .requestId(requestId)
                .timestamp(LocalDateTime.now())
                .responseTime(responseTime)
                .deviceInfo(deviceInfo)
                .build();
    }
} 