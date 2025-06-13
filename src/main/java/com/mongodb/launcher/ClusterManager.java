package com.mongodb.launcher;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class ClusterManager {
    
    private final List<ClusterLauncher<?>> launchers;
    
    @SuppressWarnings("unchecked")
    public ClusterManager() {
        this.launchers = (List<ClusterLauncher<?>>) (List<?>) StreamSupport.stream(
            ServiceLoader.load(ClusterLauncher.class).spliterator(), false)
            .toList();
    }
    
    public ClusterManager(List<ClusterLauncher<?>> launchers) {
        this.launchers = launchers;
    }
    
    @SuppressWarnings("unchecked")
    public ClusterInstance launch(ClusterSpec spec) throws ClusterLaunchException {
        ClusterLauncher<ClusterSpec> launcher = (ClusterLauncher<ClusterSpec>) 
            launchers.stream()
                .filter(l -> l.supports(spec))
                .findFirst()
                .orElseThrow(() -> new ClusterLaunchException(
                    "No launcher found for cluster type: " + spec.getType()));
        
        return launcher.launch(spec);
    }
    
    public void stop(ClusterInstance instance) throws ClusterLaunchException {
        ClusterLauncher<?> launcher = findLauncher(instance.getSpec());
        launcher.stop(instance);
    }
    
    public void destroy(ClusterInstance instance) throws ClusterLaunchException {
        ClusterLauncher<?> launcher = findLauncher(instance.getSpec());
        launcher.destroy(instance);
    }
    
    public ClusterInstance.Status getStatus(ClusterInstance instance) throws ClusterLaunchException {
        ClusterLauncher<?> launcher = findLauncher(instance.getSpec());
        return launcher.getStatus(instance);
    }
    
    private ClusterLauncher<?> findLauncher(ClusterSpec spec) throws ClusterLaunchException {
        return launchers.stream()
            .filter(l -> l.supports(spec))
            .findFirst()
            .orElseThrow(() -> new ClusterLaunchException(
                "No launcher found for cluster type: " + spec.getType()));
    }
    
    public List<ClusterLauncher<?>> getAvailableLaunchers() {
        return launchers;
    }
}