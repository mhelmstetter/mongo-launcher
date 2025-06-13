package com.mongodb.launcher.cli;

import com.mongodb.launcher.config.ConfigManager;
import com.mongodb.launcher.config.UserConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "config",
    description = "Manage MongoLauncher configuration",
    subcommands = {
        ConfigCommand.ShowCommand.class,
        ConfigCommand.SetCommand.class,
        ConfigCommand.GetCommand.class,
        ConfigCommand.UnsetCommand.class,
        ConfigCommand.ResetCommand.class
    }
)
public class ConfigCommand implements Callable<Integer> {
    
    @Override
    public Integer call() throws Exception {
        ConfigManager configManager = new ConfigManager();
        
        System.out.println("MongoLauncher Configuration");
        System.out.println("Commands:");
        System.out.println("  show    - Show all configuration settings");
        System.out.println("  set     - Set a configuration value");
        System.out.println("  get     - Get a configuration value");
        System.out.println("  unset   - Remove a configuration setting");
        System.out.println("  reset   - Reset to default configuration");
        System.out.println();
        System.out.println("Configuration file: " + configManager.getConfigFile());
        
        return 0;
    }
    
    @Command(name = "show", description = "Show all configuration settings")
    static class ShowCommand implements Callable<Integer> {
        
        @Override
        public Integer call() throws Exception {
            ConfigManager configManager = new ConfigManager();
            UserConfig config = configManager.getConfig();
            
            System.out.println("MongoLauncher Configuration");
            System.out.println("════════════════════════════");
            System.out.println();
            
            System.out.println("General Settings:");
            System.out.printf("  defaultMongoVersion:    %s%n", config.getDefaultMongoVersion());
            System.out.printf("  interactiveMode:        %s%n", config.isInteractiveMode());
            System.out.printf("  defaultDataPath:        %s%n", config.getDefaultDataPath());
            System.out.printf("  defaultLogPath:         %s%n", config.getDefaultLogPath());
            System.out.println();
            
            System.out.println("Atlas Settings:");
            System.out.printf("  defaultAtlasProjectId:  %s%n", 
                config.getDefaultAtlasProjectId() != null ? config.getDefaultAtlasProjectId() : "(not set)");
            System.out.printf("  defaultInstanceSize:    %s%n", config.getDefaultInstanceSize());
            System.out.printf("  defaultRegion:          %s%n", config.getDefaultRegion());
            System.out.printf("  defaultCloudProvider:   %s%n", config.getDefaultCloudProvider());
            System.out.println();
            
            Map<String, String> customProps = config.getCustomProperties();
            if (!customProps.isEmpty()) {
                System.out.println("Custom Properties:");
                customProps.forEach((key, value) -> 
                    System.out.printf("  %s: %s%n", key, value));
                System.out.println();
            }
            
            System.out.println("Configuration Location:");
            System.out.printf("  Directory: %s%n", configManager.getConfigDirectory());
            System.out.printf("  File:      %s%n", configManager.getConfigFile());
            
            return 0;
        }
    }
    
    @Command(name = "set", description = "Set a configuration value")
    static class SetCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Configuration key")
        private String key;
        
        @Parameters(index = "1", description = "Configuration value")
        private String value;
        
        @Override
        public Integer call() throws Exception {
            ConfigManager configManager = new ConfigManager();
            UserConfig config = configManager.getConfig();
            
            switch (key.toLowerCase()) {
                case "defaultmongoversion":
                    config.setDefaultMongoVersion(value);
                    break;
                case "interactivemode":
                    config.setInteractiveMode(Boolean.parseBoolean(value));
                    break;
                case "defaultdatapath":
                    config.setDefaultDataPath(value);
                    break;
                case "defaultlogpath":
                    config.setDefaultLogPath(value);
                    break;
                case "defaultatlasprojectid":
                    config.setDefaultAtlasProjectId(value);
                    break;
                case "defaultinstancesize":
                    config.setDefaultInstanceSize(value);
                    break;
                case "defaultregion":
                    config.setDefaultRegion(value);
                    break;
                case "defaultcloudprovider":
                    config.setDefaultCloudProvider(value);
                    break;
                default:
                    configManager.setCustomProperty(key, value);
                    break;
            }
            
            configManager.saveConfig();
            System.out.printf("Set %s = %s%n", key, value);
            
            return 0;
        }
    }
    
    @Command(name = "get", description = "Get a configuration value")
    static class GetCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Configuration key")
        private String key;
        
        @Override
        public Integer call() throws Exception {
            ConfigManager configManager = new ConfigManager();
            UserConfig config = configManager.getConfig();
            
            String value = switch (key.toLowerCase()) {
                case "defaultmongoversion" -> config.getDefaultMongoVersion();
                case "interactivemode" -> String.valueOf(config.isInteractiveMode());
                case "defaultdatapath" -> config.getDefaultDataPath();
                case "defaultlogpath" -> config.getDefaultLogPath();
                case "defaultatlasprojectid" -> config.getDefaultAtlasProjectId();
                case "defaultinstancesize" -> config.getDefaultInstanceSize();
                case "defaultregion" -> config.getDefaultRegion();
                case "defaultcloudprovider" -> config.getDefaultCloudProvider();
                default -> configManager.getCustomProperty(key);
            };
            
            if (value != null) {
                System.out.println(value);
            } else {
                System.err.println("Configuration key '" + key + "' not found");
                return 1;
            }
            
            return 0;
        }
    }
    
    @Command(name = "unset", description = "Remove a configuration setting")
    static class UnsetCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Configuration key")
        private String key;
        
        @Override
        public Integer call() throws Exception {
            ConfigManager configManager = new ConfigManager();
            UserConfig config = configManager.getConfig();
            
            switch (key.toLowerCase()) {
                case "defaultatlasprojectid":
                    config.setDefaultAtlasProjectId(null);
                    configManager.saveConfig();
                    break;
                default:
                    configManager.removeCustomProperty(key);
                    break;
            }
            
            System.out.printf("Unset %s%n", key);
            return 0;
        }
    }
    
    @Command(name = "reset", description = "Reset to default configuration")
    static class ResetCommand implements Callable<Integer> {
        
        @Option(names = {"-f", "--force"}, description = "Force reset without confirmation")
        private boolean force;
        
        @Override
        public Integer call() throws Exception {
            if (!force) {
                System.out.print("This will reset all configuration to defaults. Continue? [y/N]: ");
                String input = System.console() != null ? 
                    System.console().readLine() : 
                    new java.util.Scanner(System.in).nextLine();
                
                if (!input.toLowerCase().startsWith("y")) {
                    System.out.println("Reset cancelled");
                    return 0;
                }
            }
            
            ConfigManager configManager = new ConfigManager();
            // Delete and recreate config
            configManager.getConfigFile().toFile().delete();
            configManager = new ConfigManager();
            
            System.out.println("Configuration reset to defaults");
            return 0;
        }
    }
}