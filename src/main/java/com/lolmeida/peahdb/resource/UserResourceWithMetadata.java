package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.audit.RequestInfo;
import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.dto.response.ApiResponse;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.repository.UserRepository;
import com.lolmeida.peahdb.service.UserService;
import com.lolmeida.peahdb.util.RequestInfoExtractor;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResourceWithMetadata {

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @Inject
    MapperService mapper;

    @Inject
    RequestInfoExtractor requestInfoExtractor;

    @Context
    ContainerRequestContext requestContext;

    /**
     * Get all users with device metadata
     */
    @GET
    public Response getAllUsers() {
        try {
            // Get users data directly from repository
            List<UserResponse> users = userRepository.listAll().stream()
                    .map(mapper::toUserResponse)
                    .toList();
            
            // Extract request info for metadata
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            // Create metadata
            ApiResponse.ResponseMetadata metadata = ApiResponse.createMetadata(
                    requestInfo.getRequestId(),
                    "0ms", // Will be updated by interceptor
                    requestInfo.getDeviceType(),
                    requestInfo.getBrowserName() + " " + requestInfo.getBrowserVersion(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getUserAgent(),
                    requestInfo.getUserIp()
            );
            
            // Create response with metadata
            ApiResponse<List<UserResponse>> response = ApiResponse.success(users, metadata);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            ApiResponse<Object> errorResponse = ApiResponse.error("Failed to fetch users: " + e.getMessage(), 500);
            return Response.status(500).entity(errorResponse).build();
        }
    }

    /**
     * Get user by ID with device metadata
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        try {
            // Get user data directly from repository
            var userOptional = userRepository.findByIdOptional(id);
            
            if (userOptional.isEmpty()) {
                ApiResponse<Object> errorResponse = ApiResponse.error("User not found", 404);
                return Response.status(404).entity(errorResponse).build();
            }
            
            UserResponse user = mapper.toUserResponse(userOptional.get());
            
            // Extract request info for metadata
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            // Create metadata
            ApiResponse.ResponseMetadata metadata = ApiResponse.createMetadata(
                    requestInfo.getRequestId(),
                    "0ms", // Will be updated by interceptor
                    requestInfo.getDeviceType(),
                    requestInfo.getBrowserName() + " " + requestInfo.getBrowserVersion(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getUserAgent(),
                    requestInfo.getUserIp()
            );
            
            // Create response with metadata
            ApiResponse<UserResponse> response = ApiResponse.success(user, metadata);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            ApiResponse<Object> errorResponse = ApiResponse.error("Failed to fetch user: " + e.getMessage(), 500);
            return Response.status(500).entity(errorResponse).build();
        }
    }

    /**
     * Get user device information only
     */
    @GET
    @Path("/device-info")
    public Response getDeviceInfo() {
        try {
            // Extract request info
            RequestInfo requestInfo = requestInfoExtractor.extractRequestInfo(requestContext);
            
            // Create device info response
            ApiResponse.DeviceInfo deviceInfo = ApiResponse.DeviceInfo.builder()
                    .deviceType(requestInfo.getDeviceType())
                    .browser(requestInfo.getBrowserName() + " " + requestInfo.getBrowserVersion())
                    .operatingSystem(requestInfo.getOperatingSystem())
                    .userAgent(requestInfo.getUserAgent())
                    .userIp(requestInfo.getUserIp())
                    .build();
            
            // Create metadata
            ApiResponse.ResponseMetadata metadata = ApiResponse.createMetadata(
                    requestInfo.getRequestId(),
                    "0ms",
                    requestInfo.getDeviceType(),
                    requestInfo.getBrowserName() + " " + requestInfo.getBrowserVersion(),
                    requestInfo.getOperatingSystem(),
                    requestInfo.getUserAgent(),
                    requestInfo.getUserIp()
            );
            
            // Create response
            ApiResponse<ApiResponse.DeviceInfo> response = ApiResponse.success(deviceInfo, metadata);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            ApiResponse<Object> errorResponse = ApiResponse.error("Failed to get device info: " + e.getMessage(), 500);
            return Response.status(500).entity(errorResponse).build();
        }
    }
} 