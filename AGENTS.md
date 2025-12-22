# AGENTS.md

## Project Overview

ConvertWithMoss is a universal audio multisample converter built with Java 24+, JavaFX 25, and Maven. It converts audio sample libraries between 20+ different hardware and software sampler formats while preserving metadata, envelopes, and filter settings.

**Architecture:**
- **Modular Java application** (`de.mossgrabers.convertwithmoss` module)
- **JavaFX GUI** for interactive conversion
- **CLI interface** using picocli for automation
- **Format adapters** following detector/creator pattern for each supported format
- **File handlers** for low-level format reading/writing (RIFF, WAV, AIFF, etc.)

**Key Technologies:**
- Java 24+ (modular)
- JavaFX 25 (UI framework)
- Maven 3.8.1+ (build tool)
- picocli 4.7.7 (CLI framework)
- jpackage-maven-plugin 1.7.1 (native installers)

## Setup Commands

### Prerequisites

- **Java 24+** (JDK with JavaFX support)
- **Maven 3.8.1+**
- **Git**

### Clone and Setup

```bash
# Clone the repository
git clone https://github.com/git-moss/ConvertWithMoss.git
cd ConvertWithMoss

# Install dependencies and compile
mvn clean install
```

### Environment Variables

```bash
# Set JAVA_HOME to Java 24+ installation
export JAVA_HOME=/path/to/java24

# Optional: Increase memory for large builds
export MAVEN_OPTS="-Xmx4G"
export JAVA_TOOL_OPTIONS="-Xmx4G"
```

## Development Workflow

### Running the Application

```bash
# Run via JavaFX plugin (recommended for development)
mvn javafx:run

# Build and run standalone JAR
mvn clean package
java -jar target/lib/convertwithmoss-15.1.0.jar

# Run with increased memory for large sample libraries
java -Xmx64G -jar target/lib/convertwithmoss-15.1.0.jar
```

### Hot Reload / Development Mode

JavaFX doesn't have automatic hot reload. After code changes:

```bash
# Quick rebuild without tests
mvn clean package -DskipTests

# Then run again
mvn javafx:run
```

### Project Structure

```
src/main/java/de/mossgrabers/convertwithmoss/
├── core/                      # Core conversion logic
│   ├── creator/              # Format creators (write)
│   ├── detector/             # Format detectors (read)
│   ├── model/                # Data models
│   └── settings/             # Configuration
├── format/                    # Format adapters (20+ formats)
│   ├── akai/                 # Akai MPC
│   ├── bitwig/               # Bitwig Multisample
│   ├── ni/kontakt/           # Native Instruments Kontakt
│   ├── sfz/                  # SFZ format
│   ├── wav/                  # WAV sample files
│   └── ...                   # Other formats
├── file/                      # Low-level file handlers
│   ├── riff/                 # RIFF container
│   ├── wav/                  # WAV files
│   ├── aiff/                 # AIFF files
│   └── ...                   # Other file types
├── ui/                        # JavaFX user interface
│   └── ConvertWithMossApp.java  # Main application entry
└── exception/                 # Custom exceptions

src/main/resources/
├── Strings.properties         # Localization strings
├── css/                       # Stylesheets (dark/light mode)
├── images/                    # UI icons and images
└── templates/                 # Format-specific templates
    ├── nki/                  # Kontakt templates
    ├── adv/                  # Ableton templates
    └── ...                   # Other format templates
```

## Testing Instructions

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn clean test

# Skip tests during build
mvn package -DskipTests
```

### Test Structure

**Note:** This project currently has minimal automated tests. Tests should be added in:

```
src/test/java/de/mossgrabers/convertwithmoss/
├── core/                      # Core logic tests
├── format/                    # Format-specific tests
└── file/                      # File handler tests
```

### Manual Testing

For format conversion testing:

1. Use the **Analyse** button in GUI to validate without writing files
2. Test with small sample libraries first
3. Check conversion logs for errors
4. Verify output files in target format application

## Code Style Guidelines

### Java Conventions

Follow the Java style guide defined in `.github/instructions/java.instructions.md`:

- **Records**: Use Java Records for data-only classes (DTOs, immutable structures)
- **Pattern Matching**: Use pattern matching for `instanceof` and `switch` expressions
- **Type Inference**: Use `var` for local variables when type is clear
- **Immutability**: Make classes and fields `final` where possible
- **Streams and Lambdas**: Prefer Streams API for collection processing
- **Null Handling**: Use `Optional<T>` instead of returning/accepting `null`

### Naming Conventions

- **Classes/Interfaces**: `UpperCamelCase` (e.g., `BitwigCreator`, `IMultisampleSource`)
- **Methods/Variables**: `lowerCamelCase` (e.g., `createMultisample`, `sampleRate`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_SAMPLE_RATE`, `MAX_VOICES`)
- **Packages**: `lowercase` (e.g., `de.mossgrabers.convertwithmoss.format.bitwig`)

### Module System

The project uses Java modules. When adding new packages:

1. Export public APIs in `src/main/java/module-info.java`
2. Open resource packages for reflection access
3. Declare required modules

Example:
```java
exports de.mossgrabers.convertwithmoss.format.newformat;
opens de.mossgrabers.convertwithmoss.templates.newformat;
```

### Adding a New Format

To add support for a new sampler format:

1. **Create format package**: `src/main/java/de/mossgrabers/convertwithmoss/format/newformat/`
2. **Implement detector**: Extend detector interfaces from `core/detector/`
3. **Implement creator**: Extend creator interfaces from `core/creator/`
4. **Add constants**: Create constants file for format-specific values
5. **Add UI classes**: Create UI classes for format-specific options (DetectorUI, CreatorUI)
6. **Register format**: Add to format registry (if applicable)
7. **Update module-info.java**: Export/open new packages
8. **Add resources**: Place templates in `src/main/resources/templates/newformat/`

Example structure:
```
format/newformat/
├── NewFormatDetector.java      # Reads the format
├── NewFormatDetectorUI.java    # UI options for reading
├── NewFormatCreator.java       # Writes the format
├── NewFormatCreatorUI.java     # UI options for writing
├── NewFormatConstants.java     # Format-specific constants
└── NewFormatFile.java          # File format handler
```

## Build and Deployment

### Standard Build

```bash
# Full clean build with dependencies
mvn clean package

# Build output locations:
# - target/lib/                   # JAR files and dependencies
# - target/classes/               # Compiled classes
```

### Native Installers

Create platform-specific installers using jpackage:

```bash
# macOS (requires macOS)
./jpackage-mac.sh
# Output: target/release/*.dmg

# Linux (requires Linux)
./jpackage-linux.sh
# Output: target/release/*.deb

# Windows (requires Windows)
jpackage-win.cmd
# Output: target/release/*.exe
```

**Alternative:** Use Maven directly:

```bash
mvn jpackage:jpackage
```

### Linux Installation (Makefile)

For Debian-based Linux distributions:

```bash
# Build and install
make
sudo make install

# Clean build artifacts
make clean

# Installation locations:
# - /usr/local/share/ConvertWithMoss/  # Application files
# - /usr/local/bin/convertwithmoss     # Launcher script
# - /usr/local/share/applications/     # Desktop entry
# - /usr/local/share/pixmaps/          # Icon
```

### Dependency Management

**Local Maven Repository:**
- Location: `maven-local-repository/`
- Contains: `uitools-2.0.1.jar` (custom UI library)
- **Do not delete** this folder - required for offline builds

**Checking for Updates:**

```bash
# Check for outdated dependencies
mvn versions:display-dependency-updates

# Check for outdated plugins
mvn versions:display-plugin-updates
```

## CLI Usage for Agents

The CLI is ideal for batch processing and automation:

```bash
# Basic conversion
java -jar target/lib/convertwithmoss-15.1.0.jar \
  -s kontakt -d bitwig \
  /path/to/source/folder \
  /path/to/output/folder

# Analyze only (no output files)
java -jar target/lib/convertwithmoss-15.1.0.jar \
  -a -s kontakt -d bitwig \
  /path/to/source/folder \
  /path/to/output/folder

# Create library
java -jar target/lib/convertwithmoss-15.1.0.jar \
  -s kontakt -d bitwig \
  -l "My Sample Library" \
  /path/to/source/folder \
  /path/to/output/folder

# With custom naming map
java -jar target/lib/convertwithmoss-15.1.0.jar \
  -s kontakt -d bitwig \
  -r mapping.csv \
  /path/to/source/folder \
  /path/to/output/folder

# Flat output (no folder structure)
java -jar target/lib/convertwithmoss-15.1.0.jar \
  -f -s kontakt -d bitwig \
  /path/to/source/folder \
  /path/to/output/folder
```

### Available CLI Options

```
-s, --source <format>        Source format (required)
-d, --destination <format>   Destination format (required)
-a, --analyze                Analyze only, don't write files
-t, --type <type>           Output type: 'preset' or 'performance'
-l, --library <name>        Create library with specified name
-r, --rename <file>         CSV file for automatic renaming
-f, --flat                  Don't recreate folder structure
-h, --help                  Show help message
-V, --version               Show version information
```

### Supported Formats

Source/destination format codes:
- `1010music` - 1010music blackbox/tangerine/bitbox
- `ableton` - Ableton Sampler
- `akai` - Akai MPC Keygroups
- `bitwig` - Bitwig Multisample
- `decentsampler` - DecentSampler
- `disting` - Expert Sleepers disting EX
- `exs` - Apple Logic EXS24
- `kmp` - Korg KMP/KSC/KSF
- `korgmultisample` - Korg wavestate/modwave
- `nki` - Native Instruments Kontakt
- `maschine` - Native Instruments Maschine
- `nnxt` - Propellerhead Reason NN-XT
- `samplefile` - Sample files (AIFF, FLAC, WAV, OGG)
- `sfz` - SFZ format
- `sf2` - SoundFont 2
- `tal` - TAL Sampler
- `tx16wx` - CWITEC TX16Wx
- `waldorf` - Waldorf Quantum/Iridium
- `ysfc` - Yamaha YSFC

## Security Considerations

### Snyk Integration

The project has Snyk security scanning configured:

- **Rule**: Always run `snyk_code_scan` for new first-party code
- **Location**: `.github/instructions/snyk_rules.instructions.md`
- **Process**:
  1. Generate new code
  2. Run Snyk code scan
  3. Fix any security issues found
  4. Rescan to verify fixes
  5. Repeat until clean

### Sensitive Data

- No credentials or API keys should be committed
- Sample file paths may contain user information - handle carefully
- Conversion logs may contain file paths - sanitize before sharing

## Troubleshooting

### Common Issues

**Out of Memory Errors:**
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx4G"
mvn clean package

# Or for running the app
java -Xmx64G -jar target/lib/convertwithmoss-15.1.0.jar
```

**JavaFX Module Issues:**
```bash
# Ensure JavaFX is available
mvn dependency:tree | grep javafx

# Try forcing JavaFX download
mvn clean install -U
```

**Missing uitools Dependency:**
```bash
# Verify local repo exists
ls -la maven-local-repository/de/mossgrabers/uitools/

# If missing, build artifacts are incomplete
# Contact project maintainer
```

**macOS "App is damaged" Error:**
```bash
# Remove quarantine attribute
cd /Applications/ConvertWithMoss.app
sudo xattr -rc .
```

**Build Fails on Module Compilation:**
- Check Java version: `java --version` (must be 24+)
- Update JAVA_HOME: `export JAVA_HOME=/path/to/java24`
- Clean Maven cache: `mvn clean install -U`

### Debug Logging

Enable verbose logging:

```bash
# Set Java logging level
export JAVA_TOOL_OPTIONS="-Djava.util.logging.config.file=logging.properties"

# Or run with Maven debug
mvn -X javafx:run
```

## Pull Request Guidelines

### Before Submitting

```bash
# 1. Build and verify compilation
mvn clean package

# 2. Run tests
mvn test

# 3. Check for dependency updates
mvn versions:display-dependency-updates

# 4. Run security scan (if Snyk configured)
# snyk code test
```

### PR Requirements

- **Title Format**: `[Component] Brief description`
  - Examples: `[Bitwig] Add envelope support`, `[Core] Fix metadata detection`, `[UI] Update dark mode colors`
- **Code Style**: Follow Java conventions in `.github/instructions/java.instructions.md`
- **Module Updates**: Update `module-info.java` if adding new packages
- **Documentation**: Update format documentation if adding/changing formats
- **Testing**: Add or update tests for changed code

### Commit Messages

- Use conventional commit format:
  - `feat(bitwig): add envelope support`
  - `fix(core): correct metadata detection`
  - `docs(readme): update format table`
  - `refactor(ui): simplify dialog creation`

## Additional Notes

### Key Files Reference

- **`pom.xml`** - Maven build configuration, dependency versions, jpackage settings
- **`module-info.java`** - Java module definition, exports, and requires
- **`src/main/resources/Strings.properties`** - UI text and localization
- **`.github/copilot-instructions.md`** - AI agent context (more detailed)
- **`.github/instructions/java.instructions.md`** - Java coding standards
- **`.github/instructions/snyk_rules.instructions.md`** - Security scanning rules

### Main Class Property

The main entry class is configured via the `main.class` property in `pom.xml`:
```xml
<main.class>de.mossgrabers.convertwithmoss.ui.ConvertWithMossApp</main.class>
```

When changing the entry point, update this property and ensure `module-info.java` exports the package.

### Performance Considerations

- Large sample libraries may require 32-64GB heap size
- jpackage installer is configured with `-Xmx64G` by default
- Use the "Analyse" feature before full conversion on very large libraries
- Consider batch processing in smaller chunks for huge collections

### Format-Specific Notes

Each format has unique characteristics - consult these resources:

- **Format Details**: `documentation/README-FORMATS.md`
- **Feature Matrix**: `documentation/SupportedFeaturesSampleFormats.ods`
- **Format Package**: `src/main/java/de/mossgrabers/convertwithmoss/format/<format>/`
- **Templates**: `src/main/resources/templates/<format>/`

### Version Information

- Current version: `15.1.0` (defined in `pom.xml`)
- Java requirement: `24+` (module system with pattern matching)
- JavaFX version: `25.0.1`
- Maven requirement: `3.8.1+`

---

For more detailed information, refer to:
- **User Manual**: `documentation/README.md`
- **Changelog**: `documentation/CHANGELOG.md`
- **GitHub Issues**: https://github.com/git-moss/ConvertWithMoss/issues

