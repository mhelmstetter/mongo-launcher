package com.mongodb.launcher.version;

public class PlatformDetector {
    
    public enum OS {
        LINUX, MACOS, WINDOWS, UNKNOWN
    }
    
    public enum Architecture {
        X86_64, ARM64, UNKNOWN
    }
    
    public static class Platform {
        private final OS os;
        private final Architecture arch;
        
        public Platform(OS os, Architecture arch) {
            this.os = os;
            this.arch = arch;
        }
        
        public OS getOs() {
            return os;
        }
        
        public Architecture getArch() {
            return arch;
        }
        
        @Override
        public String toString() {
            return os + "_" + arch;
        }
    }
    
    public static Platform detect() {
        return new Platform(detectOS(), detectArchitecture());
    }
    
    private static OS detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("mac") || osName.contains("darwin")) {
            return OS.MACOS;
        } else if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("linux")) {
            return OS.LINUX;
        } else {
            return OS.UNKNOWN;
        }
    }
    
    private static Architecture detectArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (arch.contains("amd64") || arch.contains("x86_64")) {
            return Architecture.X86_64;
        } else if (arch.contains("aarch64") || arch.contains("arm64")) {
            return Architecture.ARM64;
        } else {
            return Architecture.UNKNOWN;
        }
    }
    
    public static String getDownloadSuffix(Platform platform) {
        switch (platform.getOs()) {
            case LINUX:
                return "linux-" + getArchString(platform.getArch()) + ".tgz";
            case MACOS:
                return "macos-" + getArchString(platform.getArch()) + ".tgz";
            case WINDOWS:
                return "windows-" + getArchString(platform.getArch()) + ".zip";
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platform);
        }
    }
    
    public static String getArchString(Architecture arch) {
        switch (arch) {
            case X86_64:
                return "x86_64";
            case ARM64:
                return "arm64";
            default:
                throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        }
    }
    
    public static String getExecutableName(String baseName, Platform platform) {
        if (platform.getOs() == OS.WINDOWS) {
            return baseName + ".exe";
        }
        return baseName;
    }
}