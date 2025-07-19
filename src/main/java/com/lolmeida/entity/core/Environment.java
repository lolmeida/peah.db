package com.lolmeida.entity.core;

import com.lolmeida.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_environments")
public class Environment extends BaseEntity {
    public String name;
    public String description;
    @Column(name = "is_active")
    public Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

}