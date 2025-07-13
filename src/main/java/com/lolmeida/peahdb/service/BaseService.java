package com.lolmeida.peahdb.service;

import java.util.List;
import java.util.Optional;

public interface BaseService<R, E> {

    public List<E> getAll();
    
    public Optional<R> getById(Long id);

    public E create(R entity);
    
    public E update(R entity);
    
    public void delete(Long id);

    
}
