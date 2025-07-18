package com.lolmeida.peahdb.resource;

import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        return userService.getUserById(id);
    }

    @GET
    @Path("/search/{key}/{value}")
    public Response search(@PathParam("key") String key, @PathParam("value") String value) {
        return userService.search(key, value);
    }

    @POST
    public Response createUser(@Valid UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PATCH
    @Path("/{id}")
    public Response partialUpdateUser(@PathParam("id") Long id, @Valid UserPatchRequest patchRequest) {
        return userService.partialUpdateUser(id, patchRequest);
    }

    @PUT
    @Path("/{id}")
    public Response replaceUser(@PathParam("id") Long id, @Valid UserRequest userRequest) {
        return userService.replaceUser(id, userRequest);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        return userService.delete(id);
    }
} 