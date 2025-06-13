package com.mongodb.launcher;

public class AtlasClusterSpec extends ClusterSpec {
    
    private String projectId;
    private String instanceSize;
    private String region;
    private String cloudProvider;
    private boolean enableBackup;
    private ClusterTopology topology;
    
    public AtlasClusterSpec() {}
    
    public AtlasClusterSpec(String name, String mongoVersion) {
        super(name, mongoVersion);
        this.topology = ClusterTopology.REPLICA_SET;
        this.instanceSize = "M10";
        this.cloudProvider = "AWS";
        this.region = "US_EAST_1";
        this.enableBackup = true;
    }
    
    @Override
    public ClusterType getType() {
        return ClusterType.ATLAS;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getInstanceSize() {
        return instanceSize;
    }
    
    public void setInstanceSize(String instanceSize) {
        this.instanceSize = instanceSize;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getCloudProvider() {
        return cloudProvider;
    }
    
    public void setCloudProvider(String cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
    
    public boolean isEnableBackup() {
        return enableBackup;
    }
    
    public void setEnableBackup(boolean enableBackup) {
        this.enableBackup = enableBackup;
    }
    
    public ClusterTopology getTopology() {
        return topology;
    }
    
    public void setTopology(ClusterTopology topology) {
        this.topology = topology;
    }
    
    public enum ClusterTopology {
        REPLICA_SET, SHARDED
    }
}