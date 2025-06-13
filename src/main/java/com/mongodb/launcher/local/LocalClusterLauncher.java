package com.mongodb.launcher.local;

import com.mongodb.launcher.*;
import com.mongodb.launcher.version.MongoVersion;
import com.mongodb.launcher.version.MongoVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalClusterLauncher implements ClusterLauncher<LocalClusterSpec> {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalClusterLauncher.class);
    private final MongoVersionManager versionManager;
    
    public LocalClusterLauncher() {
        this.versionManager = new MongoVersionManager();
    }
    
    public LocalClusterLauncher(MongoVersionManager versionManager) {
        this.versionManager = versionManager;
    }
    
    @Override
    public ClusterInstance launch(LocalClusterSpec spec) throws ClusterLaunchException {
        logger.info("Launching local cluster: {}", spec.getName());
        
        ClusterInstance instance = new ClusterInstance(
            generateClusterId(spec),
            spec.getName(),
            spec
        );
        
        try {
            ensureVersionInstalled(spec);
            setupDirectories(spec);
            startMongodProcesses(spec, instance);
            
            instance.setStatus(ClusterInstance.Status.READY);
            instance.setConnectionString(generateConnectionString(spec));
            
            logger.info("Local cluster {} launched successfully", spec.getName());
            return instance;
            
        } catch (Exception e) {
            instance.setStatus(ClusterInstance.Status.ERROR);
            throw new ClusterLaunchException("Failed to launch local cluster: " + spec.getName(), e);
        }
    }
    
    @Override
    public void stop(ClusterInstance instance) throws ClusterLaunchException {
        logger.info("Stopping local cluster: {}", instance.getName());
        // TODO: Stop mongod processes
        instance.setStatus(ClusterInstance.Status.STOPPED);
    }
    
    @Override
    public void destroy(ClusterInstance instance) throws ClusterLaunchException {
        logger.info("Destroying local cluster: {}", instance.getName());
        stop(instance);
        // TODO: Clean up data directories
        instance.setStatus(ClusterInstance.Status.DESTROYED);
    }
    
    @Override
    public ClusterInstance.Status getStatus(ClusterInstance instance) throws ClusterLaunchException {
        // TODO: Check if mongod processes are running
        return instance.getStatus();
    }
    
    @Override
    public boolean supports(ClusterSpec spec) {
        return spec instanceof LocalClusterSpec;
    }
    
    private String generateClusterId(LocalClusterSpec spec) {
        return "local-" + spec.getName() + "-" + System.currentTimeMillis();
    }
    
    private void setupDirectories(LocalClusterSpec spec) throws IOException {
        if (spec.getDataPath() != null) {
            Path dataPath = Paths.get(spec.getDataPath());
            Files.createDirectories(dataPath);
        }
        
        if (spec.getLogPath() != null) {
            Path logPath = Paths.get(spec.getLogPath()).getParent();
            if (logPath != null) {
                Files.createDirectories(logPath);
            }
        }
    }
    
    private void startMongodProcesses(LocalClusterSpec spec, ClusterInstance instance) throws IOException, ClusterLaunchException {
        switch (spec.getTopology()) {
            case STANDALONE:
                startStandalone(spec, instance);
                break;
            case REPLICA_SET:
                startReplicaSet(spec, instance);
                break;
            case SHARDED:
                startShardedCluster(spec, instance);
                break;
        }
    }
    
    private void startStandalone(LocalClusterSpec spec, ClusterInstance instance) throws IOException, ClusterLaunchException {
        List<String> command = buildMongodCommand(spec, spec.getPort());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        
        // TODO: Store process reference for later management
        logger.info("Started standalone mongod on port {}", spec.getPort());
    }
    
    private void startReplicaSet(LocalClusterSpec spec, ClusterInstance instance) throws IOException, ClusterLaunchException {
        // TODO: Implement replica set launch logic similar to mlaunch
        for (int i = 0; i < spec.getReplicaSetSize(); i++) {
            int port = spec.getPort() + i;
            List<String> command = buildMongodCommand(spec, port);
            command.add("--replSet");
            command.add(spec.getName());
            
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            
            logger.info("Started replica set member {} on port {}", i, port);
        }
        
        // TODO: Initialize replica set
    }
    
    private void startShardedCluster(LocalClusterSpec spec, ClusterInstance instance) throws IOException {
        // TODO: Implement sharded cluster launch logic
        // This would involve config servers, mongos routers, and shard replica sets
        throw new UnsupportedOperationException("Sharded clusters not yet implemented");
    }
    
    private void ensureVersionInstalled(LocalClusterSpec spec) throws ClusterLaunchException {
        try {
            MongoVersion version = new MongoVersion(spec.getMongoVersion());
            if (!versionManager.isVersionInstalled(version)) {
                logger.info("MongoDB version {} not found locally, installing...", version);
                versionManager.installVersion(version);
            }
        } catch (Exception e) {
            throw new ClusterLaunchException("Failed to ensure MongoDB version is installed: " + spec.getMongoVersion(), e);
        }
    }
    
    private List<String> buildMongodCommand(LocalClusterSpec spec, int port) throws ClusterLaunchException {
        List<String> command = new ArrayList<>();
        
        try {
            MongoVersion version = new MongoVersion(spec.getMongoVersion());
            Path mongodPath = versionManager.getMongodPath(version);
            command.add(mongodPath.toString());
        } catch (Exception e) {
            throw new ClusterLaunchException("Failed to locate mongod binary for version: " + spec.getMongoVersion(), e);
        }
        
        command.add("--port");
        command.add(String.valueOf(port));
        
        if (spec.getDataPath() != null) {
            command.add("--dbpath");
            command.add(spec.getDataPath() + "/" + port);
        }
        
        if (spec.getLogPath() != null) {
            command.add("--logpath");
            command.add(spec.getLogPath() + "/mongod-" + port + ".log");
        }
        
        if (spec.isEnableAuth()) {
            command.add("--auth");
        }
        
        command.addAll(spec.getAdditionalOptions());
        
        return command;
    }
    
    private String generateConnectionString(LocalClusterSpec spec) {
        switch (spec.getTopology()) {
            case STANDALONE:
                return "mongodb://localhost:" + spec.getPort();
            case REPLICA_SET:
                StringBuilder sb = new StringBuilder("mongodb://");
                for (int i = 0; i < spec.getReplicaSetSize(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("localhost:").append(spec.getPort() + i);
                }
                sb.append("/?replicaSet=").append(spec.getName());
                return sb.toString();
            case SHARDED:
                return "mongodb://localhost:" + (spec.getPort() + 1000); // mongos port
            default:
                return "mongodb://localhost:" + spec.getPort();
        }
    }
}