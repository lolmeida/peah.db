package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;
import com.lolmeida.peahdb.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MapperService mapperService;

    private User testUser;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        testUser = createEntity();
        userRequest = createRequest();
        userResponse = createResponse();
    }

    @Nested
    class GetAllUsersTest {
        @Test
        @DisplayName("Should return list of users")
        void testGetAllUsersSuccess() {
            List<User> users = Collections.singletonList(testUser);
            when(userRepository.listAll()).thenReturn(users);

            Response response = userService.getAllUsers();

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(users, response.getEntity());
            verify(userRepository).listAll();
        }
    }

    @Nested
    class GetUserByIdTest {
        @Test
        @DisplayName("Should return user when found")
        void testGetUserByIdSuccess() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));

            Response response = userService.getUserById(userId);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(Optional.of(testUser), response.getEntity());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).findByIdOptional(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
        }

        @Test
        @DisplayName("Should return empty optional when user not found")
        void testGetUserByIdNotFound() {
            Long userId = 999L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            Response response = userService.getUserById(userId);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(Optional.empty(), response.getEntity());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).findByIdOptional(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
        }
    }

    @Nested
    class SearchTest {
        @Test
        @DisplayName("Should return search results")
        void testSearchSuccess() {
            String field = "username";
            String value = "john";
            when(userRepository.search(field, value)).thenReturn(Optional.of(testUser));

            Response response = userService.search(field, value);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(Optional.of(testUser), response.getEntity());
            
            ArgumentCaptor<String> fieldCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(userRepository).search(fieldCaptor.capture(), valueCaptor.capture());
            
            assertEquals(field, fieldCaptor.getValue());
            assertEquals(value, valueCaptor.getValue());
        }
    }

    @Nested
    class CreateUserTest {
        @Test
        @DisplayName("Should create user successfully")
        void testCreateUserSuccess() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.empty());
            when(mapperService.toUser(userRequest)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.createUser(userRequest);

            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).createOrUpdate(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getCreatedAt());
            assertNotNull(savedUser.getUpdatedAt());
            
            ArgumentCaptor<UserRequest> requestCaptor = ArgumentCaptor.forClass(UserRequest.class);
            verify(mapperService).toUser(requestCaptor.capture());
            
            assertEquals(userRequest.getUsername(), requestCaptor.getValue().getUsername());
            assertEquals(userRequest.getEmail(), requestCaptor.getValue().getEmail());
        }

        @Test
        @DisplayName("Should return CONFLICT when username already exists")
        void testCreateUserWithExistingUsername() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

            Response response = userService.createUser(userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            verify(userRepository).findByUsername(usernameCaptor.capture());
            
            assertEquals("john_doe", usernameCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when email already exists")
        void testCreateUserWithExistingEmail() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));

            Response response = userService.createUser(userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            verify(userRepository).findByEmail(emailCaptor.capture());
            
            assertEquals("john.doe@email.com", emailCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }
    }

    @Nested
    class ReplaceUserTest {
        @Test
        @DisplayName("Should replace user successfully")
        void testReplaceUserSuccess() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserWithId(userRequest, userId)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.replaceUser(userId, userRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).createOrUpdate(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getUpdatedAt());
            assertEquals(testUser.getCreatedAt(), savedUser.getCreatedAt());
            
            ArgumentCaptor<UserRequest> requestCaptor = ArgumentCaptor.forClass(UserRequest.class);
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(mapperService).toUserWithId(requestCaptor.capture(), idCaptor.capture());
            
            assertEquals(userRequest.getUsername(), requestCaptor.getValue().getUsername());
            assertEquals(userId, idCaptor.getValue());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when user ID is null")
        void testReplaceUserWithNullId() {
            Response response = userService.replaceUser(null, userRequest);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("User ID cannot be null", response.getEntity());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void testReplaceUserNotFound() {
            Long userId = 999L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            Response response = userService.replaceUser(userId, userRequest);

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("User with id 999 not found", response.getEntity());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).findByIdOptional(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when username already exists")
        void testReplaceUserWithConflictingUsername() {
            Long userId = 1L;
            User otherUser = User.builder()
                    .id(2L)
                    .username("john_doe")
                    .email("other@email.com")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(otherUser));

            Response response = userService.replaceUser(userId, userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            verify(userRepository).findByUsername(usernameCaptor.capture());
            
            assertEquals("john_doe", usernameCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }
    }

    @Nested
    class PartialUpdateUserTest {
        @Test
        @DisplayName("Should update user partially")
        void testPartialUpdateUserSuccess() {
            Long userId = 1L;
            UserPatchRequest patchRequest = UserPatchRequest.builder()
                    .username("new_username")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("new_username")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<UserPatchRequest> patchCaptor = ArgumentCaptor.forClass(UserPatchRequest.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(mapperService).updateUserFromPatch(patchCaptor.capture(), userCaptor.capture());

            assertEquals(patchRequest.getUsername(), patchCaptor.getValue().getUsername());
            assertEquals(testUser.getId(), userCaptor.getValue().getId());

            ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).createOrUpdate(savedUserCaptor.capture());

            User savedUser = savedUserCaptor.getValue();
            assertNotNull(savedUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return BAD_REQUEST when user ID is null")
        void testPartialUpdateUserWithNullId() {
            UserPatchRequest patchRequest = UserPatchRequest.builder().build();

            Response response = userService.partialUpdateUser(null, patchRequest);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("User ID cannot be null", response.getEntity());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void testPartialUpdateUserNotFound() {
            Long userId = 999L;
            UserPatchRequest patchRequest = UserPatchRequest.builder().build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            assertEquals("User with id 999 not found", response.getEntity());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).findByIdOptional(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when email already exists")
        void testPartialUpdateUserWithConflictingEmail() {
            Long userId = 1L;
            UserPatchRequest patchRequest = UserPatchRequest.builder()
                    .email("conflict@email.com")
                    .build();

            User otherUser = User.builder()
                    .id(2L)
                    .username("other_user")
                    .email("conflict@email.com")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("conflict@email.com")).thenReturn(Optional.of(otherUser));

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            verify(userRepository).findByEmail(emailCaptor.capture());
            
            assertEquals("conflict@email.com", emailCaptor.getValue());
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should keep existing values when patch request has null fields")
        void testPartialUpdateUserWithNullFields() {
            Long userId = 1L;
            UserPatchRequest patchRequest = UserPatchRequest.builder()
                    .username(null)
                    .email(null)
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<UserPatchRequest> patchCaptor = ArgumentCaptor.forClass(UserPatchRequest.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(mapperService).updateUserFromPatch(patchCaptor.capture(), userCaptor.capture());

            assertEquals(patchRequest.getUsername(), patchCaptor.getValue().getUsername());
            assertEquals(patchRequest.getEmail(), patchCaptor.getValue().getEmail());
            assertEquals(testUser.getId(), userCaptor.getValue().getId());
        }

        @Test
        @DisplayName("Should update only email when username is null")
        void testPartialUpdateUserWithNullUsername() {
            Long userId = 1L;
            UserPatchRequest patchRequest = UserPatchRequest.builder()
                    .username(null)
                    .email("new_email@test.com")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("new_email@test.com")).thenReturn(Optional.empty());
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<UserPatchRequest> patchCaptor = ArgumentCaptor.forClass(UserPatchRequest.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(mapperService).updateUserFromPatch(patchCaptor.capture(), userCaptor.capture());

            assertNull(patchCaptor.getValue().getUsername());
            assertEquals("new_email@test.com", patchCaptor.getValue().getEmail());
        }

        @Test
        @DisplayName("Should update only username when email is null")
        void testPartialUpdateUserWithNullEmail() {
            Long userId = 1L;
            UserPatchRequest patchRequest = UserPatchRequest.builder()
                    .username("new_username")
                    .email(null)
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("new_username")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.partialUpdateUser(userId, patchRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<UserPatchRequest> patchCaptor = ArgumentCaptor.forClass(UserPatchRequest.class);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(mapperService).updateUserFromPatch(patchCaptor.capture(), userCaptor.capture());

            assertEquals("new_username", patchCaptor.getValue().getUsername());
            assertNull(patchCaptor.getValue().getEmail());
        }
    }

    @Nested
    class DeleteUserTest {
        @Test
        @DisplayName("Should delete user successfully")
        void testDeleteUserSuccess() {
            Long userId = 1L;
            when(userRepository.delete(userId)).thenReturn(true);

            Response response = userService.delete(userId);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals("User deleted successfully", response.getEntity());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).delete(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
        }

        @Test
        @DisplayName("Should return NOT_FOUND when user does not exist")
        void testDeleteUserNotFound() {
            Long userId = 999L;
            when(userRepository.delete(userId)).thenReturn(false);

            Response response = userService.delete(userId);

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(userRepository).delete(idCaptor.capture());
            
            assertEquals(userId, idCaptor.getValue());
        }
    }

    @Nested
    class CreateOrUpdateUserTest {
        @Test
        @DisplayName("Should create new user when not exists")
        void testCreateOrUpdateUserCreate() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.empty());
            when(mapperService.toUserWithId(userRequest, userId)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).createOrUpdate(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getCreatedAt());
            assertNotNull(savedUser.getUpdatedAt());
            
            ArgumentCaptor<UserRequest> requestCaptor = ArgumentCaptor.forClass(UserRequest.class);
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(mapperService).toUserWithId(requestCaptor.capture(), idCaptor.capture());
            
            assertEquals(userRequest.getUsername(), requestCaptor.getValue().getUsername());
            assertEquals(userId, idCaptor.getValue());
        }

        @Test
        @DisplayName("Should update existing user")
        void testCreateOrUpdateUserUpdate() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserWithId(userRequest, userId)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).createOrUpdate(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNotNull(savedUser.getUpdatedAt());
            assertEquals(testUser.getCreatedAt(), savedUser.getCreatedAt());
            
            ArgumentCaptor<UserRequest> requestCaptor = ArgumentCaptor.forClass(UserRequest.class);
            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            verify(mapperService).toUserWithId(requestCaptor.capture(), idCaptor.capture());
            
            assertEquals(userRequest.getUsername(), requestCaptor.getValue().getUsername());
            assertEquals(userId, idCaptor.getValue());
        }

        @Test
        @DisplayName("Should return CONFLICT when creating user with existing username")
        void testCreateOrUpdateUserCreateConflictUsername() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty()); // isCreating = true
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            verify(userRepository).findByIdOptional(userId);
            verify(userRepository).findByUsername("john_doe");
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when creating user with existing email")
        void testCreateOrUpdateUserCreateConflictEmail() {
            Long userId = 1L;
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty()); // isCreating = true
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            verify(userRepository).findByIdOptional(userId);
            verify(userRepository).findByUsername("john_doe");
            verify(userRepository).findByEmail("john.doe@email.com");
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when updating user with conflicting username")
        void testCreateOrUpdateUserUpdateConflictUsername() {
            Long userId = 1L;
            User otherUser = User.builder()
                    .id(2L)
                    .username("john_doe")
                    .email("other@email.com")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser)); // isCreating = false
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(otherUser));

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            verify(userRepository).findByIdOptional(userId);
            verify(userRepository).findByUsername("john_doe");
            verify(userRepository, never()).createOrUpdate(any());
        }

        @Test
        @DisplayName("Should return CONFLICT when updating user with conflicting email")
        void testCreateOrUpdateUserUpdateConflictEmail() {
            Long userId = 1L;
            User otherUser = User.builder()
                    .id(2L)
                    .username("other_user")
                    .email("john.doe@email.com")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser)); // isCreating = false
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(otherUser));

            Response response = userService.createOrUpdateUser(userId, userRequest);

            assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
            assertEquals("Username or email already exists", response.getEntity());
            
            verify(userRepository).findByIdOptional(userId);
            verify(userRepository).findByUsername("john_doe");
            verify(userRepository).findByEmail("john.doe@email.com");
            verify(userRepository, never()).createOrUpdate(any());
        }
    }

    @Nested
    class IsUsernameOrEmailTakenTest {
        @Test
        @DisplayName("Should return false when username and email are not taken")
        void testIsUsernameOrEmailTakenReturnsFalse() {
            Long userId = 1L;
            UserRequest newUserRequest = UserRequest.builder()
                    .username("unique_username")
                    .email("unique@email.com")
                    .passwordHash("$2a$10$hashedpassword")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("unique_username")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("unique@email.com")).thenReturn(Optional.empty());
            when(mapperService.toUserWithId(newUserRequest, userId)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.replaceUser(userId, newUserRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            verify(userRepository).findByUsername("unique_username");
            verify(userRepository).findByEmail("unique@email.com");
            verify(userRepository).createOrUpdate(any(User.class));
        }

        @Test
        @DisplayName("Should return false when username and email belong to same user being updated")
        void testIsUsernameOrEmailTakenForSameUser() {
            Long userId = 1L;
            UserRequest sameUserRequest = UserRequest.builder()
                    .username("john_doe")
                    .email("john.doe@email.com")
                    .passwordHash("$2a$10$hashedpassword")
                    .build();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
            when(mapperService.toUserWithId(sameUserRequest, userId)).thenReturn(testUser);
            when(mapperService.toUserResponse(testUser)).thenReturn(userResponse);

            Response response = userService.replaceUser(userId, sameUserRequest);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(userResponse, response.getEntity());

            verify(userRepository).findByUsername("john_doe");
            verify(userRepository).findByEmail("john.doe@email.com");
            verify(userRepository).createOrUpdate(any(User.class));
        }
    }

    // Helper methods
    private UserResponse createResponse() {
        return UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@email.com")
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
    }

    private static UserRequest createRequest() {
        return UserRequest.builder()
                .username("john_doe")
                .email("john.doe@email.com")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    private static User createEntity() {
        return User.builder()
                .id(1L)
                .username("john_doe")
                .email("john.doe@email.com")
                .passwordHash("$2a$10$hashedpassword")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}