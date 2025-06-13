package com.mongodb.launcher.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class UserConfig {
    
    @JsonProperty("defaultMongoVersion")
    private String defaultMongoVersion = "7.0";
    
    @JsonProperty("defaultAtlasProjectId")
    private String defaultAtlasProjectId;
    
    @JsonProperty("defaultInstanceSize")
    private String defaultInstanceSize = "M10";
    
    @JsonProperty("defaultRegion")
    private String defaultRegion = "US_EAST_1";
    
    @JsonProperty("defaultCloudProvider")
    private String defaultCloudProvider = "AWS";
    
    @JsonProperty("interactiveMode")
    private boolean interactiveMode = true;
    
    @JsonProperty("defaultDataPath")
    private String defaultDataPath;
    
    @JsonProperty("defaultLogPath")
    private String defaultLogPath;
    
    @JsonProperty("customProperties")
    private Map<String, String> customProperties = new HashMap<>();
    
    public UserConfig() {
        // Set platform-specific defaults
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            defaultDataPath = userHome + "\\AppData\\Local\\MongoLauncher\\data";
            defaultLogPath = userHome + "\\AppData\\Local\\MongoLauncher\\logs";
        } else {
            defaultDataPath = userHome + "/.mongo-launcher/data";
            defaultLogPath = userHome + "/.mongo-launcher/logs";
        }
    }
    
    // Getters and setters
    public String getDefaultMongoVersion() {
        return defaultMongoVersion;
    }
    
    public void setDefaultMongoVersion(String defaultMongoVersion) {
        this.defaultMongoVersion = defaultMongoVersion;
    }
    
    public String getDefaultAtlasProjectId() {
        return defaultAtlasProjectId;
    }
    
    public void setDefaultAtlasProjectId(String defaultAtlasProjectId) {
        this.defaultAtlasProjectId = defaultAtlasProjectId;
    }
    
    public String getDefaultInstanceSize() {
        return defaultInstanceSize;
    }
    
    public void setDefaultInstanceSize(String defaultInstanceSize) {
        this.defaultInstanceSize = defaultInstanceSize;
    }
    
    public String getDefaultRegion() {
        return defaultRegion;
    }
    
    public void setDefaultRegion(String defaultRegion) {
        this.defaultRegion = defaultRegion;
    }
    
    public String getDefaultCloudProvider() {
        return defaultCloudProvider;
    }
    
    public void setDefaultCloudProvider(String defaultCloudProvider) {
        this.defaultCloudProvider = defaultCloudProvider;
    }
    
    public boolean isInteractiveMode() {
        return interactiveMode;
    }
    
    public void setInteractiveMode(boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
    }
    
    public String getDefaultDataPath() {
        return defaultDataPath;
    }
    
    public void setDefaultDataPath(String defaultDataPath) {
        this.defaultDataPath = defaultDataPath;
    }
    
    public String getDefaultLogPath() {
        return defaultLogPath;
    }
    
    public void setDefaultLogPath(String defaultLogPath) {
        this.defaultLogPath = defaultLogPath;
    }
    
    public Map<String, String> getCustomProperties() {
        if (customProperties == null) {
            customProperties = new HashMap<>();
        }
        return customProperties;
    }
    
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }
}