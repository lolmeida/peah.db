package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.*;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Operations for user management")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(
        summary = "Get all users",
        description = "Retrieve a list of all users in the system with pagination support"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "Successfully retrieved users list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.ARRAY, implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Users List",
                        description = "Example of users list response",
                        value = """
                        [
                          {
                            "id": 1,
                            "username": "john_doe",
                            "email": "john.doe@email.com",
                            "createdAt": "2023-01-15T10:30:00",
                            "updatedAt": "2023-01-15T10:30:00"
                          },
                          {
                            "id": 2,
                            "username": "jane_smith",
                            "email": "jane.smith@email.com",
                            "createdAt": "2023-02-20T14:15:30",
                            "updatedAt": "2023-02-25T16:45:20"
                          }
                        ]
                        """
                    )
                }
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server Error",
                    value = """
                    {
                      "error": "Internal server error",
                      "message": "Unable to retrieve users"
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
        summary = "Get user by ID",
        description = "Retrieve a specific user by their unique identifier"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200", 
            description = "User found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = @ExampleObject(
                    name = "User Details",
                    description = "Example of user details response",
                    value = """
                    {
                      "id": 1,
                      "username": "john_doe",
                      "email": "john.doe@email.com",
                      "createdAt": "2023-01-15T10:30:00",
                      "updatedAt": "2023-01-15T10:30:00"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found",
                    value = """
                    {
                      "error": "User not found",
                      "message": "User with id 999 does not exist"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid user ID",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Bad Request",
                    value = """
                    {
                      "error": "Invalid ID",
                      "message": "User ID must be a positive number"
                    }
                    """
                )
            )
        )
    })
    public Response getUserById(
        @Parameter(
            description = "Unique identifier of the user to retrieve",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")
        )
        @PathParam("id") Long id
    ) {
        return userService.getUserById(id);
    }

    @GET
    @Path("/search/{key}/{value}")
    @Operation(
        summary = "Search users",
        description = "Search users by a specific field and value"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = SchemaType.ARRAY, implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = @ExampleObject(
                    name = "Search Results",
                    description = "Example of search results",
                    value = """
                    [
                      {
                        "id": 1,
                        "username": "john_doe",
                        "email": "john.doe@email.com",
                        "createdAt": "2023-01-15T10:30:00",
                        "updatedAt": "2023-01-15T10:30:00"
                      }
                    ]
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Bad Request",
                    value = """
                    {
                      "error": "Invalid search parameters",
                      "message": "Search key must be one of: username, email"
                    }
                    """
                )
            )
        )
    })
    public Response search(
        @Parameter(
            description = "Field to search by (username, email)",
            required = true,
            example = "username",
            schema = @Schema(type = SchemaType.STRING, enumeration = {"username", "email"})
        )
        @PathParam("key") String key,
        
        @Parameter(
            description = "Value to search for",
            required = true,
            example = "john_doe",
            schema = @Schema(type = SchemaType.STRING, minLength = 1)
        )
        @PathParam("value") String value
    ) {
        return userService.search(key, value);
    }

    @POST
    @Operation(
        summary = "Create new user",
        description = "Create a new user with the provided information"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = @ExampleObject(
                    name = "Created User",
                    description = "Example of created user response",
                    value = """
                    {
                      "id": 9,
                      "username": "new_user",
                      "email": "new.user@email.com",
                      "createdAt": "2025-07-18T10:30:00",
                      "updatedAt": "2025-07-18T10:30:00"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "error": "Validation failed",
                      "violations": [
                        {
                          "field": "username",
                          "message": "Username must be between 3 and 50 characters"
                        },
                        {
                          "field": "email",
                          "message": "Email must be a valid email address"
                        }
                      ]
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "409",
            description = "User already exists",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Conflict",
                    value = """
                    {
                      "error": "User already exists",
                      "message": "Username or email already taken"
                    }
                    """
                )
            )
        )
    })
    public Response createUser(
        @RequestBody(
            description = "User information to create",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserRequest.class),
                examples = @ExampleObject(
                    name = "New User",
                    description = "Example of user creation request",
                    value = """
                    {
                      "username": "new_user",
                      "email": "new.user@email.com",
                      "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                    }
                    """
                )
            )
        )
        @Valid UserRequest userRequest
    ) {
        return userService.createUser(userRequest);
    }

    @PATCH
    @Path("/{id}")
    @Operation(
        summary = "Partially update user",
        description = "Update specific fields of an existing user"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = @ExampleObject(
                    name = "Updated User",
                    description = "Example of updated user response",
                    value = """
                    {
                      "id": 1,
                      "username": "john_doe",
                      "email": "john.doe.updated@email.com",
                      "createdAt": "2023-01-15T10:30:00",
                      "updatedAt": "2025-07-18T10:30:00"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found",
                    value = """
                    {
                      "error": "User not found",
                      "message": "User with id 999 does not exist"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "error": "Validation failed",
                      "violations": [
                        {
                          "field": "email",
                          "message": "Email must be a valid email address"
                        }
                      ]
                    }
                    """
                )
            )
        )
    })
    public Response partialUpdateUser(
        @Parameter(
            description = "Unique identifier of the user to update",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")
        )
        @PathParam("id") Long id,
        
        @RequestBody(
            description = "User fields to update (only provided fields will be updated)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserPatchRequest.class),
                examples = @ExampleObject(
                    name = "Partial Update",
                    description = "Example of partial user update",
                    value = """
                    {
                      "email": "updated.email@email.com"
                    }
                    """
                )
            )
        )
        @Valid UserPatchRequest patchRequest
    ) {
        return userService.partialUpdateUser(id, patchRequest);
    }

    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Replace user",
        description = "Completely replace an existing user with new data"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User replaced successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.lolmeida.peahdb.dto.response.UserResponse.class),
                examples = @ExampleObject(
                    name = "Replaced User",
                    description = "Example of replaced user response",
                    value = """
                    {
                      "id": 1,
                      "username": "completely_new_user",
                      "email": "completely.new@email.com",
                      "createdAt": "2023-01-15T10:30:00",
                      "updatedAt": "2025-07-18T10:30:00"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found",
                    value = """
                    {
                      "error": "User not found",
                      "message": "User with id 999 does not exist"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                      "error": "Validation failed",
                      "violations": [
                        {
                          "field": "username",
                          "message": "Username must be between 3 and 50 characters"
                        }
                      ]
                    }
                    """
                )
            )
        )
    })
    public Response replaceUser(
        @Parameter(
            description = "Unique identifier of the user to replace",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")
        )
        @PathParam("id") Long id,
        
        @RequestBody(
            description = "Complete user information to replace existing data",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserRequest.class),
                examples = @ExampleObject(
                    name = "Replace User",
                    description = "Example of user replacement request",
                    value = """
                    {
                      "username": "completely_new_user",
                      "email": "completely.new@email.com",
                      "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
                    }
                    """
                )
            )
        )
        @Valid UserRequest userRequest
    ) {
        return userService.replaceUser(id, userRequest);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Delete an existing user by their unique identifier"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Not Found",
                    value = """
                    {
                      "error": "User not found",
                      "message": "User with id 999 does not exist"
                    }
                    """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid user ID",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Bad Request",
                    value = """
                    {
                      "error": "Invalid ID",
                      "message": "User ID must be a positive number"
                    }
                    """
                )
            )
        )
    })
    public Response deleteUser(
        @Parameter(
            description = "Unique identifier of the user to delete",
            required = true,
            example = "1",
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")
        )
        @PathParam("id") Long id
    ) {
        return userService.delete(id);
    }
} 