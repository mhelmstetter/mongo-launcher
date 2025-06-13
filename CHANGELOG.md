# Changelog

All notable changes to MongoLauncher will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2025-06-13

### Added
- **Interactive CLI Mode**: Claude-like interactive prompting for guided cluster setup
- **Configuration Management**: Persistent, platform-specific configuration storage
  - macOS: `~/.mongo-launcher/config.json`
  - Linux: `~/.config/mongo-launcher/config.json`
  - Windows: `%APPDATA%\MongoLauncher\config.json`
- **Configuration Commands**: Full CLI for managing settings
  - `config show` - Display all configuration settings
  - `config set/get/unset` - Manage individual settings
  - `config reset` - Reset to defaults
- **Cross-Platform Installers**: Automated installation scripts
  - `bin/install.sh` - Unix/Linux/macOS installer
  - `bin/install.bat` - Windows installer with PATH management
- **Distribution Packaging**: Complete packaging system
  - `bin/package.sh` - Creates platform-specific packages
  - Universal, macOS, Linux, and Windows packages
  - SHA256 checksums for verification
- **Homebrew Support**: Ready-to-use Homebrew formula
- **GitHub Actions**: Automated CI/CD pipeline
  - Automated testing and building
  - Release automation with artifacts
- **Comprehensive Documentation**:
  - Updated README.md with interactive features
  - Detailed INSTALL.md guide
  - CHANGELOG.md for version tracking

### Features
- **Unified API**: Single interface for managing both Atlas and local MongoDB clusters
- **Atlas Integration**: Leverage atlas-api-client for cloud cluster management
- **Local Clusters**: Launch standalone, replica set, and sharded clusters locally
- **Version Management**: Automatic MongoDB version installation (compatible with `m` tool)
- **Smart Defaults**: Intelligent configuration defaults based on user environment
- **Non-Interactive Mode**: Full CLI support for automation and scripting
- **Cross-Platform**: Native support for macOS, Linux, and Windows

### Technical Details
- **Java 17+** requirement
- **Maven** build system with shade plugin for executable JAR
- **picocli** for command-line interface
- **Jackson** for JSON configuration management
- **Platform Detection** for OS-specific behavior
- **Multi-location Version Management** compatible with existing `m` installations

### Installation Options
1. **Automated Installers**: Platform-specific install scripts
2. **Homebrew**: Native macOS package manager support
3. **Manual Installation**: Direct JAR execution
4. **Distribution Packages**: Pre-built platform packages

### Project Structure
```
mongo-launcher/
├── bin/                    # Distribution tools and executables
├── src/main/java/         # Source code
├── .github/workflows/     # CI/CD automation
├── homebrew/             # Homebrew formula
├── README.md             # Comprehensive documentation
├── INSTALL.md            # Installation guide
└── CHANGELOG.md          # This file
```

[Unreleased]: https://github.com/mhelmstetter/mongo-launcher/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/mhelmstetter/mongo-launcher/releases/tag/v1.0.0