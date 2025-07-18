package com.lolmeida.peahdb.repository;

import com.lolmeida.peahdb.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> search(String key, String value) {
        return find(key, value).firstResultOptional();
    }
    
    public boolean existsByUsername(String username) {
        return find("username", username).count() > 0;
    }
    
    public boolean existsByEmail(String email) {
        return find("email", email).count() > 0;
    }

    public Optional<User> findByIdOptional(Long id) {
        return find("id", id).firstResultOptional();
    }

    public boolean delete (long id) {
        return deleteById(id);
    }

    public void createOrUpdate (User entity){
         persistAndFlush(entity);
    }

} 