package com.lolmeida.service;

import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public interface BaseService<R, E> {

    public List<E> getAll();
    
    public Optional<R> getById(Long id);

    public E create(R entity);
    
    public E update(R entity);
    
    public void delete(Long id);


    static Response result (Status status, Object entity) {
        return Response
                .status(status)
                .entity(entity)
                .build();
    }

    
}
