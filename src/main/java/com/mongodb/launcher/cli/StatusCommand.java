package com.mongodb.launcher.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
    name = "status",
    description = "Get cluster status"
)
public class StatusCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Cluster ID or name")
    private String clusterId;
    
    @Override
    public Integer call() throws Exception {
        System.out.println("Getting status for cluster: " + clusterId);
        // TODO: Implement status checking
        return 0;
    }
}