package com.mongodb.launcher;

public interface ClusterLauncher<T extends ClusterSpec> {
    
    ClusterInstance launch(T spec) throws ClusterLaunchException;
    
    void stop(ClusterInstance instance) throws ClusterLaunchException;
    
    void destroy(ClusterInstance instance) throws ClusterLaunchException;
    
    ClusterInstance.Status getStatus(ClusterInstance instance) throws ClusterLaunchException;
    
    boolean supports(ClusterSpec spec);
}