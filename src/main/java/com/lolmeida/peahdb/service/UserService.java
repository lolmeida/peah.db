package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;

@ApplicationScoped
public class UserService {
    
    @Inject
    UserRepository userRepository;

    @Inject
    MapperService mapper;
    
    public Response getAllUsers() {
        return BaseService.result(Response.Status.OK,userRepository.listAll());
    }
    
    public Response getUserById(Long id) {
        return BaseService.result(Response.Status.OK,userRepository.findByIdOptional(id));
    }

    public Response search(String key, String value) {
            return BaseService.result(Response.Status.OK,userRepository.search(key, value));
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

        
        LocalDateTime now = LocalDateTime.now();
        mapper.toUser(userRequest).setCreatedAt(now);
        mapper.toUser(userRequest).setUpdatedAt(now);
        
        userRepository.createOrUpdate(mapper.toUser(userRequest));
        return BaseService.result(Response.Status.CREATED, mapper.toUserResponse(mapper.toUser(userRequest)));
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

        if (userRepository.findByIdOptional(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found")
                    .build();
        }

        if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username or email already exists")
                    .build();
        }

        mapper.toUserWithId(userRequest, id).setUpdatedAt(LocalDateTime.now());
        mapper.toUserWithId(userRequest, id).setCreatedAt(userRepository.findByIdOptional(id).get().getCreatedAt()); // Keep original createdAt
        
        userRepository.createOrUpdate(mapper.toUserWithId(userRequest, id));
        return BaseService.result(Response.Status.OK, mapper.toUserResponse(mapper.toUserWithId(userRequest, id)));
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

        if (userRepository.findByIdOptional(id).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User with id " + id + " not found")
                    .build();
        }


        String newUsername = patchRequest.getUsername() != null ? patchRequest.getUsername() : userRepository.findByIdOptional(id).get().getUsername();
        String newEmail = patchRequest.getEmail() != null ? patchRequest.getEmail() : userRepository.findByIdOptional(id).get().getEmail();
        
        if (isUsernameOrEmailTaken(newUsername, newEmail, id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Username or email already exists")
                    .build();
        }

        mapper.updateUserFromPatch(patchRequest, userRepository.findByIdOptional(id).get());
        
        userRepository.findByIdOptional(id).get().setUpdatedAt(LocalDateTime.now());
        
        userRepository.createOrUpdate(userRepository.findByIdOptional(id).get());
        return BaseService.result(Response.Status.OK, mapper.toUserResponse(userRepository.findByIdOptional(id).get()));
    }

    /**
     * PUT /users/{id} - Create or update a user (upsert)
     * This is the idempotent version that can create OR update
     * @deprecated This method is kept for backwards compatibility but not exposed via REST
     */
    @Deprecated
    @Transactional
    public Response createOrUpdateUser(Long id, UserRequest userRequest) {
        boolean isCreating = userRepository.findByIdOptional(id).isEmpty();

        if (isCreating) {
            if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), null)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            LocalDateTime now = LocalDateTime.now();
            mapper.toUserWithId(userRequest, id).setCreatedAt(now);
            mapper.toUserWithId(userRequest, id).setUpdatedAt(now);
            
            userRepository.createOrUpdate(mapper.toUserWithId(userRequest, id));
            return BaseService.result(Response.Status.CREATED, mapper.toUserResponse(mapper.toUserWithId(userRequest, id)));
        } else {
            if (isUsernameOrEmailTaken(userRequest.getUsername(), userRequest.getEmail(), id)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            mapper.toUserWithId(userRequest, id).setUpdatedAt(LocalDateTime.now());
            mapper.toUserWithId(userRequest, id).setCreatedAt(userRepository.findByIdOptional(id).get().getCreatedAt()); // Keep original createdAt
            
            userRepository.createOrUpdate(mapper.toUserWithId(userRequest, id));
            return BaseService.result(Response.Status.OK, mapper.toUserResponse(mapper.toUserWithId(userRequest, id)));
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
        if (userRepository.findByUsername(username).isPresent() && !userRepository.findByUsername(username).get().getId().equals(excludeId)) {
            return true;
        }

        return userRepository.findByEmail(email).isPresent() && !userRepository.findByEmail(email).get().getId().equals(excludeId);
    }

}