<div align="center">

<img src="icons/convertwithmoss.png" alt="ConvertWithMoss" height="96" />

# ConvertWithMoss

*Universal audio multisample converter*

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)](#)
[![Java Version](https://img.shields.io/badge/Java-24%2B-blue?style=flat-square)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-orange?style=flat-square)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-GPLv3-yellow?style=flat-square)](LICENSE)

[Features](#features) • [Getting Started](#getting-started) • [Supported Formats](#supported-formats) • [Usage](#usage) • [Building](#building)

![ConvertWithMoss Interface](documentation/images/screenshot.png)

</div>

ConvertWithMoss is a powerful multisample converter that transforms audio sample libraries between different formats. Whether you're working with hardware samplers, software instruments, or building sample libraries, ConvertWithMoss bridges the gap between incompatible formats while preserving metadata, envelopes, and filter settings.

> [!NOTE]
> This tool converts multisamples from one format to another, including support for plain sample files like AIFF, FLAC, and WAV. It also intelligently detects metadata from file names and paths when the source format doesn't include metadata tags.

## Features

- **Wide Format Support** - Convert between 20+ multisample formats including Kontakt, Bitwig, SFZ, SoundFont 2, and more
- **Metadata Preservation** - Maintains sample names, categories, creators, descriptions, and keywords across formats
- **Intelligent Detection** - Automatically extracts metadata from file names and Broadcast Audio Extension chunks when not explicitly defined
- **Parameter Conversion** - Transfers envelopes, filter settings, and other parameters between compatible formats
- **Batch Processing** - Process entire folders with automatic sub-folder handling and duplicate management
- **Flexible Interfaces** - Use the JavaFX GUI or command-line interface for automation and scripting
- **Library Creation** - Generate preset libraries and performances from multiple source files
- **Format Recording** - Analyze sample files without writing output to verify conversion compatibility

## Getting Started

### Installation

Download and run the installer for your operating system from the [ConvertWithMoss website](https://mossgrabers.de/Software/ConvertWithMoss/ConvertWithMoss.html).

**Supported Platforms:**
- macOS 13+ (Intel) / macOS 14+ (ARM)
- Windows 10+
- Linux (Debian-based distributions)

#### macOS Security Notes

After installation, macOS may display security warnings. To resolve:

1. Attempt to run the application
2. Open **System Settings** → **Privacy & Security**
3. Scroll to the bottom and click **Open Anyway** next to the ConvertWithMoss message
4. Confirm you want to run the application

Alternative method if the above doesn't work:

```sh
cd /Applications/ConvertWithMoss.app
sudo xattr -rc .
```

### Quick Start

1. **Select source format** - Choose the format of your input files (left panel)
2. **Choose source folder** - Navigate to the folder containing your multisamples
3. **Select destination format** - Pick the output format (right panel)
4. **Choose output folder** - Set where converted files should be saved
5. **Select output type** - Choose between single presets, libraries, or performances
6. **Convert** - Click the Convert button to start the process

The conversion log displays progress and any errors encountered during processing.

## Supported Formats

ConvertWithMoss supports conversion between the following formats:

| Format | Read | Write | Notes |
|--------|:----:|:-----:|-------|
| 1010music (blackbox/tangerine/bitbox) | ✓ | ✓ | Preset XML format |
| Ableton Sampler | ✓ | ✓ | ADV/ADG formats |
| Akai MPC Keygroups/Drum | ✓ | ✓ | Keygroup programs |
| Apple Logic EXS24 | ✓ | ✓ | EXS instrument format |
| Bitwig Multisample | ✓ | ✓ | Native Bitwig format |
| CWITEC TX16Wx | ✓ | ✓ | TX16Wx sampler format |
| DecentSampler | ✓ | ✓ | Open sampler format |
| Expert Sleepers disting EX | ✓ | ✓ | Eurorack module format |
| Korg KSC/KMP/KSF | ✓ | ✓ | Korg sample library formats |
| Korg wavestate/modwave | ✓ | ✓ | Korg synthesizer formats |
| Native Instruments Kontakt | ✓ | ✓ | NKI/NKM formats |
| Native Instruments Maschine | ✓ | ✓ | Maschine group format |
| Propellerhead Reason NN-XT | ✓ | ✓ | NN-XT patch format |
| Sample files | ✓ | ✓ | AIFF, FLAC, NCW, OGG, WAV |
| SFZ | ✓ | ✓ | SFZ sampler format |
| SoundFont 2 | ✓ | ✓ | SF2 standard |
| TAL Sampler | ✓ | ✓ | TAL Software sampler |
| Waldorf Quantum/Iridium | ✓ | ✓ | Waldorf synthesizer formats |
| Yamaha YSFC | ✓ | ✓ | Yamaha sample format |

For detailed format-specific options and limitations, see [README-FORMATS.md](documentation/README-FORMATS.md).

## Usage

### GUI Application

Launch ConvertWithMoss from your applications folder or start menu.

**Common Options:**

- **Renaming** - Apply custom naming rules via CSV mapping file (format: `OldName;NewName`)
- **Folder Structure** - Preserve source sub-folder hierarchy in output
- **Add New Files** - Append to existing output folder without overwriting
- **Dark Mode** - Toggle between light and dark interface themes

**Output Types:**

- **Single Presets** - One file per source multisample (default)
- **Library** - Combine all sources into a single library file
- **Performance** - Create performance presets with multiple instruments
- **Performance Library** - Collection of multiple performances

> [!TIP]
> Use the **Analyse** button to preview conversion without writing files. This helps identify issues before processing large sample libraries.

### Command Line Interface

The CLI provides automation capabilities for batch processing and integration into workflows.

**Basic Usage:**

```bash
ConvertWithMoss -s <source> -d <destination> <source_folder> <output_folder>
```

**Example:**

```bash
ConvertWithMoss -s nki -d bitwig /path/to/kontakt/library /path/to/output
```

**Available Options:**

```
-s, --source <format>        Source format
-d, --destination <format>   Destination format
-t, --type <type>           Output type: 'preset' or 'performance'
-l, --library <name>        Create library with specified name
-r, --rename <file>         CSV file for automatic renaming
-f, --flat                  Don't recreate folder structure
-a, --analyze               Analyze only, don't write files
-h, --help                  Show help message
-V, --version               Show version information
```

**Windows Users:** Use `ConvertWithMossCLI.exe` for command-line operations.

## Building

### Prerequisites

- **Java 24+** (JDK with JavaFX support)
- **Maven 3.8.1+**
- **Git**

### Build Steps

**Clone and build:**

```bash
git clone https://github.com/git-moss/ConvertWithMoss.git
cd ConvertWithMoss
mvn clean package
```

**Run from source:**

```bash
mvn javafx:run
```

**Create native installers:**

```bash
# macOS
./jpackage-mac.sh

# Linux
./jpackage-linux.sh

# Windows
jpackage-win.cmd
```

Installers will be created in the `target/release` directory.

**Linux users:** Use the provided Makefile:

```bash
make        # Build project
make install # Install to system
```

### Project Structure

```
ConvertWithMoss/
├── src/main/java/
│   └── de/mossgrabers/convertwithmoss/
│       ├── core/          # Core conversion logic
│       ├── format/        # Format-specific adapters
│       ├── file/          # File format handlers
│       └── ui/            # JavaFX user interface
├── src/main/resources/    # Application resources
├── documentation/         # User manuals and guides
├── icons/                # Application icons
└── pom.xml               # Maven configuration
```

## Resources

- **Website** - [mossgrabers.de/Software/ConvertWithMoss](https://mossgrabers.de/Software/ConvertWithMoss/ConvertWithMoss.html)
- **Documentation** - [User Manual](documentation/README.md)
- **Format Details** - [Supported Formats Guide](documentation/README-FORMATS.md)
- **Changelog** - [Release History](documentation/CHANGELOG.md)
- **Feature Matrix** - [Supported Features Spreadsheet](https://github.com/git-moss/ConvertWithMoss/blob/main/documentation/SupportedFeaturesSampleFormats.ods)

## Support

For bug reports and questions:

- **GitHub Issues** - [Create an issue](https://github.com/git-moss/ConvertWithMoss/issues)
- **KVR Forum** - [ConvertWithMoss thread](https://www.kvraudio.com/)

Please review the documentation and existing issues before posting.

## Author

**Jürgen Moßgraber**
- Website: [mossgrabers.de](http://www.mossgrabers.de)
- GitHub: [@git-moss](https://github.com/git-moss)

---

<div align="center">

Made with dedication by the ConvertWithMoss community

[⬆ back to top](#convertwithmoss)

</div>
