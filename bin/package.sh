#!/bin/bash

# MongoLauncher Distribution Packaging Script
# Creates distribution packages for different platforms

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DIST_DIR="$PROJECT_DIR/dist"
VERSION="1.0.3"
PROJECT_NAME="mongo-launcher"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_color() {
    printf "${1}${2}${NC}\n"
}

print_color $BLUE "MongoLauncher Distribution Packager"
print_color $BLUE "======================================"
echo

# Clean and create dist directory
print_color $BLUE "Cleaning distribution directory..."
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# Build the project
print_color $BLUE "Building MongoLauncher..."
mvn clean package -DskipTests -f "$PROJECT_DIR/pom.xml"

if [[ ! -f "$SCRIPT_DIR/mongo-launcher.jar" ]]; then
    print_color $RED "Error: Build failed - JAR file not found"
    exit 1
fi

print_color $GREEN "✓ Build successful"

# Create platform-specific packages
print_color $BLUE "Creating distribution packages..."

# 1. Universal package (cross-platform)
UNIVERSAL_DIR="$DIST_DIR/${PROJECT_NAME}-${VERSION}-universal"
mkdir -p "$UNIVERSAL_DIR"

# Copy core files
cp "$SCRIPT_DIR/mongo-launcher.jar" "$UNIVERSAL_DIR/"
cp "$SCRIPT_DIR/install.sh" "$UNIVERSAL_DIR/"
cp "$SCRIPT_DIR/install.bat" "$UNIVERSAL_DIR/"
cp "$PROJECT_DIR/INSTALL.md" "$UNIVERSAL_DIR/"
cp "$PROJECT_DIR/README.md" "$UNIVERSAL_DIR/" 2>/dev/null || echo "# MongoLauncher" > "$UNIVERSAL_DIR/README.md"

# Create simple launcher scripts
cat > "$UNIVERSAL_DIR/mongo-launcher" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$SCRIPT_DIR/mongo-launcher.jar" "$@"
EOF

cat > "$UNIVERSAL_DIR/mongo-launcher.bat" << 'EOF'
@echo off
set SCRIPT_DIR=%~dp0
java -jar "%SCRIPT_DIR%mongo-launcher.jar" %*
EOF

chmod +x "$UNIVERSAL_DIR/mongo-launcher"
chmod +x "$UNIVERSAL_DIR/install.sh"

# Create archive
print_color $BLUE "Creating universal archive..."
(cd "$DIST_DIR" && tar -czf "${PROJECT_NAME}-${VERSION}-universal.tar.gz" "${PROJECT_NAME}-${VERSION}-universal")
(cd "$DIST_DIR" && zip -r "${PROJECT_NAME}-${VERSION}-universal.zip" "${PROJECT_NAME}-${VERSION}-universal" > /dev/null)

print_color $GREEN "✓ Universal package created"

# 2. macOS package
MACOS_DIR="$DIST_DIR/${PROJECT_NAME}-${VERSION}-macos"
mkdir -p "$MACOS_DIR"

cp "$SCRIPT_DIR/mongo-launcher.jar" "$MACOS_DIR/"
cp "$SCRIPT_DIR/install.sh" "$MACOS_DIR/"
cp "$PROJECT_DIR/INSTALL.md" "$MACOS_DIR/"
cp "$PROJECT_DIR/homebrew/mongo-launcher.rb" "$MACOS_DIR/" 2>/dev/null || true

cat > "$MACOS_DIR/mongo-launcher" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$SCRIPT_DIR/mongo-launcher.jar" "$@"
EOF

chmod +x "$MACOS_DIR/mongo-launcher"
chmod +x "$MACOS_DIR/install.sh"

(cd "$DIST_DIR" && tar -czf "${PROJECT_NAME}-${VERSION}-macos.tar.gz" "${PROJECT_NAME}-${VERSION}-macos")

print_color $GREEN "✓ macOS package created"

# 3. Linux package
LINUX_DIR="$DIST_DIR/${PROJECT_NAME}-${VERSION}-linux"
mkdir -p "$LINUX_DIR"

cp "$SCRIPT_DIR/mongo-launcher.jar" "$LINUX_DIR/"
cp "$SCRIPT_DIR/install.sh" "$LINUX_DIR/"
cp "$PROJECT_DIR/INSTALL.md" "$LINUX_DIR/"

cat > "$LINUX_DIR/mongo-launcher" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec java -jar "$SCRIPT_DIR/mongo-launcher.jar" "$@"
EOF

chmod +x "$LINUX_DIR/mongo-launcher"
chmod +x "$LINUX_DIR/install.sh"

(cd "$DIST_DIR" && tar -czf "${PROJECT_NAME}-${VERSION}-linux.tar.gz" "${PROJECT_NAME}-${VERSION}-linux")

print_color $GREEN "✓ Linux package created"

# 4. Windows package
WINDOWS_DIR="$DIST_DIR/${PROJECT_NAME}-${VERSION}-windows"
mkdir -p "$WINDOWS_DIR"

cp "$SCRIPT_DIR/mongo-launcher.jar" "$WINDOWS_DIR/"
cp "$SCRIPT_DIR/install.bat" "$WINDOWS_DIR/"
cp "$PROJECT_DIR/INSTALL.md" "$WINDOWS_DIR/"

cat > "$WINDOWS_DIR/mongo-launcher.bat" << 'EOF'
@echo off
set SCRIPT_DIR=%~dp0
java -jar "%SCRIPT_DIR%mongo-launcher.jar" %*
EOF

(cd "$DIST_DIR" && zip -r "${PROJECT_NAME}-${VERSION}-windows.zip" "${PROJECT_NAME}-${VERSION}-windows" > /dev/null)

print_color $GREEN "✓ Windows package created"

# Generate checksums
print_color $BLUE "Generating checksums..."
(cd "$DIST_DIR" && sha256sum *.tar.gz *.zip > checksums.sha256 2>/dev/null || shasum -a 256 *.tar.gz *.zip > checksums.sha256)

print_color $GREEN "✓ Checksums generated"

# Summary
echo
print_color $GREEN "Distribution packages created successfully!"
print_color $BLUE "Location: $DIST_DIR"
echo

print_color $BLUE "Available packages:"
ls -la "$DIST_DIR"/*.tar.gz "$DIST_DIR"/*.zip

echo
print_color $BLUE "Package contents:"
echo "• mongo-launcher.jar - Main executable JAR"
echo "• install.sh/.bat - Automated installer scripts" 
echo "• mongo-launcher/mongo-launcher.bat - Portable launcher scripts"
echo "• INSTALL.md - Installation documentation"
echo "• checksums.sha256 - Package verification checksums"

echo
print_color $YELLOW "Next steps:"
echo "1. Test the packages on target platforms"
echo "2. Update GitHub release with these artifacts"
echo "3. Update Homebrew formula with correct SHA256"
echo "4. Publish to package managers as needed"

print_color $GREEN "Packaging complete!"