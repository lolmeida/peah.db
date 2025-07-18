package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;
import com.lolmeida.peahdb.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public Response createOrUpdate(User entity) {
        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User entity cannot be null")
                    .build();
        }

        // Validate required fields
        if (entity.getUsername() == null || entity.getUsername().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Username is required")
                    .build();
        }

        if (entity.getEmail() == null || entity.getEmail().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Email is required")
                    .build();
        }

        boolean isUpdate = entity.getId() != null;
        
        if (isUpdate) {
            // Update existing user
            Optional<User> existingUser = userRepository.findByIdOptional(entity.getId());
            if (existingUser.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("User with id " + entity.getId() + " not found")
                        .build();
            }

            // Check for unique constraint violations (excluding current user)
            if (isUsernameOrEmailTaken(entity.getUsername(), entity.getEmail(), entity.getId())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            entity.setUpdatedAt(LocalDateTime.now());
            // Keep original createdAt
            entity.setCreatedAt(existingUser.get().getCreatedAt());
            
            userRepository.createOrUpdate(entity);
            return result(Response.Status.OK, mapper.toUserResponse(entity));
        } else {
            // Create new user
            if (isUsernameOrEmailTaken(entity.getUsername(), entity.getEmail(), null)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Username or email already exists")
                        .build();
            }

            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            
            userRepository.createOrUpdate(entity);
            return result(Response.Status.CREATED, mapper.toUserResponse(entity));
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
        // Check username
        Optional<User> userByUsername = userRepository.findByUsername(username);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(excludeId)) {
            return true;
        }

        // Check email
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(excludeId)) {
            return true;
        }

        return false;
    }

    private Response result (Response.Status status, Object entity) {
        return Response
                .status(status)
                .entity(entity)
                .build();
    }
}