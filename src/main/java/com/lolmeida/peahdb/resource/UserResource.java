package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.entity.User;
import com.lolmeida.peahdb.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    public Response getAllUsers() {
        return Response.ok()
                .entity(userService.getAllUsers())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getUserById( Long id) {
        return Response.ok()
                .entity(userService.getUserById(id))
                .build();
    }

    @GET
    @Path("/{key}/{value}")
    public Response search( String key, String value) {
        return userService.search(key, value);
    }

    @DELETE
    @Path("/{id}")
    public  Response delete(Long id) {
        return userService.delete(id);
    }

    @PUT
    public  Response createOrUpdate(User entity) {
        return userService.createOrUpdate(entity);
    }


} 