package com.mongodb.launcher.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoVersion implements Comparable<MongoVersion> {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.+))?");
    
    private final int major;
    private final int minor;
    private final int patch;
    private final String preRelease;
    private final String originalVersion;
    
    public MongoVersion(String version) {
        this.originalVersion = version;
        Matcher matcher = VERSION_PATTERN.matcher(version);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid MongoDB version format: " + version);
        }
        
        this.major = Integer.parseInt(matcher.group(1));
        this.minor = Integer.parseInt(matcher.group(2));
        this.patch = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0;
        this.preRelease = matcher.group(4);
    }
    
    public MongoVersion(int major, int minor) {
        this(major, minor, 0, null);
    }
    
    public MongoVersion(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }
    
    public MongoVersion(int major, int minor, int patch, String preRelease) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = preRelease;
        this.originalVersion = buildVersionString();
    }
    
    private String buildVersionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append('.').append(minor);
        if (patch > 0) {
            sb.append('.').append(patch);
        }
        if (preRelease != null && !preRelease.isEmpty()) {
            sb.append('-').append(preRelease);
        }
        return sb.toString();
    }
    
    public int getMajor() {
        return major;
    }
    
    public int getMinor() {
        return minor;
    }
    
    public int getPatch() {
        return patch;
    }
    
    public String getPreRelease() {
        return preRelease;
    }
    
    public String getVersion() {
        return originalVersion;
    }
    
    public String getMajorMinor() {
        return major + "." + minor;
    }
    
    public boolean isPreRelease() {
        return preRelease != null && !preRelease.isEmpty();
    }
    
    public boolean matches(String pattern) {
        if (pattern.equals(getVersion())) {
            return true;
        }
        
        // Match major.minor pattern
        if (pattern.equals(getMajorMinor())) {
            return true;
        }
        
        // Match major pattern
        if (pattern.equals(String.valueOf(major))) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int compareTo(MongoVersion other) {
        int result = Integer.compare(this.major, other.major);
        if (result != 0) return result;
        
        result = Integer.compare(this.minor, other.minor);
        if (result != 0) return result;
        
        result = Integer.compare(this.patch, other.patch);
        if (result != 0) return result;
        
        // Handle pre-release versions (they are considered "less than" release versions)
        if (this.preRelease == null && other.preRelease != null) return 1;
        if (this.preRelease != null && other.preRelease == null) return -1;
        if (this.preRelease != null && other.preRelease != null) {
            return this.preRelease.compareTo(other.preRelease);
        }
        
        return 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MongoVersion that = (MongoVersion) obj;
        return major == that.major && 
               minor == that.minor && 
               patch == that.patch && 
               Objects.equals(preRelease, that.preRelease);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, preRelease);
    }
    
    @Override
    public String toString() {
        return originalVersion;
    }
}