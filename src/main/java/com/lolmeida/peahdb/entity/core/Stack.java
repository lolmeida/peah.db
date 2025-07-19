package com.lolmeida.peahdb.entity.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.lolmeida.peahdb.entity.k8s.App;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "config_stacks")
public class Stack extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "environment_id")
    public Environment environment;

    public String name;
    public Boolean enabled = false;
    public String description;

    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode config;

    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stack", cascade = CascadeType.ALL)
    public List<App> apps = new ArrayList<>();
}