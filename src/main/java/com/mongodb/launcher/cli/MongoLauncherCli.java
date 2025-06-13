package com.mongodb.launcher.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "mongo-launcher",
    description = "MongoDB Cluster Management Tool",
    mixinStandardHelpOptions = true,
    version = "1.0.4",
    subcommands = {
        LaunchCommand.class,
        StatusCommand.class,
        StopCommand.class,
        DestroyCommand.class,
        ListCommand.class,
        VersionCommand.class,
        ConfigCommand.class
    }
)
public class MongoLauncherCli implements Runnable {
    
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MongoLauncherCli()).execute(args);
        System.exit(exitCode);
    }
}