package com.mongodb.launcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AtlasClusterSpec.class, name = "atlas"),
    @JsonSubTypes.Type(value = LocalClusterSpec.class, name = "local")
})
public abstract class ClusterSpec {
    
    private String name;
    private String mongoVersion;
    
    public ClusterSpec() {}
    
    public ClusterSpec(String name, String mongoVersion) {
        this.name = name;
        this.mongoVersion = mongoVersion;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getMongoVersion() {
        return mongoVersion;
    }
    
    public void setMongoVersion(String mongoVersion) {
        this.mongoVersion = mongoVersion;
    }
    
    public abstract ClusterType getType();
    
    public enum ClusterType {
        ATLAS, LOCAL
    }
}