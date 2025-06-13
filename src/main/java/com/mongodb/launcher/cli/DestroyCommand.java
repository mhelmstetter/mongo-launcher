package com.mongodb.launcher.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
    name = "destroy",
    description = "Destroy a cluster and clean up resources"
)
public class DestroyCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Cluster ID or name")
    private String clusterId;
    
    @Override
    public Integer call() throws Exception {
        System.out.println("Destroying cluster: " + clusterId);
        // TODO: Implement cluster destruction
        return 0;
    }
}