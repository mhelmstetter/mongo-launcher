package com.mongodb.launcher.cli;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "list",
    description = "List all managed clusters"
)
public class ListCommand implements Callable<Integer> {
    
    @Override
    public Integer call() throws Exception {
        System.out.println("Listing all clusters:");
        // TODO: Implement cluster listing
        return 0;
    }
}