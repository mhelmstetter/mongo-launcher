package com.mongodb.launcher.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
    name = "stop",
    description = "Stop a running cluster"
)
public class StopCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Cluster ID or name")
    private String clusterId;
    
    @Override
    public Integer call() throws Exception {
        System.out.println("Stopping cluster: " + clusterId);
        // TODO: Implement cluster stopping
        return 0;
    }
}