@echo off
rem MongoLauncher executable script for Windows

rem Find the directory containing this script
set "SCRIPT_DIR=%~dp0"
set "JAR_FILE=%SCRIPT_DIR%mongo-launcher.jar"

rem Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: mongo-launcher.jar not found at %JAR_FILE%
    echo Please run 'mvn clean package' to build the project first.
    exit /b 1
)

rem Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or later.
    exit /b 1
)

rem Execute the JAR file with all provided arguments
java -jar "%JAR_FILE%" %*