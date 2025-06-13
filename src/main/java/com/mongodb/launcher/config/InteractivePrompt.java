package com.mongodb.launcher.config;

import com.mongodb.launcher.version.MongoVersion;
import com.mongodb.launcher.version.MongoVersionManager;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

public class InteractivePrompt {
    
    private final Scanner scanner;
    private final Console console;
    private final ConfigManager configManager;
    
    public InteractivePrompt(ConfigManager configManager) {
        this.configManager = configManager;
        this.console = System.console();
        this.scanner = new Scanner(System.in);
    }
    
    public String promptForInput(String message, String defaultValue) {
        String prompt = defaultValue != null ? 
            String.format("%s [%s]: ", message, defaultValue) : 
            String.format("%s: ", message);
        
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty() && defaultValue != null) {
            return defaultValue;
        }
        
        return input.isEmpty() ? null : input;
    }
    
    public String promptForPassword(String message) {
        if (console != null) {
            char[] password = console.readPassword("%s: ", message);
            return password != null ? new String(password) : null;
        } else {
            // Fallback for IDEs or environments without console
            System.out.print(message + ": ");
            return scanner.nextLine();
        }
    }
    
    public boolean promptForConfirmation(String message, boolean defaultValue) {
        String defaultStr = defaultValue ? "Y/n" : "y/N";
        System.out.printf("%s [%s]: ", message, defaultStr);
        
        String input = scanner.nextLine().trim().toLowerCase();
        
        if (input.isEmpty()) {
            return defaultValue;
        }
        
        return input.startsWith("y") || input.equals("true");
    }
    
    public String promptForChoice(String message, String[] choices, String defaultChoice) {
        System.out.println(message);
        for (int i = 0; i < choices.length; i++) {
            String marker = choices[i].equals(defaultChoice) ? " (default)" : "";
            System.out.printf("  %d) %s%s%n", i + 1, choices[i], marker);
        }
        
        System.out.print("Choose [1]: ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            return defaultChoice;
        }
        
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= choices.length) {
                return choices[choice - 1];
            }
        } catch (NumberFormatException e) {
            // Try to match by string
            for (String choice : choices) {
                if (choice.toLowerCase().startsWith(input.toLowerCase())) {
                    return choice;
                }
            }
        }
        
        System.out.println("Invalid choice, using default: " + defaultChoice);
        return defaultChoice;
    }
    
    public String promptForMongoVersion() {
        try {
            MongoVersionManager versionManager = new MongoVersionManager();
            List<MongoVersion> installed = versionManager.getInstalledVersions();
            
            if (!installed.isEmpty()) {
                System.out.println("Available installed MongoDB versions:");
                for (int i = 0; i < Math.min(installed.size(), 10); i++) {
                    MongoVersion version = installed.get(i);
                    System.out.printf("  %d) %s%n", i + 1, version.getVersion());
                }
                
                if (installed.size() > 10) {
                    System.out.printf("  ... and %d more%n", installed.size() - 10);
                }
                
                String defaultVersion = configManager.getDefaultMongoVersion();
                String choice = promptForInput("Enter MongoDB version", defaultVersion);
                
                // Validate if it's a version number or choice number
                try {
                    int choiceNum = Integer.parseInt(choice);
                    if (choiceNum >= 1 && choiceNum <= installed.size()) {
                        return installed.get(choiceNum - 1).getVersion();
                    }
                } catch (NumberFormatException e) {
                    // It's a version string, return as-is
                }
                
                return choice;
            }
        } catch (Exception e) {
            // Fall back to simple prompt
        }
        
        return promptForInput("MongoDB version", configManager.getDefaultMongoVersion());
    }
    
    public String promptForClusterName(String suggestedName) {
        String defaultName = suggestedName != null ? suggestedName : "test-cluster";
        return promptForInput("Cluster name", defaultName);
    }
    
    public String promptForClusterType() {
        String[] types = {"local", "atlas"};
        return promptForChoice("Cluster type:", types, "local");
    }
    
    public String promptForTopology(boolean isLocal) {
        if (isLocal) {
            String[] topologies = {"standalone", "replica-set", "sharded"};
            return promptForChoice("Cluster topology:", topologies, "standalone");
        } else {
            String[] topologies = {"replica-set", "sharded"};
            return promptForChoice("Cluster topology:", topologies, "replica-set");
        }
    }
    
    public int promptForPort(int defaultPort) {
        String input = promptForInput("Port", String.valueOf(defaultPort));
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port, using default: " + defaultPort);
            return defaultPort;
        }
    }
    
    public int promptForReplicaSetSize() {
        String input = promptForInput("Replica set size", "3");
        try {
            int size = Integer.parseInt(input);
            return Math.max(1, Math.min(50, size)); // Reasonable bounds
        } catch (NumberFormatException e) {
            System.out.println("Invalid size, using default: 3");
            return 3;
        }
    }
    
    public void displayWelcomeMessage() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│          MongoLauncher Interactive         │");
        System.out.println("│     MongoDB Cluster Management Tool        │");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("Let's set up your MongoDB cluster...");
        System.out.println();
    }
    
    public void displaySummary(String clusterType, String name, String version, String topology) {
        System.out.println();
        System.out.println("Cluster Configuration Summary:");
        System.out.println("──────────────────────────────");
        System.out.printf("  Type:     %s%n", clusterType);
        System.out.printf("  Name:     %s%n", name);
        System.out.printf("  Version:  %s%n", version);
        System.out.printf("  Topology: %s%n", topology);
        System.out.println();
    }
}