package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.entity.User;
import com.lolmeida.peahdb.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class UserService {
    
    @Inject
    UserRepository userRepository;

    @Inject
    MapperService mapper;
    
    public Response getAllUsers() {
        return result(Response.Status.OK,userRepository.listAll());
    }
    
    public Response getUserById(Long id) {
        return result(Response.Status.OK,userRepository.findByIdOptional(id));
    }

    public Response search(String key, String value) {
            return result(Response.Status.OK,userRepository.search(key, value));
    }

    /**
     * POST /users - Create a new user
     * Server generates the ID
     */
    @Transactional
    public Response createUser(UserRequest userRequest) {
        // Check for unique constraint violations
        if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), null)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username or email already exists")
                    .build();
        }

        User entity = mapper.toUser(userRequest);
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        
        userRepository.createOrUpdate(entity);
        return result(Response.Status.CREATED, mapper.toUserResponse(entity));
    }

    /**
     * PUT /users/{id} - Replace the entire user resource
     * All fields must be provided, missing fields will be set to null/default
     */
    @Transactional
    public Response replaceUser(Long id, UserRequest userRequest) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User ID cannot be null")
                    .build();
        }

        Optional<User> existingUser = userRepository.findByIdOptional(id);
        if (existingUser.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found")
                    .build();
        }

        if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username or email already exists")
                    .build();
        }

        User entity = mapper.toUserWithId(userRequest, id);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedAt(existingUser.get().getCreatedAt()); // Keep original createdAt
        
        userRepository.createOrUpdate(entity);
        return result(Response.Status.OK, mapper.toUserResponse(entity));
    }

    /**
     * PATCH /users/{id} - Partially update a user
     * Only provided fields will be updated, others remain unchanged
     */
    @Transactional
    public Response partialUpdateUser(Long id, UserPatchRequest patchRequest) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User ID cannot be null")
                    .build();
        }

        Optional<User> existingUserOpt = userRepository.findByIdOptional(id);
        if (existingUserOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found")
                    .build();
        }

        User existingUser = existingUserOpt.get();

        String newUsername = patchRequest.getUsername() != null ? patchRequest.getUsername() : existingUser.getUsername();
        String newEmail = patchRequest.getEmail() != null ? patchRequest.getEmail() : existingUser.getEmail();
        
        if (isUsernameOrEmailTaken(newUsername, newEmail, id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username or email already exists")
                    .build();
        }

        mapper.updateUserFromPatch(patchRequest, existingUser);
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.createOrUpdate(existingUser);
        return result(Response.Status.OK, mapper.toUserResponse(existingUser));
    }

    /**
     * PUT /users/{id} - Create or update a user (upsert)
     * This is the idempotent version that can create OR update
     * @deprecated This method is kept for backwards compatibility but not exposed via REST
     */
    @Deprecated
    @Transactional
    public Response createOrUpdateUser(Long id, UserRequest userRequest) {
        Optional<User> existingUser = userRepository.findByIdOptional(id);
        boolean isCreating = existingUser.isEmpty();

        if (isCreating) {
            if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), null)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            User entity = mapper.toUserWithId(userRequest, id);
            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            
            userRepository.createOrUpdate(entity);
            return result(Response.Status.CREATED, mapper.toUserResponse(entity));
        } else {
            if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), id)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            User entity = mapper.toUserWithId(userRequest, id);
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setCreatedAt(existingUser.get().getCreatedAt()); // Keep original createdAt
            
            userRepository.createOrUpdate(entity);
            return result(Response.Status.OK, mapper.toUserResponse(entity));
        }
    }

    @Transactional
    public Response delete(long id) {
        if (userRepository.delete(id)) {
            return Response.ok().entity("User deleted successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private boolean isUsernameOrEmailTaken(String username, String email, Long excludeId) {
        Optional<User> userByUsername = userRepository.findByUsername(username);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(excludeId)) {
            return true;
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        return userByEmail.isPresent() && !userByEmail.get().getId().equals(excludeId);
    }

    private Response result (Response.Status status, Object entity) {
        return Response
                .status(status)
                .entity(entity)
                .build();
    }
}