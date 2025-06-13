package com.mongodb.launcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE_NAME = "config.json";
    
    private final Path configDir;
    private final Path configFile;
    private final ObjectMapper objectMapper;
    private UserConfig config;
    
    public ConfigManager() {
        this.configDir = determineConfigDirectory();
        this.configFile = configDir.resolve(CONFIG_FILE_NAME);
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        
        try {
            Files.createDirectories(configDir);
            loadConfig();
        } catch (IOException e) {
            logger.warn("Failed to initialize config directory: {}", e.getMessage());
            this.config = new UserConfig();
        }
    }
    
    private static Path determineConfigDirectory() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (osName.contains("win")) {
            // Windows: %APPDATA%\MongoLauncher
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "MongoLauncher");
            }
            return Paths.get(userHome, "AppData", "Roaming", "MongoLauncher");
        } else if (osName.contains("mac")) {
            // macOS: ~/.mongo-launcher
            return Paths.get(userHome, ".mongo-launcher");
        } else {
            // Linux/Unix: ~/.config/mongo-launcher
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null) {
                return Paths.get(xdgConfig, "mongo-launcher");
            }
            return Paths.get(userHome, ".config", "mongo-launcher");
        }
    }
    
    private void loadConfig() {
        if (Files.exists(configFile)) {
            try {
                config = objectMapper.readValue(configFile.toFile(), UserConfig.class);
                logger.debug("Loaded configuration from {}", configFile);
            } catch (IOException e) {
                logger.warn("Failed to load config from {}: {}", configFile, e.getMessage());
                config = new UserConfig();
            }
        } else {
            config = new UserConfig();
            logger.debug("No existing config found, using defaults");
        }
    }
    
    public void saveConfig() {
        try {
            objectMapper.writeValue(configFile.toFile(), config);
            logger.debug("Saved configuration to {}", configFile);
        } catch (IOException e) {
            logger.error("Failed to save config to {}: {}", configFile, e.getMessage());
        }
    }
    
    public UserConfig getConfig() {
        return config;
    }
    
    public Path getConfigDirectory() {
        return configDir;
    }
    
    public Path getConfigFile() {
        return configFile;
    }
    
    // Convenience methods for common settings
    public String getDefaultMongoVersion() {
        return config.getDefaultMongoVersion();
    }
    
    public void setDefaultMongoVersion(String version) {
        config.setDefaultMongoVersion(version);
        saveConfig();
    }
    
    public String getDefaultAtlasProjectId() {
        return config.getDefaultAtlasProjectId();
    }
    
    public void setDefaultAtlasProjectId(String projectId) {
        config.setDefaultAtlasProjectId(projectId);
        saveConfig();
    }
    
    public boolean isInteractiveMode() {
        return config.isInteractiveMode();
    }
    
    public void setInteractiveMode(boolean interactive) {
        config.setInteractiveMode(interactive);
        saveConfig();
    }
    
    public String getCustomProperty(String key) {
        return config.getCustomProperties().get(key);
    }
    
    public void setCustomProperty(String key, String value) {
        config.getCustomProperties().put(key, value);
        saveConfig();
    }
    
    public void removeCustomProperty(String key) {
        config.getCustomProperties().remove(key);
        saveConfig();
    }
    
    public Map<String, String> getAllCustomProperties() {
        return new HashMap<>(config.getCustomProperties());
    }
}