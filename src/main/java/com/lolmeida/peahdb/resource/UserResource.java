package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(
        summary = "📋 Get All Users",
        description = """
            ## 📋 **Get All Users**
            
            Retrieves all users from the system with comprehensive information.
            
            ### ✅ **Features:**
            - **Complete User List**: Returns all users in the system
            - **Security**: Excludes password information
            - **Monitoring**: Includes request monitoring in headers
            - **Performance**: Optimized database query
            
            ### 📊 **Response Information:**
            - **User Data**: Complete user information without passwords
            - **Timestamps**: Creation and update timestamps
            - **Sorting**: Users sorted by creation date (newest first)
            - **Headers**: Monitoring information included
            """
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "✅ **Success** - List of all users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse[].class),
                examples = @ExampleObject(
                    name = "Users List Response",
                    summary = "List of users with complete information",
                    value = """
                        [
                          {
                            "id": 123,
                            "username": "john_doe",
                            "email": "john@example.com",
                            "fullName": "John Doe",
                            "createdAt": "2024-01-20T10:30:00Z",
                            "updatedAt": "2024-01-20T10:30:00Z"
                          },
                          {
                            "id": 124,
                            "username": "jane_smith",
                            "email": "jane@example.com",
                            "fullName": "Jane Smith",
                            "createdAt": "2024-01-20T11:00:00Z",
                            "updatedAt": "2024-01-20T11:00:00Z"
                          }
                        ]
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "🚨 **Internal Server Error** - Database or server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Internal server error occurred",
                    value = """
                        {
                          "error": "Internal server error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-20T10:30:00Z",
                          "requestId": "req-123456789"
                        }
                        """
                )
            )
        )
    })
    public Response getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    @Operation(
        summary = "🔍 Get User by ID",
        description = """
            ## 🔍 **Get User by ID**
            
            Retrieves a specific user by their unique identifier.
            
            ### ✅ **Features:**
            - **Specific Lookup**: Find user by exact ID
            - **Security**: Password information excluded
            - **Error Handling**: Clear 404 response if user not found
            - **Monitoring**: Request tracking in headers
            
            ### 🎯 **Use Cases:**
            - User profile retrieval
            - Account information display
            - User verification
            - Admin user management
            """
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "✅ **Success** - User found and returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(
                    name = "User Response",
                    summary = "Complete user information response",
                    value = """
                        {
                          "id": 123,
                          "username": "john_doe",
                          "email": "john@example.com",
                          "fullName": "John Doe",
                          "createdAt": "2024-01-20T10:30:00Z",
                          "updatedAt": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "🔍 **Not Found** - User with specified ID not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Resource Not Found",
                    summary = "Resource with specified ID not found",
                    value = """
                        {
                          "error": "Resource not found",
                          "message": "User with ID 999 not found",
                          "timestamp": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "❌ **Bad Request** - Invalid user ID format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Field validation failed",
                    value = """
                        {
                          "error": "Validation failed",
                          "message": "Invalid input data",
                          "details": [
                            {
                              "field": "id",
                              "message": "ID must be a positive number"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "🚨 **Internal Server Error** - Database or server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Internal server error occurred",
                    value = """
                        {
                          "error": "Internal server error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-20T10:30:00Z",
                          "requestId": "req-123456789"
                        }
                        """
                )
            )
        )
    })
    public Response getUserById(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    @POST
    @Operation(
        summary = "🎯 Create New User",
        description = """
            ## 🎯 **Create New User**
            
            Creates a new user in the system with comprehensive validation and security features.
            
            ### ✅ **Features:**
            - **Input Validation**: Validates all fields using Bean Validation
            - **Password Security**: Automatically hashes passwords using bcrypt
            - **Uniqueness Check**: Ensures username and email are unique
            - **Automatic Timestamps**: Sets createdAt and updatedAt automatically
            - **Response Headers**: Includes monitoring information in headers
            
            ### 📋 **Validation Rules:**
            - **Username**: 3-50 characters, alphanumeric with underscores
            - **Email**: Valid email format, unique in system
            - **Full Name**: 2-100 characters, required
            - **Password**: 8-100 characters, strong password recommended
            
            ### 🔄 **Process Flow:**
            1. Validate input data using Bean Validation
            2. Check username and email uniqueness
            3. Hash password using bcrypt
            4. Save user to database
            5. Return created user without password
            6. Add monitoring headers to response
            """
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "201",
            description = "🎉 **Created** - User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(
                    name = "User Response",
                    summary = "Complete user information response",
                    value = """
                        {
                          "id": 123,
                          "username": "john_doe",
                          "email": "john@example.com",
                          "fullName": "John Doe",
                          "createdAt": "2024-01-20T10:30:00Z",
                          "updatedAt": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "❌ **Bad Request** - Invalid input data or validation errors",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Field validation failed",
                    value = """
                        {
                          "error": "Validation failed",
                          "message": "Invalid input data",
                          "details": [
                            {
                              "field": "username",
                              "message": "Username must be between 3 and 50 characters"
                            },
                            {
                              "field": "email",
                              "message": "Email format is invalid"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "409",
            description = "⚠️ **Conflict** - Username or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Duplicate User",
                    summary = "Username or email already exists",
                    value = """
                        {
                          "error": "Conflict",
                          "message": "Username 'john_doe' already exists",
                          "field": "username",
                          "timestamp": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "🚨 **Internal Server Error** - Database or server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Internal server error occurred",
                    value = """
                        {
                          "error": "Internal server error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-20T10:30:00Z",
                          "requestId": "req-123456789"
                        }
                        """
                )
            )
        )
    })
    public Response createUser(@Valid UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PUT
    @Path("/{id}")
    @Operation(
        summary = "✏️ Update User",
        description = """
            ## ✏️ **Update User**
            
            Updates an existing user with new information. Supports partial updates.
            
            ### ✅ **Features:**
            - **Partial Updates**: Only provided fields are updated
            - **Validation**: All updates validated using Bean Validation
            - **Password Security**: Passwords hashed if provided
            - **Uniqueness**: Username/email uniqueness maintained
            - **Timestamps**: UpdatedAt timestamp automatically updated
            
            ### 🔄 **Update Process:**
            1. Validate user exists
            2. Validate provided fields
            3. Check uniqueness constraints
            4. Hash password if provided
            5. Update only changed fields
            6. Return updated user information
            """
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "✅ **Success** - User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(
                    name = "User Response",
                    summary = "Complete user information response",
                    value = """
                        {
                          "id": 123,
                          "username": "john_doe",
                          "email": "john@example.com",
                          "fullName": "John Doe",
                          "createdAt": "2024-01-20T10:30:00Z",
                          "updatedAt": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "❌ **Bad Request** - Invalid input data or validation errors",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Field validation failed",
                    value = """
                        {
                          "error": "Validation failed",
                          "message": "Invalid input data",
                          "details": [
                            {
                              "field": "username",
                              "message": "Username must be between 3 and 50 characters"
                            },
                            {
                              "field": "email",
                              "message": "Email format is invalid"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "🔍 **Not Found** - User with specified ID not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Resource Not Found",
                    summary = "Resource with specified ID not found",
                    value = """
                        {
                          "error": "Resource not found",
                          "message": "User with ID 999 not found",
                          "timestamp": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "409",
            description = "⚠️ **Conflict** - Username or email already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Duplicate User",
                    summary = "Username or email already exists during update",
                    value = """
                        {
                          "error": "Conflict",
                          "message": "Email 'john@example.com' already exists",
                          "field": "email",
                          "timestamp": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "🚨 **Internal Server Error** - Database or server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Internal server error occurred",
                    value = """
                        {
                          "error": "Internal server error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-20T10:30:00Z",
                          "requestId": "req-123456789"
                        }
                        """
                )
            )
        )
    })
    public Response updateUser(@PathParam("id") Long id, @Valid UserRequest userRequest) {
        return userService.replaceUser(id, userRequest);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "🗑️ Delete User",
        description = """
            ## 🗑️ **Delete User**
            
            Permanently removes a user from the system.
            
            ### ✅ **Features:**
            - **Safe Deletion**: Verifies user exists before deletion
            - **Cascading**: Handles related data cleanup
            - **Monitoring**: Deletion tracked in audit logs
            - **Response**: Clear success/error responses
            
            ### ⚠️ **Important Notes:**
            - This operation is **irreversible**
            - All user data will be permanently deleted
            - Related audit logs are preserved for compliance
            - Returns 404 if user doesn't exist
            """
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "204",
            description = "✅ **No Content** - User deleted successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "🔍 **Not Found** - User with specified ID not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Resource Not Found",
                    summary = "Resource with specified ID not found",
                    value = """
                        {
                          "error": "Resource not found",
                          "message": "User with ID 999 not found",
                          "timestamp": "2024-01-20T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "❌ **Bad Request** - Invalid user ID format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    summary = "Field validation failed",
                    value = """
                        {
                          "error": "Validation failed",
                          "message": "Invalid input data",
                          "details": [
                            {
                              "field": "id",
                              "message": "ID must be a positive number"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "🚨 **Internal Server Error** - Database or server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    summary = "Internal server error occurred",
                    value = """
                        {
                          "error": "Internal server error",
                          "message": "An unexpected error occurred",
                          "timestamp": "2024-01-20T10:30:00Z",
                          "requestId": "req-123456789"
                        }
                        """
                )
            )
        )
    })
    public Response deleteUser(@PathParam("id") Long id) {
        return userService.delete(id);
    }
} 