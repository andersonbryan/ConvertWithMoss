# Supported Formats

The following multi-sample formats are supported:

* [AIFF](#aiff)
* [Ableton Sampler](#ableton-sampler)
* [Akai MPC Keygroups / Drum](#akai-mpc-keygroups--drum)
* [CWITEC TX16Wx](#cwitec-tx16wx)
* [DecentSampler](#decentsampler)
* [Kontakt NKI/NKM](#kontakt-nkinkm)
* [Expert Sleepers disting EX](#expert-sleepers-disting-ex)
* [Korg KMP/KSF](#korg-kmpksf)
* [Korg wavestate/modwave](#korg-wavestatemodwave)
* [Logic EXS24](#logic-exs24)
* [Multisample Format (Bitwig Studio, Presonus Studio One)](#multisample-format-bitwig-studio-presonus-studio-one)
* [Propellerhead Reason NN-XT](#propellerhead-reason-nn-xt)
* [Sample files (AIFF, FLAC, NCW, OGG, WAV)](#sample-files-aiff-flac-ncw-ogg-wav)
* [SFZ](#sfz)
* [SoundFont 2](#soundfont-2)
* [TAL Sampler](#tal-sampler)
* [Waldorf Quantum MkI,MkII/Iridium/Iridium Core](#waldorf-quantum-mki-mkii--iridium--iridium-core)
* [Yamaha YSFC](#yamaha-ysfc)

## Automatic Metadata detection

If a source format does not support metadata like author, category or additional tags, ConvertWithMoss can detect from the name and path of the file. The following settings can be used to tweak the detection process.

## WAV Chunk Information

If the format uses WAV files to store the samples, there are the following options to additionally write metadata information to the respective chunks:

* Broadcast Audio Metadata: This can contain a description text, the creator of the sample and the creation date and time.
* Instrument: Contains the root note, fine tuning, gain, the key range and the velocity range.
* Sample: Contains the root note, fine tuning and loop points.
* Remove JUNK, junk, FLLR and MD5 chunks: Enable this option to drop these chunks. Junk and filler chunks are only for aligning the following chunks to certain data positions. The MD5 chunk contains a checksum which is currently not updated and therefore should be dropped.

### Metadata Detection Options

* Prefer folder name: If enabled the name of the multi-sample will be extracted from the folder instead of the sample names.
* Default creator: The name which is set as the creator of the multi-samples, if no creator tag could be found.
* Creator tag(s): Here you can set a number of creator names, which need to be separated by comma. You can also use this to look up other things. For example, I set the names of the synthesizers which I sampled. My string looks like: "01W,FM8,Pro-53,Virus B,XV" (without the quotes).

## 1010music blackbox, tangerine, bitbox

This format is simply called a *preset*. A preset contains 16 slots and each slot can either contain a simple sample or a complex multi-sample. All presets need to be placed in the *Presets* folder on the SD-card. The main information of a preset is stored in a file which is always called *preset.xml*. This file is located in a folder with the name of the preset (eg. /Presets/MyLovelyPiano/preset.xml).

The related samples can be anywhere on the SD-card, if referenced accordingly in the preset.xml file. But to make the handling easier, the output of this tool puts all sample files in the same folder as the preset.xml file. Therefore, only one folder needs to be copied to the Presets folder on the SD-card.

There are the same options as with [WAV files](#wav-files) to write the different chunk information. It is suggested to leave them all enabled.

If the format is selected as the source, there are two things to consider:

* One or multiple slots contain a multi-sample: for each of the multi-samples a file in the destination format is created.
* All slots contain only single samples: one file in the destination format is created which combines all 16 slots. The notes are from 36 upwards if not configured differently in the preset.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Destination Options

* Option to set the *Interpolation Quality*. Setting it to *High* requires a bit more processing power on the 1010music devices.
* Option to trim sample to range of zone start to end. Since the format does not support a sample start attribute for multi-sample, this fixes the issue.
* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## AIFF

The Audio Interchange File Format (Audio IFF) provides a standard for storing sampled sounds. The format is quite flexible, allowing for the storage of monaural or multichannel sampled sounds at a variety of sample rates and sample widths. Audio IFF conforms to the "EA IFF 85" Standard for Interchange Format Files developed by Electronic Arts.

If AIFF is selected as the source format, all AIFF files located in the same folder are considered as a part of one multi-sample. You can also select a top folder. If you do so, all sub-folders are checked for potential multi-sample folders.

The applied algorithm and configuration options are the same as when WAV files are used as the source. See the [WAV section](#wav-files) for details.

## Ableton Sampler

Ableton uses a generic preset format (*.adv) for all of their devices. For combined rack presets another format (*.adg) is used. All their formats are XML documents which are compressed with the open GZIP algorithm.

ConvertWithMoss can extract Sampler and Simpler presets from ADV files as well as all instances of Sampler or Simpler in ADG files when selected as a source. The presets from the Ableton libraries cannot be extracted since their AIFF files use a proprietary encryption algorithm. It writes ADV files as the destination.

ADV files and their samples need to be placed in the Ableton user library in the correct folders to allow Ableton to open it. Therefore, ConvertWithMoss creates the necessary folder structure which can be simply copied to the user library. If the source has sub-folders the global option *Create folder structure* should be deactivated otherwise it can be quite tedious to collect all the results files with their additional Ableton sub-folder structure.

## Akai MPC Keygroups / Drum

A MPC Keygroup or MPC Drum setup is stored in a folder. It contains a description file (.xpm) and the sample files (.WAV). Both keygroup and drum types are supported.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

Other restrictions are:

* A round robin keygroup can only contain up to 4 layers (groups). An error is displayed in this case but the file is converted anyway.
* Only 128 keygroups are allowed. An error is displayed in this case but the file is written anyway but might not be loadable.

### Destination Options

* Limit layers to: MPC Firmware 3.4 increased the number of possible layers in a keygroup to 8. This option allows you to choose between 4 (for older firmware revisions) or 8.
* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## CWITEC TX16Wx

TX16Wx is a free sampler plug-in available for Windows and MacOS. TX16Wx Professional is the commercial expansion of TX16Wx. It adds some advanced features like effects, signal routing or trigger switching. But the free version is already very powerful and covers all of the features that ConvertWithMoss supports.

The format uses a XML format and keeps the samples separate.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## DecentSampler

The Decent Sampler plugin is a free (but closed source) sample player plugin that allows you to play sample libraries in the DecentSampler format (files with extensions: dspreset and dslibrary). See https://www.decentsamples.com/product/decent-sampler-plugin/
The format specification is available here: https://www.decentsamples.com/wp-content/uploads/2020/06/format-documentation.html#the-sample-element

A dspreset file contains a single preset and the description of the multi-sample. The related samples are normally kept in a separate folder. WAV and FLAC files are supported. A dslibrary file contains several dspreset files including the samples compressed in ZIP format. A dsbundle is similar but uncompressed.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Source Options

* Log unused XML elements and attributes: If enabled the XML elements and attributes which are not used in the translation process are logged.

### Destination Options

* Output Format - Create Bundle: Choose to create a bundle (instead of single presets or a library).
* Make monophonic: Restricts the sound to 1 note, use e.g. for lead sounds.
* Templates and resources folder: Allows to modify the UI and effects section of the presets (see below).
* Options to write/update [WAV Chunk Information](#wav-chunk-information).

If no 'Templates and resources folder' is configured the default templates are used which create a reverb effect with 2 knob controls for the reverb mix and reverb time.
To modify these templates, first create an empty folder somewhere on your disc. Select this folder in the 'Templates and resources folder' field. Then click on the button 'Create templates in the selected folder'. This copies the 2 templates ('ui.xml' and 'effects.xml') into this folder. You can copy additional images and documentation files to the folder as well. These resource files will be added to the output as well. These templates will be applied to all created dspresets. But note that you can have multiple templates if you use several template folders which can then be switched for each conversion run.

## Kontakt NKI/NKM

Kontakt is a sampler from Native Instruments which uses a plethora of file formats which all are sadly proprietary and therefore no documentation is publicly available. Nevertheless, several people analyzed the format and by now sufficient information is available to provide the support as the source.

However, the format changed many times across the different Kontakt versions. So far, the following formats are known and supported as a source:

| Kontakt Version |
|:----------------|
| 1               |
| 1.5             |
| 2 - 4.1.x       |
| 4.2.2+          |
| 5 - 7           |

A NKI file contains one instrument which is a multi-sample with many parameters. Currently, the usual multi-sample parameters are supported incl. loops. Furthermore, metadata information, the amplitude, pitch and filter cutoff envelope, filter parameters as well as pitchbend.
(Most) NCW encoded sample files can be read as well.
A NKM file contains up to 64 instruments and is supported as well as a source.

Encrypted files are not supported.

If selected as a destination, a NKI file is written and all samples are placed in a sub-folder with the same name. 

### Destination Options

* Currently, only the Kontakt 1 format is supported which sadly does not contain any metadata information.
* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Expert Sleepers disting EX

The disting EX is a multi-function Eurorack module which provides many different algorithms. On of them is the SD Multisample algorithm which is an eight voice polyphonic, three part multi-timbral, sample playback instrument, playing WAV files from the MicroSD card. It can have up to 3 input CV/gate pairs, or can be played via MIDI or I2C. It supports both velocity switches and round robins per sample.
The basic multi-sample setup is encoded in the file-names of the samples. Further information like the amplitude envelope are stored in a preset (*.dexpreset). The preset references only the name of the folder which contains the related samples. All samples in the folder considered to be belonging to the multi-sample.

### Destination Options

* 'Re-sample to 16bit/44.1kHz': If enabled, samples will be resampled to 16bit and 44.1kHz. While the device can play higher resolutions as well it decrease the number of voices it can play.
* Option to trim sample to range of zone start to end. Since the format does not support a sample start attribute, this fixes the issue.
* Options to write/update [WAV Chunk Information](#wav-chunk-information). Writing the Sample chunk is important since the disting EX reads the loop information from it.

## Korg KMP/KSF

The KMP/KSF format (*.KMP) was first introduced in the Korg Trinity workstation (1995) and since then supported in many Korg workstations and entertainment keyboards up to the latest Korg Nautilus (2020). The following keyboards are known to support the format:

* Trinity
* Triton
* OASYS
* M3
* Kronos
* KROSS (only for pads)
* PA1X/PA800/PA2X/PA3X/PA4X
* Nautilus

The format is documented in detail (more or less) in the appendix of the respective parameter guides. The KMP format contains only 1 group of a multi-sample, which means there are only key splits but no groups. The file references several KSF files which contain the sample data for each key region.

Since the KMP format can only contain 1 group of a multi-sample, sources with multiple groups can be optionally split into several destination KMP files. Due to limitations of the format only uncompressed 8 or 16 bit samples up to 48kHz are supported. Files in other formats are automatically converted.

Even if the KSF files can store stereo files, they do not work. Therefore, they need to be split into 2 KMP files. To ease the use of these 2 files an additional KSC file is created, which loads all referenced files.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Destination Options

* Write group KMPs: Writes a KMP for each group in the source multi-sample. This option will be ignored for split stereo source files.
* Enable the +12dB option: Increases the volume of each sample by +12dB. Use for low volume samples.
* Set sample volume to +99: If enabled, sets all sample volumes to +99. Use for very low volume samples.

## Korg wavestate/modwave

The korgmultisample format is currently used by the Korg wavestate and modwave keyboards as well as their VST plugin siblings. Files in that format (*.korgmultisample) can be opened with the Korg Sample Builder software and transferred to the keyboard.

Since the format is pretty simple all data stored in the file is available for the conversion.

Since the format supports only one group of a multi-sample, multiple destination files are created for each group available in the source. If there is more than one group in the source the name of the created file has the velocity range of the group added. Using that information a multi-sample with up to 4 groups can be created as a Performance in the device.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Logic EXS24

The Logic EXS24 format is a proprietary sample format used by Logic Pro, a digital audio workstation. It is primarily used for storing and playback of sampled instruments and sounds within Logic Pro. The format allows for comprehensive mapping and editing of samples, as well as providing various modulation and performance options.

The format only stores absolute paths to the sample files. Therefore, the easiest way to make the converter find the sample files is to place them in the same folder as the EXS file. If it cannot be found in this folder the sample file is searched recursively starting from a number of levels up from the source folder of the EXS. *The number of folders can be configured*.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Multisample Format (Bitwig Studio, Presonus Studio One)

This open format is currently supported by the stock sampler in Bitwig Studio and Presonus Studio One. A multisample file is a zip archive which contains all samples in WAV format and a metadata file in XML format.
It supports multiple groups, key and velocity crossfades as well as several metadata information: creator, sound category and keywords.

The parser supports all information from the format except the group color and select parameters 1 to 3, which are not mappable.

This converter supports (split) stereo uncompressed and IEEE float 32 bit formats for the WAV files.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Propellerhead Reason NN-XT

The Propellerhead Reason NN-XT is a software sampler that is included in the Reason software package. Reason is a digital audio workstation (DAW) software developed by Propellerhead Software. It allows users to load and play back sampled sounds, such as instruments or drum hits. The file ending is *sxt*.

There are metadata fields for creator and a creator URL. However, additional information like a category is retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

## SFZ

"The SFZ format is a file format to define how a collection of samples are arranged for performance. The goal behind the SFZ format is to provide a free, simple, minimalistic and expandable format to arrange, distribute and use audio samples with the highest possible quality and the highest possible performance flexibility" (cited from https://sfzformat.com/).

The SFZ file contains only the description of the multi-sample. The related samples are normally kept in a separate folder. The converter supports samples in WAV, OGG and FLAC format.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Source Options

* Log unsupported SFZ opcodes: If enabled, opcodes which are found in the source but are not used (not supported) as input for the conversion are logged.

### Destination Options

* Convert to FLAC format: If enabled, the sample files are converted to FLAC.
* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## SoundFont 2

The original SoundFont file format was developed in the early 1990s by E-mu Systems and Creative Labs. It was first used on the Sound Blaster AWE32 sound card for its General MIDI support.

A SoundFont can contain several presets grouped into banks. Presets refer to one or more instruments which are distributed over a keyboard by key and velocity ranges. The sample data contained in the file is in mono or split stereo with 16 or 24 bit.

The conversion process creates one destination file for each preset found in a SoundFont file. The mono files are combined into stereo files. If the left and right channel mono samples contain different loops, the loop of the left channel is used.

There are metadata fields for creator and some description specified in the format. However, additional information like a category is retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Source Options

* Prefix with file name: If enabled, the name of the Soundfont file is added to all resulting destination files.
* Prefix with program number: If enabled, the preset number of the preset is added to the resulting destination file.

## TAL Sampler

TAL-Sampler is an analog modeled synthesizer with a sampler engine as the sound source, including a modulation matrix and self-oscillating filters. Most of the presets in it's library store the sample files in an encrypted format (*.wavsmpl), this format is not supported. Only presets using plain WAV or AIFF files are supported.

Choosing TAL Sampler as the destination format, creates a *talsmpl*
file and stores all samples in a sub-folder by the same name. The samples of the source groups are distributed across the 4 layers of TAL Sampler in such a way that the key and velocity splits do not overlap. This is a workaround for the fact that TAL Sampler does not support overlapping samples. Since groups have only the name and trigger type as attributes, which are not supported in TAL Sampler anyway, this should work in most cases. If there are still overlapping samples a warning is displayed.

There are no metadata fields (category, creator, etc.) specified in the format. Therefore, information is stored and retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Waldorf Quantum MkI, MkII / Iridium / Iridium Core

This family of Waldorf synthesizers supports the playback of multi-samples. One preset can contain 2 layers. A layer is a complete preset in itself and simply concatenates 2 single presets. Each preset can have up to 3 oscillators of which each oscillator can contain its own multi-sample.
If this format is used as the source it produces 1 or 2 output presets, one for each layer. If used as the destination format, each group of the source multi-sample is applied to one of the 3 oscillators. If the source contains more than 3 groups, all zones of the additional groups are added to the multi-sample of the 3rd oscillator.

### Destination Options

* Re-sample to 16bit/44.1kHz: If enabled, samples will be resampled to 16bit and 44.1kHz. While the device can play higher resolutions as well it might impact the performance.
* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Sample files (AIFF, FLAC, NCW, OGG, WAV)

If WAV is selected as the source format, all WAV files located in the same folder are considered as a part of one multi-sample. You can also select a top folder. If you do so, all sub-folders are checked for potential multi-sample folders.

First, all WAV files of a folder are checked if they contain instrument chunks. If this is the case they are used to create the layout of the multi-sample (range and velocity splits as well as gain and pitch settings). If no such information is available a clever algorithm tries to detect the necessary key range and velocity information from the names of the WAV files. Furthermore, the algorithm tries to detect as much metadata as possible from the WAV files:

* Notes are first detected from the sample chunk. If none is present, different parser settings are applied on the file name to detect a note name (or MIDI note value).
* A category is extracted from the file name as well based on a list of several synonyms and abbreviations (e.g. Solo as a synonym for Lead). If this fails the same logic is applied to the folder names (e.g. you might have sorted your lead sounds in a folder called *Lead*).
* Characterizations like *hard* are extracted as well with a similar algorithm as for the category.

Metadata information is retrieved from Broadcast Audio Extension chunks in the WAV files. If no such chunks are present an [automatic detection](#automatic-metadata-detection) is applied.

### Groups

Detected groups will be equally distributed across the velocity range. E.g. if 2 groups are detected the first will be mapped to the velocity range of 0-63 and the second to 64-127.

* Detection pattern: Comma separated list of patterns to detect groups. The pattern must contain a star character ("*"), which indicates the position which contains the group number.
* Order of group numbering: Enable to map groups inversed. This means that the highest number will be mapped to the lowest velocity range.

### Mono Splits

WAV file can contain different sample formats. This converter supports (split) stereo uncompressed and IEEE float 32 bit formats. Only WAV files in Mono or Stereo are supported. Stereo samples might be split up into 2 mono files (the left and right channel). This tool will combine them into a stereo file.

* Left channel detection pattern: Comma separated list of patterns to detect the left channel from the filename. E.g. "_L".

### Source Options

* Crossfade notes: You can automatically create crossfades between the different note ranges. This makes especially sense if you only sampled a couple of notes. Set the number of notes, which should be cross-faded between two samples (0-127). If you set a too high number the crossfade is automatically limited to the maximum number of notes between the two neighboring samples.
* Crossfade velocities: You can automatically create crossfades between the different groups. This makes especially sense if you sampled several sample groups with different velocity values. Set the number of velocity steps (0-127), which should be crossfaded between two samples. If you set a too high number the crossfade is automatically limited to the maximum number of velocity steps between the two neighbouring samples.
* Post-fix text to remove: The algorithm automatically removes the note information to extract the name of the multi-sample but there might be further text at the end of the name, which you might want to remove. For example the multi-samples I created with SampleRobot have a group information like "_ms0_0". You can set a comma separated list of such postfix texts in that field.

### Destination Options

* Options to write/update [WAV Chunk Information](#wav-chunk-information)

## Yamaha YSFC

This format is used in many Yamaha Workstation. While the format is the same, the content is different.

### Using it as the source format

The following file formats are supported as a source:

* Motif XS: X0A, X0W
* Motif XF: X3A, X3W
* MOXF: X6A, X6W
* Montage: X7A, X7L, X7U with Performance Data
* MODX/MODX+: X8A, X8L, X8U with Performance Data
* Montage M: Y2U, Y2L

The wave files in professional Yamaha libraries are often compressed. Such files are not supported. Furthermore, only self-contained libraries (= libraries which do not reference samples in other libraries) are supported.

So far, reading of Performances is only supported for Montage (not Montage M!) and MODX/MODX+ files. This means that for all other formats only the basic multi-sample data is converted (no filter and envelopes data is converted).

### Using it as the destination format

Currently, the user and library formats of the Montage (not Montage M!) and MODX/MODX+ are available as the destination format. The backup formats X7A and X8A are supported only as a source. The structure is as follows (bottom-up):

* A key-group references several samples which form a multi-sample setup (key-/velocity ranges)
* An element references 1 key-group (adds synthesis parameters)
* A part can contain up to 8 elements (this forms already a complex setup with layers and ranges)
* A performance can contain up to 16 parts (e.g. to perform a song by muting, soloing parts via scenes)
* A library contains several performances

When creating presets or libraries as the destination type, each multi-sample source creates one performance with one active part. Each group of the the multi-sample source is assigned to 1 element for which 1 key-group is created as well which contains the samples. If there are more groups than elements, the remaining groups are all added to the last element. If there are no groups all samples will be assigned to key-group/element one.

When creating (ConvertWithMoss) performances as the destination type, each performance source creates one (Yamaha) performance with one active part for each multi-sample (instrument) of the source. If there are more than 16 instruments in the source they are ignored. The rest of the mapping is identical to creating presets/libraries.

Note: There are no checks that the created libraries stay in the boundaries of the workstation specifications (e.g. the number of the maximum allowed samples or the required memory size)!

### Source Options

* Create multi-samples for: 
  * Waveforms: this reads only the raw-multi-sample(s) without additional Performance information. Use this option as well if the Performances do not reference all multi-samples in the library/user-bank.
  * Performances: Only supported for Montage and MODX/MODX+ files and when Performance data is present in the file. Will create a multi-sample source for each Performance.

### Destination Options

* Library Format: Chooses the output format which is created.
* Create only Waveforms: No Performances will be written. Only Waveform data.
