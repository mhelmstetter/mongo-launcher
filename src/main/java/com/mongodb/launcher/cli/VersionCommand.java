package com.mongodb.launcher.cli;

import com.mongodb.launcher.version.MongoVersion;
import com.mongodb.launcher.version.MongoVersionManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "version",
    description = "Manage MongoDB versions",
    subcommands = {
        VersionCommand.ListVersionsCommand.class,
        VersionCommand.InstallVersionCommand.class,
        VersionCommand.RemoveVersionCommand.class,
        VersionCommand.AvailableVersionsCommand.class,
        VersionInfoCommand.class
    }
)
public class VersionCommand implements Callable<Integer> {
    
    @Override
    public Integer call() throws Exception {
        System.out.println("MongoDB Version Manager");
        System.out.println("Commands:");
        System.out.println("  list      - List installed versions");
        System.out.println("  install   - Install a specific version");
        System.out.println("  remove    - Remove an installed version");
        System.out.println("  available - List available versions for download");
        System.out.println("  info      - Show installation information and locations");
        return 0;
    }
    
    @Command(name = "list", description = "List installed MongoDB versions")
    static class ListVersionsCommand implements Callable<Integer> {
        
        @Override
        public Integer call() throws Exception {
            MongoVersionManager manager = new MongoVersionManager();
            List<MongoVersion> versions = manager.getInstalledVersions();
            
            if (versions.isEmpty()) {
                System.out.println("No MongoDB versions installed");
            } else {
                System.out.println("Installed MongoDB versions:");
                for (MongoVersion version : versions) {
                    System.out.println("  " + version.getVersion());
                }
            }
            
            return 0;
        }
    }
    
    @Command(name = "install", description = "Install a MongoDB version")
    static class InstallVersionCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Version to install (e.g., 7.0.6, 7.0, latest)")
        private String version;
        
        @Override
        public Integer call() throws Exception {
            try {
                MongoVersionManager manager = new MongoVersionManager();
                
                MongoVersion mongoVersion;
                if ("latest".equals(version)) {
                    List<MongoVersion> available = manager.getAvailableVersions();
                    mongoVersion = available.get(0); // First is latest
                } else {
                    mongoVersion = manager.findVersion(version);
                }
                
                System.out.println("Installing MongoDB version " + mongoVersion.getVersion() + "...");
                manager.installVersion(mongoVersion);
                System.out.println("Successfully installed MongoDB " + mongoVersion.getVersion());
                
                return 0;
            } catch (Exception e) {
                System.err.println("Failed to install version: " + e.getMessage());
                return 1;
            }
        }
    }
    
    @Command(name = "remove", description = "Remove an installed MongoDB version")
    static class RemoveVersionCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Version to remove")
        private String version;
        
        @Override
        public Integer call() throws Exception {
            try {
                MongoVersionManager manager = new MongoVersionManager();
                MongoVersion mongoVersion = new MongoVersion(version);
                
                if (!manager.isVersionInstalled(mongoVersion)) {
                    System.err.println("Version " + version + " is not installed");
                    return 1;
                }
                
                manager.removeVersion(mongoVersion);
                System.out.println("Removed MongoDB version " + version);
                
                return 0;
            } catch (Exception e) {
                System.err.println("Failed to remove version: " + e.getMessage());
                return 1;
            }
        }
    }
    
    @Command(name = "available", description = "List available MongoDB versions for download")
    static class AvailableVersionsCommand implements Callable<Integer> {
        
        @Option(names = {"-l", "--limit"}, description = "Limit number of versions to show", defaultValue = "20")
        private int limit;
        
        @Override
        public Integer call() throws Exception {
            try {
                MongoVersionManager manager = new MongoVersionManager();
                List<MongoVersion> versions = manager.getAvailableVersions();
                
                System.out.println("Available MongoDB versions (showing latest " + limit + "):");
                versions.stream()
                    .limit(limit)
                    .forEach(v -> System.out.println("  " + v.getVersion()));
                
                return 0;
            } catch (Exception e) {
                System.err.println("Failed to fetch available versions: " + e.getMessage());
                return 1;
            }
        }
    }
}