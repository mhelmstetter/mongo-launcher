package com.mongodb.launcher;

public class ClusterLaunchException extends Exception {
    
    public ClusterLaunchException(String message) {
        super(message);
    }
    
    public ClusterLaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}