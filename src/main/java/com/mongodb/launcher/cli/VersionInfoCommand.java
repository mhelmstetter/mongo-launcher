package com.mongodb.launcher.cli;

import com.mongodb.launcher.version.MongoVersion;
import com.mongodb.launcher.version.MongoVersionManager;
import picocli.CommandLine.Command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "info", description = "Show MongoDB installation information")
public class VersionInfoCommand implements Callable<Integer> {
    
    @Override
    public Integer call() throws Exception {
        MongoVersionManager manager = new MongoVersionManager();
        
        System.out.println("MongoDB Installation Information");
        System.out.println("================================");
        
        // Show detected primary location
        System.out.println("Primary installation location: " + manager.getVersionsDir());
        System.out.println();
        
        // Check all possible locations
        List<Path> possibleLocations = Arrays.asList(
            Paths.get(System.getProperty("user.home"), ".local", "m", "versions"),
            Paths.get("/usr/local/m/versions"),
            Paths.get("/opt/m/versions")
        );
        
        System.out.println("Scanning for existing installations:");
        for (Path location : possibleLocations) {
            if (Files.exists(location) && Files.isDirectory(location)) {
                try {
                    long versionCount = Files.list(location)
                        .filter(Files::isDirectory)
                        .count();
                    
                    System.out.println("  ✓ " + location + " (" + versionCount + " versions)");
                } catch (Exception e) {
                    System.out.println("  ✗ " + location + " (access denied)");
                }
            } else {
                System.out.println("  - " + location + " (not found)");
            }
        }
        
        System.out.println();
        
        // Show installed versions with their locations
        List<MongoVersion> versions = manager.getInstalledVersions();
        if (versions.isEmpty()) {
            System.out.println("No MongoDB versions found");
        } else {
            System.out.println("Installed versions:");
            for (MongoVersion version : versions) {
                Path versionDir = manager.getVersionDir(version);
                Path mongodPath = manager.getMongodPath(version);
                boolean mongodExists = Files.exists(mongodPath);
                
                System.out.println("  " + version.getVersion() + 
                    " @ " + versionDir.getParent() + 
                    (mongodExists ? " ✓" : " ✗ (mongod missing)"));
            }
        }
        
        return 0;
    }
}