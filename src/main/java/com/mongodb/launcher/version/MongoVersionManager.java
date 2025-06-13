package com.mongodb.launcher.version;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MongoVersionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoVersionManager.class);
    
    private static final String MONGODB_RELEASES_URL = "https://api.github.com/repos/mongodb/mongo/releases";
    private static final String MONGODB_DOWNLOAD_BASE = "https://fastdl.mongodb.org";
    
    private final Path versionsDir;
    private final Path binDir;
    private final ObjectMapper objectMapper;
    private final PlatformDetector.Platform platform;
    
    public MongoVersionManager() {
        this(getDefaultVersionsDir());
    }
    
    public MongoVersionManager(Path versionsDir) {
        this.versionsDir = versionsDir;
        this.binDir = versionsDir.getParent().resolve("bin");
        this.objectMapper = new ObjectMapper();
        this.platform = PlatformDetector.detect();
        
        // Only create directories if we have write permissions and they don't exist
        try {
            if (!Files.exists(this.versionsDir)) {
                Files.createDirectories(this.versionsDir);
            }
            if (!Files.exists(this.binDir)) {
                Files.createDirectories(this.binDir);
            }
        } catch (IOException e) {
            // Don't fail here - just warn. We might be using a read-only location
            // and will handle write operations separately
            logger.warn("Cannot create directories in {}: {}. Will use read-only mode.", 
                versionsDir.getParent(), e.getMessage());
        }
    }
    
    private static Path getDefaultVersionsDir() {
        String mPrefix = System.getenv("M_PREFIX");
        if (mPrefix != null) {
            return Paths.get(mPrefix, "versions");
        }
        
        // Check for existing m installations in common locations
        List<Path> possibleLocations = Arrays.asList(
            Paths.get(System.getProperty("user.home"), ".local", "m", "versions"),
            Paths.get("/usr/local/m/versions"),
            Paths.get("/opt/m/versions")
        );
        
        // Find the best writable location with versions
        Path bestWritableLocation = null;
        int maxWritableVersions = 0;
        
        // Also track the location with most versions (even if read-only)
        Path bestReadOnlyLocation = null;
        int maxTotalVersions = 0;
        
        for (Path location : possibleLocations) {
            if (Files.exists(location) && Files.isDirectory(location)) {
                try {
                    int versionCount = (int) Files.list(location)
                        .filter(Files::isDirectory)
                        .count();
                    
                    // Track location with most versions overall
                    if (versionCount > maxTotalVersions) {
                        maxTotalVersions = versionCount;
                        bestReadOnlyLocation = location;
                    }
                    
                    // Check if location is writable
                    boolean isWritable = Files.isWritable(location.getParent());
                    if (isWritable && versionCount > maxWritableVersions) {
                        maxWritableVersions = versionCount;
                        bestWritableLocation = location;
                    }
                } catch (IOException e) {
                    // Skip this location if we can't read it
                }
            }
        }
        
        // Prefer writable location with versions, fall back to any location with versions
        if (bestWritableLocation != null) {
            return bestWritableLocation;
        } else if (bestReadOnlyLocation != null && maxTotalVersions > 0) {
            return bestReadOnlyLocation;
        }
        
        // Default to user-local installation
        return Paths.get(System.getProperty("user.home"), ".local", "m", "versions");
    }
    
    public List<MongoVersion> getAvailableVersions() throws IOException {
        logger.info("Fetching available MongoDB versions...");
        
        URL url = new URL(MONGODB_RELEASES_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        
        try (InputStream is = conn.getInputStream()) {
            JsonNode releases = objectMapper.readTree(is);
            List<MongoVersion> versions = new ArrayList<>();
            
            for (JsonNode release : releases) {
                String tagName = release.get("tag_name").asText();
                // Remove 'r' prefix if present (e.g., "r7.0.6" -> "7.0.6")
                if (tagName.startsWith("r")) {
                    tagName = tagName.substring(1);
                }
                
                try {
                    versions.add(new MongoVersion(tagName));
                } catch (IllegalArgumentException e) {
                    // Skip invalid version formats
                    logger.debug("Skipping invalid version: {}", tagName);
                }
            }
            
            return versions.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        }
    }
    
    public List<MongoVersion> getInstalledVersions() {
        Set<MongoVersion> allVersions = new HashSet<>();
        
        // Scan all possible m installation locations
        List<Path> possibleLocations = Arrays.asList(
            versionsDir, // Primary location
            Paths.get(System.getProperty("user.home"), ".local", "m", "versions"),
            Paths.get("/usr/local/m/versions"),
            Paths.get("/opt/m/versions")
        );
        
        for (Path location : possibleLocations) {
            if (Files.exists(location) && Files.isDirectory(location)) {
                try {
                    Files.list(location)
                        .filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .forEach(versionStr -> {
                            try {
                                allVersions.add(new MongoVersion(versionStr));
                            } catch (IllegalArgumentException e) {
                                // Skip invalid version directories
                            }
                        });
                } catch (IOException e) {
                    logger.warn("Failed to list versions in {}", location, e);
                }
            }
        }
        
        return allVersions.stream()
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());
    }
    
    public boolean isVersionInstalled(MongoVersion version) {
        return findVersionDir(version) != null;
    }
    
    public Path getVersionDir(MongoVersion version) {
        Path found = findVersionDir(version);
        return found != null ? found : versionsDir.resolve(version.getVersion());
    }
    
    private Path findVersionDir(MongoVersion version) {
        List<Path> possibleLocations = Arrays.asList(
            versionsDir.resolve(version.getVersion()),
            Paths.get(System.getProperty("user.home"), ".local", "m", "versions", version.getVersion()),
            Paths.get("/usr/local/m/versions", version.getVersion()),
            Paths.get("/opt/m/versions", version.getVersion())
        );
        
        for (Path location : possibleLocations) {
            if (Files.exists(location) && Files.isDirectory(location)) {
                return location;
            }
        }
        
        return null;
    }
    
    public Path getMongodPath(MongoVersion version) {
        String executable = PlatformDetector.getExecutableName("mongod", platform);
        Path versionDir = getVersionDir(version);
        return versionDir.resolve("bin").resolve(executable);
    }
    
    public void installVersion(MongoVersion version) throws IOException {
        if (isVersionInstalled(version)) {
            logger.info("Version {} is already installed", version);
            return;
        }
        
        // Ensure we have a writable location for installation
        Path installLocation = getWritableInstallLocation();
        
        logger.info("Installing MongoDB version {} to {}", version, installLocation.getParent());
        
        String downloadUrl = buildDownloadUrl(version);
        Path downloadPath = downloadFile(downloadUrl, version, installLocation);
        extractAndInstall(downloadPath, version, installLocation);
        
        logger.info("Successfully installed MongoDB version {}", version);
    }
    
    private Path getWritableInstallLocation() throws IOException {
        // Try to use the primary versionsDir if writable
        if (Files.isWritable(versionsDir.getParent())) {
            return versionsDir;
        }
        
        // Fall back to user-local installation
        Path userLocal = Paths.get(System.getProperty("user.home"), ".local", "m", "versions");
        Files.createDirectories(userLocal);
        return userLocal;
    }
    
    private String buildDownloadUrl(MongoVersion version) {
        String suffix = PlatformDetector.getDownloadSuffix(platform);
        String filename = String.format("mongodb-%s-%s", 
            platform.getOs().toString().toLowerCase(), version.getVersion());
        
        // Handle different URL patterns for different OS
        switch (platform.getOs()) {
            case LINUX:
                return String.format("%s/linux/mongodb-linux-%s-%s.tgz", 
                    MONGODB_DOWNLOAD_BASE, 
                    PlatformDetector.getArchString(platform.getArch()), 
                    version.getVersion());
            case MACOS:
                return String.format("%s/osx/mongodb-macos-%s-%s.tgz", 
                    MONGODB_DOWNLOAD_BASE, 
                    PlatformDetector.getArchString(platform.getArch()), 
                    version.getVersion());
            case WINDOWS:
                return String.format("%s/windows/mongodb-windows-%s-%s.zip", 
                    MONGODB_DOWNLOAD_BASE, 
                    PlatformDetector.getArchString(platform.getArch()), 
                    version.getVersion());
            default:
                throw new UnsupportedOperationException("Unsupported platform: " + platform);
        }
    }
    
    private Path downloadFile(String url, MongoVersion version, Path installLocation) throws IOException {
        logger.info("Downloading MongoDB {} from {}", version, url);
        
        Path downloadPath = installLocation.resolve(version.getVersion() + 
            (platform.getOs() == PlatformDetector.OS.WINDOWS ? ".zip" : ".tgz"));
        
        URL downloadUrl = new URL(url);
        try (InputStream in = downloadUrl.openStream()) {
            Files.copy(in, downloadPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        return downloadPath;
    }
    
    private void extractAndInstall(Path archivePath, MongoVersion version, Path installLocation) throws IOException {
        Path versionDir = installLocation.resolve(version.getVersion());
        Files.createDirectories(versionDir);
        
        if (archivePath.toString().endsWith(".zip")) {
            extractZip(archivePath, versionDir);
        } else {
            extractTarGz(archivePath, versionDir);
        }
        
        // Clean up downloaded archive
        Files.deleteIfExists(archivePath);
        
        // Make binaries executable (Unix-like systems)
        if (platform.getOs() != PlatformDetector.OS.WINDOWS) {
            makeExecutable(versionDir.resolve("bin"));
        }
    }
    
    private void extractZip(Path zipPath, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    
    private void extractTarGz(Path tarGzPath, Path destDir) throws IOException {
        // For simplicity, using external tar command
        // In production, consider using Apache Commons Compress
        ProcessBuilder pb = new ProcessBuilder("tar", "-xzf", tarGzPath.toString(), "-C", destDir.toString(), "--strip-components=1");
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Failed to extract tar.gz file, exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Extraction interrupted", e);
        }
    }
    
    private void makeExecutable(Path binDir) throws IOException {
        if (Files.exists(binDir)) {
            Files.list(binDir).forEach(file -> {
                try {
                    file.toFile().setExecutable(true);
                } catch (Exception e) {
                    logger.warn("Failed to make {} executable", file, e);
                }
            });
        }
    }
    
    public MongoVersion findVersion(String versionPattern) throws IOException {
        List<MongoVersion> installed = getInstalledVersions();
        
        // First try exact match in installed versions
        for (MongoVersion version : installed) {
            if (version.matches(versionPattern)) {
                return version;
            }
        }
        
        // If not found locally, check available versions
        List<MongoVersion> available = getAvailableVersions();
        for (MongoVersion version : available) {
            if (version.matches(versionPattern)) {
                return version;
            }
        }
        
        throw new IllegalArgumentException("No MongoDB version found matching: " + versionPattern);
    }
    
    public MongoVersion getLatestVersion(String majorMinor) throws IOException {
        List<MongoVersion> available = getAvailableVersions();
        
        return available.stream()
            .filter(v -> v.getMajorMinor().equals(majorMinor))
            .filter(v -> !v.isPreRelease())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No version found for " + majorMinor));
    }
    
    public void removeVersion(MongoVersion version) throws IOException {
        Path versionDir = getVersionDir(version);
        if (Files.exists(versionDir)) {
            deleteDirectory(versionDir);
            logger.info("Removed MongoDB version {}", version);
        }
    }
    
    private void deleteDirectory(Path dir) throws IOException {
        Files.walk(dir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }
    
    public Path getVersionsDir() {
        return versionsDir;
    }
}