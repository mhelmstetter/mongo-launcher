package com.mongodb.launcher;

import java.util.ArrayList;
import java.util.List;

public class LocalClusterSpec extends ClusterSpec {
    
    private int port = 27017;
    private String dataPath;
    private String logPath;
    private LocalTopology topology = LocalTopology.STANDALONE;
    private int replicaSetSize = 3;
    private int shardCount = 2;
    private boolean enableAuth = false;
    private String authUser;
    private String authPassword;
    private List<String> additionalOptions = new ArrayList<>();
    
    public LocalClusterSpec() {}
    
    public LocalClusterSpec(String name, String mongoVersion) {
        super(name, mongoVersion);
    }
    
    @Override
    public ClusterType getType() {
        return ClusterType.LOCAL;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDataPath() {
        return dataPath;
    }
    
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
    
    public String getLogPath() {
        return logPath;
    }
    
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    
    public LocalTopology getTopology() {
        return topology;
    }
    
    public void setTopology(LocalTopology topology) {
        this.topology = topology;
    }
    
    public int getReplicaSetSize() {
        return replicaSetSize;
    }
    
    public void setReplicaSetSize(int replicaSetSize) {
        this.replicaSetSize = replicaSetSize;
    }
    
    public int getShardCount() {
        return shardCount;
    }
    
    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }
    
    public boolean isEnableAuth() {
        return enableAuth;
    }
    
    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }
    
    public String getAuthUser() {
        return authUser;
    }
    
    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }
    
    public String getAuthPassword() {
        return authPassword;
    }
    
    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }
    
    public List<String> getAdditionalOptions() {
        return additionalOptions;
    }
    
    public void setAdditionalOptions(List<String> additionalOptions) {
        this.additionalOptions = additionalOptions;
    }
    
    public enum LocalTopology {
        STANDALONE, REPLICA_SET, SHARDED
    }
}