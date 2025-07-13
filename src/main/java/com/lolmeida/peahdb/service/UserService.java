package com.lolmeida.peahdb.service;

import com.lolmeida.peahdb.dto.mapper.MapperService;
import com.lolmeida.peahdb.entity.User;
import com.lolmeida.peahdb.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;


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
    
    public Response getUserByUsername(String username) {
        return result(Response.Status.OK,userRepository.findByUsername(username));
    }
    
    public Response getUserByEmail(String email) {
        return result(Response.Status.OK,userRepository.findByEmail(email));
    }

    public Response search(String key, String value) {
            return result(Response.Status.OK,userRepository.search(key, value));
    }

    @Transactional
    public Response createOrUpdate(User entity) {
        userRepository.createOrUpdate(entity);
        return result(Response.Status.CREATED, mapper.toUserResponse(entity));
    }

    @Transactional
    public Response delete(long id) {
        if (userRepository.delete(id)) {
            return Response.ok().entity("User deleted successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


    private Response result (Response.Status status, Object entity) {
        return Response
                .status(status)
                .entity(entity)
                .build();
    }

}