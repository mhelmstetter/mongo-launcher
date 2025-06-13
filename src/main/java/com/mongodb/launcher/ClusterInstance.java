package com.mongodb.launcher;

import java.time.LocalDateTime;
import java.util.Map;

public class ClusterInstance {
    
    private String id;
    private String name;
    private ClusterSpec spec;
    private String connectionString;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private Map<String, Object> metadata;
    
    public ClusterInstance() {}
    
    public ClusterInstance(String id, String name, ClusterSpec spec) {
        this.id = id;
        this.name = name;
        this.spec = spec;
        this.status = Status.CREATING;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ClusterSpec getSpec() {
        return spec;
    }
    
    public void setSpec(ClusterSpec spec) {
        this.spec = spec;
    }
    
    public String getConnectionString() {
        return connectionString;
    }
    
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public enum Status {
        CREATING, READY, STARTING, STOPPING, STOPPED, ERROR, DESTROYED
    }
}