package com.lolmeida.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

/**
 * Base Entity for all entities in the application
 * Provides consistent ID generation strategy for MySQL using AUTO_INCREMENT
 * instead of sequence tables
 */
@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;
    
}