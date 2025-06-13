package com.mongodb.launcher.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.launcher.*;
import com.mongodb.launcher.atlas.AtlasClusterLauncher;
import com.mongodb.launcher.config.ConfigManager;
import com.mongodb.launcher.config.InteractivePrompt;
import com.mongodb.launcher.local.LocalClusterLauncher;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "launch",
    description = "Launch a MongoDB cluster"
)
public class LaunchCommand implements Callable<Integer> {
    
    @Parameters(index = "0", description = "Cluster specification file (JSON)", arity = "0..1")
    private File specFile;
    
    @Option(names = {"-t", "--type"}, description = "Cluster type: atlas, local")
    private String type;
    
    @Option(names = {"-n", "--name"}, description = "Cluster name")
    private String name;
    
    @Option(names = {"--mongo-version"}, description = "MongoDB version", defaultValue = "7.0")
    private String mongoVersion;
    
    @Option(names = {"-p", "--port"}, description = "Port (local clusters only)", defaultValue = "27017")
    private int port;
    
    @Option(names = {"--replica-set-size"}, description = "Replica set size (local clusters only)", defaultValue = "3")
    private int replicaSetSize;
    
    @Option(names = {"--project-id"}, description = "Atlas project ID (atlas clusters only)")
    private String projectId;
    
    @Option(names = {"--instance-size"}, description = "Atlas instance size (atlas clusters only)", defaultValue = "M10")
    private String instanceSize;
    
    @Option(names = {"--non-interactive"}, description = "Disable interactive prompts")
    private boolean nonInteractive;
    
    @Override
    public Integer call() throws Exception {
        ConfigManager configManager = new ConfigManager();
        
        ClusterManager manager = new ClusterManager(List.of(
            new AtlasClusterLauncher(),
            new LocalClusterLauncher()
        ));
        
        ClusterSpec spec;
        
        if (specFile != null && specFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            spec = mapper.readValue(specFile, ClusterSpec.class);
        } else {
            spec = createSpecFromOptions(configManager);
        }
        
        try {
            ClusterInstance instance = manager.launch(spec);
            System.out.println("Cluster launched successfully!");
            System.out.println("ID: " + instance.getId());
            System.out.println("Name: " + instance.getName());
            System.out.println("Status: " + instance.getStatus());
            System.out.println("Connection String: " + instance.getConnectionString());
            
            return 0;
        } catch (ClusterLaunchException e) {
            System.err.println("Failed to launch cluster: " + e.getMessage());
            return 1;
        }
    }
    
    private ClusterSpec createSpecFromOptions(ConfigManager configManager) {
        boolean interactive = configManager.isInteractiveMode() && !nonInteractive;
        InteractivePrompt prompt = interactive ? new InteractivePrompt(configManager) : null;
        
        if (interactive && hasMinimalOptions()) {
            prompt.displayWelcomeMessage();
        }
        
        // Determine cluster type
        String clusterType = type;
        if (clusterType == null && interactive) {
            clusterType = prompt.promptForClusterType();
        } else if (clusterType == null) {
            clusterType = "local"; // default
        }
        
        // Determine cluster name
        String clusterName = name;
        if (clusterName == null && interactive) {
            clusterName = prompt.promptForClusterName(null);
        } else if (clusterName == null) {
            throw new IllegalArgumentException("Cluster name is required. Use --name option or enable interactive mode.");
        }
        
        // Determine MongoDB version
        String version = mongoVersion != null ? mongoVersion : configManager.getDefaultMongoVersion();
        if (interactive && mongoVersion == null) {
            version = prompt.promptForMongoVersion();
        }
        
        ClusterSpec spec;
        
        if ("atlas".equalsIgnoreCase(clusterType)) {
            spec = createAtlasSpec(clusterName, version, configManager, prompt, interactive);
        } else {
            spec = createLocalSpec(clusterName, version, configManager, prompt, interactive);
        }
        
        if (interactive) {
            String topology = spec instanceof LocalClusterSpec ? 
                ((LocalClusterSpec) spec).getTopology().toString().toLowerCase().replace("_", "-") :
                ((AtlasClusterSpec) spec).getTopology().toString().toLowerCase().replace("_", "-");
            prompt.displaySummary(clusterType, clusterName, version, topology);
            
            if (!prompt.promptForConfirmation("Proceed with cluster creation?", true)) {
                System.out.println("Cluster creation cancelled.");
                System.exit(0);
            }
        }
        
        return spec;
    }
    
    private AtlasClusterSpec createAtlasSpec(String clusterName, String version, 
                                           ConfigManager configManager, InteractivePrompt prompt, boolean interactive) {
        AtlasClusterSpec spec = new AtlasClusterSpec(clusterName, version);
        
        // Project ID
        String atlasProjectId = projectId != null ? projectId : configManager.getDefaultAtlasProjectId();
        if (atlasProjectId == null && interactive) {
            atlasProjectId = prompt.promptForInput("Atlas Project ID", null);
        }
        if (atlasProjectId == null) {
            throw new IllegalArgumentException("Atlas project ID is required. Use --project-id option or set defaultAtlasProjectId in config.");
        }
        spec.setProjectId(atlasProjectId);
        
        // Instance size
        String size = instanceSize != null ? instanceSize : configManager.getConfig().getDefaultInstanceSize();
        if (interactive && instanceSize == null) {
            String[] sizes = {"M0", "M2", "M5", "M10", "M20", "M30", "M40", "M50"};
            size = prompt.promptForChoice("Instance size:", sizes, size);
        }
        spec.setInstanceSize(size);
        
        // Topology
        if (interactive) {
            String topology = prompt.promptForTopology(false);
            if ("sharded".equals(topology)) {
                spec.setTopology(AtlasClusterSpec.ClusterTopology.SHARDED);
            }
        }
        
        return spec;
    }
    
    private LocalClusterSpec createLocalSpec(String clusterName, String version,
                                           ConfigManager configManager, InteractivePrompt prompt, boolean interactive) {
        LocalClusterSpec spec = new LocalClusterSpec(clusterName, version);
        
        // Port
        int clusterPort = port != 27017 ? port : 27017;
        if (interactive && port == 27017) {
            clusterPort = prompt.promptForPort(27017);
        }
        spec.setPort(clusterPort);
        
        // Topology
        if (interactive) {
            String topology = prompt.promptForTopology(true);
            switch (topology) {
                case "replica-set":
                    spec.setTopology(LocalClusterSpec.LocalTopology.REPLICA_SET);
                    spec.setReplicaSetSize(prompt.promptForReplicaSetSize());
                    break;
                case "sharded":
                    spec.setTopology(LocalClusterSpec.LocalTopology.SHARDED);
                    break;
                default:
                    spec.setTopology(LocalClusterSpec.LocalTopology.STANDALONE);
            }
        } else if (replicaSetSize != 3) {
            spec.setReplicaSetSize(replicaSetSize);
        }
        
        // Data and log paths
        String dataPath = configManager.getConfig().getDefaultDataPath();
        String logPath = configManager.getConfig().getDefaultLogPath();
        
        if (interactive) {
            boolean useCustomPaths = prompt.promptForConfirmation("Use custom data/log paths?", false);
            if (useCustomPaths) {
                dataPath = prompt.promptForInput("Data path", dataPath);
                logPath = prompt.promptForInput("Log path", logPath);
            }
        }
        
        spec.setDataPath(dataPath);
        spec.setLogPath(logPath);
        
        return spec;
    }
    
    private boolean hasMinimalOptions() {
        return name == null || type == null;
    }
}