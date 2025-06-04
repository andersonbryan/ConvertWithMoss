---
title: The ConvertWithMoss Manual
author: 
- Jürgen Moßgraber
header-includes:
    \usepackage{fancyhdr}
    \pagestyle{fancy}
geometry: margin=2.5cm
papersize: a4paper
date:   \today
...

<div style="page-break-after: always; visibility: hidden"> 
\pagebreak 
</div>

# Welcome to the ConvertWithMoss Documentation

This tool converts multisamples in a [specific source format to a different destination format](README-FORMATS.md#). Furthermore, it can create multisample files from plain AIFF and WAV files.

Additionally, the conversion process reads and writes metadata (name, category, creator, description and keywords) as well as envelopes and filter settings, if supported by the format. If the source format does not support metadata a guessing algorithm is applied based on the samples names.

Details about the specific converted parameters can be found in a [spreadsheet][1].

## Installation

[Download][2] and run the matching installer for your operating system.
After that you can start the application ConvertWithMoss.

> **Note macOS**
>
> Read the [macOS installation specifics](README-MACOS.md#) for important notices!

## Build from sources

Ensure to have the required `JVM`, `JRE`, `JavaFX` and `Maven` dependencies preinstalled and set the `JAVA_HOME` environment variable to specify which Java version to use; the minimum required version is `21`. Then use `mvn install` command to start the build.

See also the various build scripts in this directory as references on how to build the documentation and/or the application source files.

For Linux (BSD not tested) there is also a `Makefile` for build and install with the usual `make` and `make install` commands.

## Usage

1. Select the source format on the left. Select a source folder, which contains one or multiple folders with multisamples in the selected source format. The files can also be located in sub-folders.
2. Select the destination format. Select the output folder where you want to create the multisamples. This folder must be empty. You can add a non-existing folder to the name, which then is automatically created. E.g. you could select the Desktop and then add a folder *Conversions*.
3. Press the *Convert* button to start the conversion. The progress is shown with notification messages in the log area, which you should check for potential errors like defect source files, skipped folder, etc.

Alternatively, press *Analyse* to execute the same process as *Convert* but does not write any files. Use this to check for errors before finally running the conversion.

### Options

* **Renaming**: Allows to rename multi-samples. Enable the checkbox to use this feature. If enabled select the file which contains the mapped names. The file is a simple text file in UTF-8 format (important if non-ASCII characters are used!). Each row contains one mapping. A mapping consists of 2 names separated either by ';' or ','. E.g. a row which contains "AcPno;Acoustic Piano" would name a multi-sample with the name "AcPno" into "Acoustic Piano" as output.
* **Create folder structure**: If enabled, sub-folders from the source folder are created as well in the output folder. For example, if I select my whole "Sounds" folder, there are sub-folders like `Sounds\07 Synth\Lead\01W Emerson'70 Samples`. In that case the output folder would contain e.g. `07 Synth\Lead\01W Emerson'70.multisample` if Bitwig multisample is selected as the destination format.
* **Add new files**: Starts the conversion even if the output folder is not empty but only adds files which are not already present.
* **Dark Mode**: Toggles the user interface between a light and dark layout.

[1]: https://github.com/git-moss/ConvertWithMoss/blob/main/documentation/SupportedFeaturesSampleFormats.ods
[2]: https://mossgrabers.de/Software/ConvertWithMoss/ConvertWithMoss.html
