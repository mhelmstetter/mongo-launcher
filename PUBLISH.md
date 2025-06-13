# Publishing MongoLauncher to GitHub

## Pre-Publication Checklist

### ✅ Files Ready for GitHub
- [x] `.gitignore` - Properly configured for Maven, IDE, and OS files
- [x] `LICENSE` - Apache 2.0 license
- [x] `README.md` - Comprehensive documentation
- [x] `INSTALL.md` - Detailed installation guide
- [x] `CHANGELOG.md` - Version history and changes
- [x] `.github/workflows/build-and-release.yml` - CI/CD automation

### ✅ Project Structure
```
mongo-launcher/
├── .github/workflows/          # GitHub Actions
├── bin/                        # Distribution tools
├── homebrew/                   # Homebrew formula
├── src/main/java/             # Source code
├── .gitignore                 # Git ignore rules
├── CHANGELOG.md               # Version history
├── INSTALL.md                 # Installation guide
├── LICENSE                    # Apache 2.0 license
├── pom.xml                    # Maven configuration
├── PUBLISH.md                 # This file
└── README.md                  # Project documentation
```

## Step-by-Step Publishing Guide

### 1. Create GitHub Repository
```bash
# Go to github.com and create a new repository named "mongo-launcher"
# Choose public/private as needed
# Do NOT initialize with README, .gitignore, or license (we have our own)
```

### 2. Initialize and Push
```bash
cd /Users/mh/git/mongo-launcher

# Initialize git (if not already done)
git init

# Add all files
git add .

# Initial commit
git commit -m "Initial release of MongoLauncher v1.0.0

Features:
- Interactive CLI with Claude-like prompting
- Cross-platform configuration management
- Atlas and local cluster support
- Automated installers for Mac/Linux/Windows
- Homebrew support
- Complete CI/CD pipeline"

# Add remote (replace with your actual repository URL)
git remote add origin https://github.com/yourusername/mongo-launcher.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### 3. Create First Release
```bash
# Tag the first release
git tag -a v1.0.0 -m "MongoLauncher v1.0.0

Initial release with interactive CLI, configuration management, 
and cross-platform installers."

# Push tags to trigger release workflow
git push origin v1.0.0
```

### 4. Verify GitHub Actions
- Go to your repository on GitHub
- Check the "Actions" tab to ensure CI/CD is working
- Verify that the release was created automatically

### 5. Update Repository Settings
- Add repository description: "MongoDB Cluster Management Tool with Interactive CLI"
- Add topics/tags: `mongodb`, `cli`, `cluster-management`, `atlas`, `java`
- Set up branch protection rules for `main` branch (optional)

### 6. Homebrew Tap (Optional)
If you want to publish to Homebrew:

```bash
# Create a separate repository for Homebrew tap
# Repository name should be: homebrew-mongo-launcher

# Copy the formula
cp homebrew/mongo-launcher.rb /path/to/homebrew-mongo-launcher/mongo-launcher.rb

# Update formula with correct URLs and SHA256
# Commit and push to homebrew tap repository
```

## Post-Publication Tasks

### 1. Update Documentation URLs
After publishing, update any placeholder URLs in:
- `README.md` installation instructions
- `INSTALL.md` download links
- Homebrew formula URLs
- GitHub Actions workflow

### 2. Test Installation
Test the various installation methods:
```bash
# Test direct install script
curl -L -o install.sh https://github.com/yourusername/mongo-launcher/releases/download/v1.0.0/install.sh
chmod +x install.sh && ./install.sh

# Test package downloads
curl -L -O https://github.com/yourusername/mongo-launcher/releases/download/v1.0.0/mongo-launcher-1.0.0-universal.tar.gz
```

### 3. Monitor and Respond
- Watch for GitHub issues
- Monitor download statistics
- Respond to community feedback
- Plan future releases

## Security Considerations

- [ ] Review all files for sensitive information
- [ ] Ensure no API keys or credentials are committed
- [ ] Consider enabling security scanning on GitHub
- [ ] Set up dependabot for dependency updates

## Marketing and Communication

- [ ] Write blog post about the release
- [ ] Share on relevant MongoDB community channels
- [ ] Submit to relevant package managers
- [ ] Create documentation site (optional)

## Future Release Process

For future releases:

1. Update version in `pom.xml`
2. Update `CHANGELOG.md` with new changes
3. Commit changes: `git commit -m "Prepare v1.1.0"`
4. Create tag: `git tag -a v1.1.0 -m "Release v1.1.0"`
5. Push: `git push origin main && git push origin v1.1.0`
6. GitHub Actions will automatically create the release

## Resources

- [GitHub Repository Best Practices](https://docs.github.com/en/repositories)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Homebrew Formula Cookbook](https://docs.brew.sh/Formula-Cookbook)
- [Semantic Versioning](https://semver.org/)

---

**Ready to publish!** 🚀