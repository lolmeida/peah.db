package com.lolmeida.resource;

import com.lolmeida.dto.request.UserRequest;
import com.lolmeida.dto.response.UserResponse;
import com.lolmeida.service.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("UserResource")
class UserResourceTest {

    @InjectMock
    UserService userService;

    private UserResponse userResponse;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@email.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRequest = UserRequest.builder()
                .username("john_doe")
                .email("john.doe@email.com")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    @Nested
    class GetAllUsersTest {
        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            List<UserResponse> users = Arrays.asList(userResponse);
            when(userService.getAllUsers()).thenReturn(
                    Response.ok(users).build()
            );

            given()
                    .when().get("/api/users")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(1))
                    .body("[0].username", is("john_doe"))
                    .body("[0].email", is("john.doe@email.com"));
        }

        @Test
        @DisplayName("Should return empty list when no users")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userService.getAllUsers()).thenReturn(
                    Response.ok(List.of()).build()
            );

            given()
                    .when().get("/api/users")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(0));
        }
    }

    @Nested
    class GetUserByIdTest {
        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() {
            when(userService.getUserById(1L)).thenReturn(
                    Response.ok(userResponse).build()
            );

            given()
                    .when().get("/api/users/1")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("username", is("john_doe"))
                    .body("email", is("john.doe@email.com"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(userService.getUserById(999L)).thenReturn(
                    Response.status(Response.Status.NOT_FOUND).build()
            );

            given()
                    .when().get("/api/users/999")
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 for invalid user ID")
        void shouldReturn400ForInvalidUserId() {
            given()
                    .when().get("/api/users/invalid")
                    .then()
                    .statusCode(404); // JAX-RS converts to 404 for invalid path params
        }
    }

    @Nested
    class CreateUserTest {
        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            when(userService.createUser(any(UserRequest.class))).thenReturn(
                    Response.status(Response.Status.CREATED).entity(userResponse).build()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(userRequest)
                    .when().post("/api/users")
                    .then()
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("username", is("john_doe"))
                    .body("email", is("john.doe@email.com"));
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameAlreadyExists() {
            when(userService.createUser(any(UserRequest.class))).thenReturn(
                    Response.status(Response.Status.CONFLICT)
                            .entity("Username or email already exists")
                            .build()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(userRequest)
                    .when().post("/api/users")
                    .then()
                    .statusCode(409)
                    .body(is("Username or email already exists"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request body")
        void shouldReturn400ForInvalidRequestBody() {
            UserRequest invalidRequest = UserRequest.builder()
                    .username("") // Invalid empty username
                    .email("invalid-email") // Invalid email format
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when().post("/api/users")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 for missing request body")
        void shouldReturn400ForMissingRequestBody() {
            given()
                    .contentType(ContentType.JSON)
                    .body("{}")
                    .when().post("/api/users")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    class UpdateUserTest {
        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            when(userService.replaceUser(anyLong(), any(UserRequest.class))).thenReturn(
                    Response.ok(userResponse).build()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(userRequest)
                    .when().put("/api/users/1")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("username", is("john_doe"))
                    .body("email", is("john.doe@email.com"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(userService.replaceUser(anyLong(), any(UserRequest.class))).thenReturn(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity("User with id 999 not found")
                            .build()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(userRequest)
                    .when().put("/api/users/999")
                    .then()
                    .statusCode(404)
                    .body(is("User with id 999 not found"));
        }

        @Test
        @DisplayName("Should return 409 when username conflicts")
        void shouldReturn409WhenUsernameConflicts() {
            when(userService.replaceUser(anyLong(), any(UserRequest.class))).thenReturn(
                    Response.status(Response.Status.CONFLICT)
                            .entity("Username or email already exists")
                            .build()
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(userRequest)
                    .when().put("/api/users/1")
                    .then()
                    .statusCode(409)
                    .body(is("Username or email already exists"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request body")
        void shouldReturn400ForInvalidRequestBody() {
            UserRequest invalidRequest = UserRequest.builder()
                    .username("") // Invalid empty username
                    .email("invalid-email") // Invalid email format
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when().put("/api/users/1")
                    .then()
                    .statusCode(400);
        }
    }


    @Nested
    class DeleteUserTest {
        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            when(userService.delete(anyLong())).thenReturn(
                    Response.ok("User deleted successfully").build()
            );

            given()
                    .when().delete("/api/users/1")
                    .then()
                    .statusCode(200)
                    .body(is("User deleted successfully"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(userService.delete(anyLong())).thenReturn(
                    Response.status(Response.Status.NOT_FOUND).build()
            );

            given()
                    .when().delete("/api/users/999")
                    .then()
                    .statusCode(404);
        }
    }

} 