// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.nki.type.kontakt2;

import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.INotifier;
import de.mossgrabers.convertwithmoss.exception.ParseException;
import de.mossgrabers.convertwithmoss.file.CompressionUtils;
import de.mossgrabers.convertwithmoss.file.FastLZ;
import de.mossgrabers.convertwithmoss.file.StreamUtils;
import de.mossgrabers.convertwithmoss.file.wav.WaveFile;
import de.mossgrabers.convertwithmoss.format.TagDetector;
import de.mossgrabers.convertwithmoss.format.nki.SoundinfoDocument;
import de.mossgrabers.convertwithmoss.format.nki.type.AbstractKontaktType;
import de.mossgrabers.convertwithmoss.format.nki.type.kontakt2.monolith.Dictionary;
import de.mossgrabers.convertwithmoss.format.nki.type.kontakt2.monolith.DictionaryItem;
import de.mossgrabers.convertwithmoss.format.nki.type.kontakt2.monolith.DictionaryItemReferenceType;
import de.mossgrabers.convertwithmoss.format.nki.type.nicontainer.chunkdata.PresetDataChunkData;
import de.mossgrabers.convertwithmoss.format.wav.WavSampleMetadata;
import de.mossgrabers.convertwithmoss.ui.IMetadataConfig;
import de.mossgrabers.tools.ui.Functions;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;


/**
 * Can handle NKI files in Kontakt 2 format including monolith. But only WAV files are supported.
 *
 * @author Jürgen Moßgraber
 */
public class Kontakt2Type extends AbstractKontaktType
{
    private static final String               NULL_ENTRY        = "(null)";
    private static final int                  HEADER_KONTAKT_42 = 0x110;

    private static final Set<String>          KNOWN_BLOCK_IDS   = new HashSet<> ();
    private static final Map<Integer, String> ICON_MAP          = new HashMap<> ();
    static
    {
        KNOWN_BLOCK_IDS.add ("Kon2"); // Kontakt 2
        KNOWN_BLOCK_IDS.add ("Kon3"); // Kontakt 3
        KNOWN_BLOCK_IDS.add ("Kon4"); // Kontakt 4
        KNOWN_BLOCK_IDS.add ("AkPi"); // Akustik Piano from Kontakt 3 Library
        KNOWN_BLOCK_IDS.add ("ElPi"); // Elektrik Piano from Kontakt 3 Library

        ICON_MAP.put (Integer.valueOf (0x00), "Organ");
        ICON_MAP.put (Integer.valueOf (0x01), "Cello");
        ICON_MAP.put (Integer.valueOf (0x02), "Drum Kit");
        ICON_MAP.put (Integer.valueOf (0x03), "Bell");
        ICON_MAP.put (Integer.valueOf (0x04), "Trumpet");
        ICON_MAP.put (Integer.valueOf (0x05), "Guitar");
        ICON_MAP.put (Integer.valueOf (0x06), "Piano");
        ICON_MAP.put (Integer.valueOf (0x07), "Marimba");
        ICON_MAP.put (Integer.valueOf (0x08), "Record Player");
        ICON_MAP.put (Integer.valueOf (0x09), "E-Piano");
        ICON_MAP.put (Integer.valueOf (0x0A), "Drum Pads");
        ICON_MAP.put (Integer.valueOf (0x0B), "Bass Guitar");
        ICON_MAP.put (Integer.valueOf (0x0C), "Electric Guitar");
        ICON_MAP.put (Integer.valueOf (0x0D), "Wave");
        ICON_MAP.put (Integer.valueOf (0x0E), "Asian Symbol");
        ICON_MAP.put (Integer.valueOf (0x0F), "Flute");
        ICON_MAP.put (Integer.valueOf (0x10), "Speaker");
        ICON_MAP.put (Integer.valueOf (0x11), "Score");
        ICON_MAP.put (Integer.valueOf (0x12), "Conga");
        ICON_MAP.put (Integer.valueOf (0x13), "Pipe Organ");
        ICON_MAP.put (Integer.valueOf (0x14), "FX");
        ICON_MAP.put (Integer.valueOf (0x15), "Computer");
        ICON_MAP.put (Integer.valueOf (0x16), "Violin");
        ICON_MAP.put (Integer.valueOf (0x17), "Surround");
        ICON_MAP.put (Integer.valueOf (0x18), "Synthesizer");
        ICON_MAP.put (Integer.valueOf (0x19), "Microphone");
        ICON_MAP.put (Integer.valueOf (0x1A), "Oboe");
        ICON_MAP.put (Integer.valueOf (0x1B), "Saxophone");
        ICON_MAP.put (Integer.valueOf (0x1C), "New");
    }

    private static final byte []        FILE_HEADER_ID        =
    {
        (byte) 0x12,
        (byte) 0x90,
        (byte) 0xA8,
        (byte) 0x7F
    };

    private static final byte []        SAMPLE_DATA_HEADER_ID =
    {
        (byte) 0x0A,
        (byte) 0xF8,
        (byte) 0xCC,
        (byte) 0x16
    };

    private static final byte []        SOUNDINFO_HEADER      =
    {
        (byte) 0xAE,
        (byte) 0xE1,
        (byte) 0x0E,
        (byte) 0xB0,
        (byte) 0x01,
        (byte) 0x01,
        (byte) 0x0C,
        (byte) 0x00,
        (byte) 0xD9,
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0x00
    };

    private final SimpleDateFormat      simpleDateFormatter   = new SimpleDateFormat ("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
    private final boolean               isBigEndian;
    private final K2MetadataFileHandler handler;
    private final PresetDataChunkData   kontakt5Preset        = new PresetDataChunkData ();


    /**
     * Constructor.
     *
     * @param metadataConfig Default metadata
     * @param notifier Where to report errors
     * @param isBigEndian Larger bytes are first, other wise smaller bytes are first (little-endian)
     */
    public Kontakt2Type (final IMetadataConfig metadataConfig, final INotifier notifier, final boolean isBigEndian)
    {
        super (metadataConfig, notifier);

        this.isBigEndian = isBigEndian;
        this.handler = new K2MetadataFileHandler (notifier);
        this.simpleDateFormatter.setTimeZone (TimeZone.getTimeZone ("UTC+1"));
    }


    /** {@inheritDoc} */
    @Override
    public List<IMultisampleSource> readNKI (final File sourceFolder, final File sourceFile, final RandomAccessFile fileAccess) throws IOException
    {
        // The size of the ZLIB block, we do not need the info
        final int zlibLength = StreamUtils.readUnsigned32 (fileAccess, this.isBigEndian);

        // 0x100 = Kontakt 2, 0x110 = Kontakt 4.2
        final int headerVersion = StreamUtils.readUnsigned16 (fileAccess, this.isBigEndian);

        // Skip Patch Version and Patch Type
        StreamUtils.skipNBytes (fileAccess, 6);

        // The version of Kontakt which stored this file
        String kontaktVersion = this.readVersion (fileAccess);

        final String blockID = StreamUtils.readASCII (fileAccess, 4, !this.isBigEndian);
        if (!KNOWN_BLOCK_IDS.contains (blockID))
            this.notifier.log ("IDS_NKI_UNKNOWN_BLOCK_ID", blockID);

        final Date creation = StreamUtils.readTimestamp (fileAccess, this.isBigEndian);
        final String formattedCreation = this.simpleDateFormatter.format (creation);

        // No idea yet about these 4 bytes...
        StreamUtils.skipNBytes (fileAccess, 4);

        // Number of Zones
        StreamUtils.readUnsigned16 (fileAccess, this.isBigEndian);
        // Number of Groups
        StreamUtils.readUnsigned16 (fileAccess, this.isBigEndian);
        // Number of Instruments
        StreamUtils.readUnsigned16 (fileAccess, this.isBigEndian);

        // No idea yet about these 16 bytes...
        StreamUtils.skipNBytes (fileAccess, 16);

        final Integer iconID = Integer.valueOf (StreamUtils.readUnsigned32 (fileAccess, this.isBigEndian));
        final String iconName = ICON_MAP.get (iconID);
        if (iconName == null)
            this.notifier.logError ("IDS_NKI_UNKNOWN_ICON_ID", iconID.toString ());
        // 8 characters, null terminated
        final String author = StreamUtils.readASCII (fileAccess, 9, StandardCharsets.ISO_8859_1).trim ();

        // No idea yet about these 2 bytes... Found once in monolith with IR Samples and Wallpaper
        final int unknown = StreamUtils.readUnsigned16 (fileAccess, this.isBigEndian);
        if (unknown != 0)
            this.notifier.logError ("IDS_NKI_UNKNOWN_NOT_NULL", Integer.toString (unknown));

        String website = StreamUtils.readASCII (fileAccess, 87).trim ();
        if (website.isBlank () || NULL_ENTRY.equals (website))
            website = null;

        // No idea yet about these 6 bytes... could be padded zeros
        StreamUtils.skipNBytes (fileAccess, 6);

        final boolean isFourDotTwo = headerVersion == HEADER_KONTAKT_42;
        if (headerVersion == HEADER_KONTAKT_42)
        {
            // 12 new bytes introduced in 4.2
            StreamUtils.skipNBytes (fileAccess, 12);
        }

        // Skip the checksum
        StreamUtils.skipNBytes (fileAccess, 4);

        final int patchLevel = StreamUtils.readUnsigned32 (fileAccess, this.isBigEndian);
        if (kontaktVersion.endsWith ("?"))
            kontaktVersion = kontaktVersion.substring (0, kontaktVersion.length () - 1) + Integer.toString (patchLevel);

        int decompressedLength = 0;
        if (isFourDotTwo)
        {
            // Unknown
            StreamUtils.readUnsigned32 (fileAccess, this.isBigEndian);

            decompressedLength = StreamUtils.readUnsigned32 (fileAccess, this.isBigEndian);

            // Padding?
            StreamUtils.skipNBytes (fileAccess, 32);
        }

        // Is it a monolith?
        final int type = fileAccess.read ();
        final boolean isMonolith = type != 0x78 && !isFourDotTwo || type != 0x0A && isFourDotTwo;
        fileAccess.seek (fileAccess.getFilePointer () - 1);
        this.notifier.log ("IDS_NKI_FOUND_KONTAKT_TYPE", isFourDotTwo ? "4.2" : "2", kontaktVersion, isMonolith ? " - monolith" : "", this.isBigEndian ? "Big-Endian" : "Little-Endian");
        final Map<String, WavSampleMetadata> monolithSamples = isMonolith ? this.readMonolith (fileAccess) : null;

        final List<IMultisampleSource> multiSamples;
        if (isFourDotTwo)
        {
            final byte [] compressedData = new byte [zlibLength];
            fileAccess.readFully (compressedData);
            final byte [] uncompressedData = FastLZ.uncompress (compressedData, decompressedLength);
            this.kontakt5Preset.parsePresetChunks (uncompressedData);
            // TODO Implement parsing of multi-samples
            this.notifier.logError ("IDS_NKI_KONTAKT422_NOT_SUPPORTED");
            multiSamples = Collections.emptyList ();
        }
        else
            multiSamples = this.handleZLIB (sourceFolder, sourceFile, fileAccess, monolithSamples);

        this.handleSoundinfo (sourceFile, fileAccess, multiSamples, formattedCreation, iconName, author, website);
        return multiSamples;
    }


    /** {@inheritDoc} */
    @Override
    public void writeNKI (final OutputStream out, final String safeSampleFolderName, final IMultisampleSource multisampleSource, final int sizeOfSamples) throws IOException
    {
        final Optional<String> result = this.handler.create (safeSampleFolderName, multisampleSource);
        if (result.isEmpty ())
            throw new IOException (Functions.getMessage ("IDS_NKI_NO_XML"));
        final ByteArrayOutputStream bout = new ByteArrayOutputStream ();
        CompressionUtils.writeZLIB (bout, result.get (), 1);
        final byte [] zlibContent = bout.toByteArray ();

        out.write (FILE_HEADER_ID);
        StreamUtils.writeUnsigned32 (out, zlibContent.length, false);

        // Since we still do not understand how to calculate the checksum, go with a static header
        // with no metadata at all --> this does not work since e.g. the number of zones/groups
        // needs to be set
        out.write (Functions.rawFileFor ("de/mossgrabers/convertwithmoss/templates/nki/Kontakt2_Static_Header.bin"));

        out.write (zlibContent);

        out.write (SOUNDINFO_HEADER);
        final SoundinfoDocument soundinfoDocument = new SoundinfoDocument (multisampleSource.getCreator (), multisampleSource.getCategory ());
        out.write (soundinfoDocument.createDocument (multisampleSource.getName ()).getBytes (StandardCharsets.UTF_8));
    }


    /**
     * Handles the ZLIB section with the contained XML document.
     *
     * @param sourceFolder The top source folder for the detection
     * @param sourceFile The source file which contains the XML document
     * @param fileAccess The random access file to read from
     * @param monolithSamples The samples that are contained in the NKI monolith otherwise null
     * @return All parsed multi-samples
     * @throws IOException
     */
    private List<IMultisampleSource> handleZLIB (final File sourceFolder, final File sourceFile, final RandomAccessFile fileAccess, final Map<String, WavSampleMetadata> monolithSamples) throws IOException
    {
        final String xmlCode = CompressionUtils.readZLIB (fileAccess);

        try
        {
            return this.handler.parse (sourceFolder, sourceFile, xmlCode, this.metadataConfig, monolithSamples);
        }
        catch (final UnsupportedEncodingException ex)
        {
            this.notifier.logError ("IDS_NOTIFY_ERR_ILLEGAL_CHARACTER", ex);
        }
        catch (final IOException ex)
        {
            this.notifier.logError ("IDS_NOTIFY_ERR_LOAD_FILE", ex);
        }

        return Collections.emptyList ();
    }


    /**
     * Read the sound info block after the ZLIB block.
     *
     * @param sourceFile The source file which contains the XML document
     * @param fileAccess The random access file to read from
     * @param multiSamples
     * @param formattedCreation The formatted creation date/time
     * @param iconName The descriptive name of the icon
     * @param author The author of the multi-sample
     * @param website The web site of the author
     */
    private void handleSoundinfo (final File sourceFile, final RandomAccessFile fileAccess, final List<IMultisampleSource> multiSamples, final String formattedCreation, final String iconName, final String author, final String website)
    {
        try
        {
            final int numOfPendingbytes = (int) (sourceFile.length () - fileAccess.getFilePointer ());
            final SoundinfoDocument soundinfo = this.readSoundinfo (fileAccess, numOfPendingbytes, iconName, author);
            updateMetadata (multiSamples, formattedCreation, website, soundinfo);
        }
        catch (final IOException ex)
        {
            this.notifier.logError ("IDS_NOTIFY_ERR_LOAD_FILE", ex);
        }
    }


    /**
     * Read and parse the sound info block.
     *
     * @param fileAccess The random access file to read from
     * @param numOfPendingbytes The number of bytes not yet handled by the ZLIB de-compressor
     * @param iconName The descriptive name of the icon
     * @param author The author of the multi-sample
     * @return The parsed sound info document
     * @throws IOException
     */
    private SoundinfoDocument readSoundinfo (final RandomAccessFile fileAccess, final int numOfPendingbytes, final String iconName, final String author) throws IOException
    {
        if (numOfPendingbytes > 0)
        {
            // Unknown so far, checksum of ZLIB?
            StreamUtils.skipNBytes (fileAccess, 12);

            final byte [] rest = new byte [numOfPendingbytes - 12];
            fileAccess.readFully (rest);

            final String soundinfoXML = new String (rest, StandardCharsets.UTF_8);
            try
            {
                final SoundinfoDocument soundinfo = new SoundinfoDocument (soundinfoXML);
                final Set<String> categories = soundinfo.getCategories ();
                if (categories.isEmpty ())
                    categories.add (iconName);
                return soundinfo;
            }
            catch (final SAXException ex)
            {
                this.notifier.logError ("IDS_NKI_UNSOUND_SOUNDINFO", ex);
            }
        }
        return new SoundinfoDocument (author, iconName);
    }


    /**
     * Reads and parses the monolith block.
     *
     * @param fileAccess The random access file to read from
     * @return All sample descriptors of the sample block
     * @throws IOException Error reading the block
     */
    private Map<String, WavSampleMetadata> readMonolith (final RandomAccessFile fileAccess) throws IOException
    {
        final Dictionary dictionary = new Dictionary (fileAccess, this.isBigEndian);
        final int nkiPointer = getNKIPointer (dictionary);
        if (nkiPointer == -1)
            throw new IOException (Functions.getMessage ("IDS_NKI_DICT_NKI_NOT_FOUND"));

        final Dictionary samplesDictionary = getSamplesDictionary (dictionary);
        final Map<String, WavSampleMetadata> result = this.handleSampleDictionary (fileAccess, samplesDictionary, nkiPointer);

        // Move to the beginning of the ZLIB block
        fileAccess.seek (nkiPointer + 27L + 170L);
        return result;
    }


    /**
     * Lookup the pointer to the beginning of the NKI block.
     *
     * @param dictionary The dictionary
     * @return The pointer position or -1 if not found
     */
    private static int getNKIPointer (final Dictionary dictionary)
    {
        for (final DictionaryItem item: dictionary.getItems ())
        {
            if (item.getReferenceType () == DictionaryItemReferenceType.NKI)
                return item.getPointer ();
        }
        return -1;
    }


    /**
     * Get the (sub-) dictionary with the samples information.
     *
     * @param dictionary The top dictionary
     * @return The samples dictionary
     * @throws IOException Found an unexpected dictionary
     */
    private static Dictionary getSamplesDictionary (final Dictionary dictionary) throws IOException
    {
        for (final DictionaryItem item: dictionary.getItems ())
        {
            if (item.getReferenceType () == DictionaryItemReferenceType.DICTIONARY)
            {
                if (!"Samples".equals (item.asWideString ()))
                    throw new IOException (Functions.getMessage ("IDS_NKI_UNEXPECTED_DICT_ITEM", item.asWideString ()));
                return item.getDictionary ();
            }
        }
        throw new IOException (Functions.getMessage ("IDS_NKI_DICT_SAMPLES_NOT_FOUND"));
    }


    /**
     * Reads all sample file names from the sample dictionary. After that all samples are extracted
     * as well.
     *
     * @param fileAccess The file
     * @param dictionary The Sample dictionary
     * @param nkiPointer The start of the NKI section
     * @return The found samples
     * @throws IOException
     */
    private Map<String, WavSampleMetadata> handleSampleDictionary (final RandomAccessFile fileAccess, final Dictionary dictionary, final int nkiPointer) throws IOException
    {
        final List<String> sampleFilenames = new ArrayList<> ();

        for (final DictionaryItem item: dictionary.getItems ())
        {
            final DictionaryItemReferenceType referenceType = item.getReferenceType ();
            switch (referenceType)
            {
                case DICTIONARY:
                    this.notifier.log ("IDS_NKI_DICT_IGNORED", item.asWideString ());
                    break;

                case SAMPLE:
                    final String sampleFilename = item.asWideString ();
                    sampleFilenames.add (sampleFilename);
                    break;

                case END:
                    // Ignore
                    break;

                default:
                    throw new IOException (Functions.getMessage ("IDS_NKI_UNEXPECTED_DICT_ITEM", referenceType.toString ()));
            }
        }

        return readSamples (fileAccess, nkiPointer, sampleFilenames);
    }


    /**
     * Reads all sample files from the monolith. Note: this is a brute force attempt which scans for
     * the sample headers backwards in the file since we still have no idea how to read the sample
     * positions from the file. AIFF is not supported.
     *
     * @param fileAccess The random access file to read from
     * @param nkiPointer The pointer where NKI starts
     * @param sampleFilenames The names of all samples
     * @return The read and parsed WAV files
     * @throws IOException An error occurred
     */
    private static Map<String, WavSampleMetadata> readSamples (final RandomAccessFile fileAccess, final int nkiPointer, final List<String> sampleFilenames) throws IOException
    {
        final int numSamples = sampleFilenames.size ();
        final List<Long> positions = collectSampleHeaders (fileAccess, nkiPointer, SAMPLE_DATA_HEADER_ID, numSamples);
        if (numSamples != positions.size ())
            throw new IOException (Functions.getMessage ("IDS_NKI_NUMBER_OF_SAMPLES_NOT_MATCHING"));

        final Map<String, WavSampleMetadata> sampleMetadataMap = new HashMap<> (numSamples);
        for (int i = 0; i < numSamples; i++)
        {
            final long position = positions.get (i).longValue ();
            // Move to start and skip header
            fileAccess.seek (31 + position);

            final WaveFile wavFile = new WaveFile ();
            final InputStream is = Channels.newInputStream (fileAccess.getChannel ());
            try
            {
                wavFile.read (is, true);
            }
            catch (final ParseException ex)
            {
                throw new IOException (ex);
            }
            final WavSampleMetadata wavSampleMetadata = new WavSampleMetadata (wavFile);
            final String sampleFilename = sampleFilenames.get (i);
            wavSampleMetadata.setCombinedName (sampleFilename);
            sampleMetadataMap.put (sampleFilename, wavSampleMetadata);
        }

        return sampleMetadataMap;
    }


    /**
     * Finds all occurrences of the given array in the file up to the given limit. Search starts at
     * the end of the file.
     *
     * @param fileAccess The file in which to search
     * @param startPointer The start position of the search + 1
     * @param searchBytes The array to look for
     * @param numOccurrences Stops after this number of occurrences has been found
     * @return A list with all occurrences starting with the lowest position
     * @throws IOException An error occurred
     */
    private static List<Long> collectSampleHeaders (final RandomAccessFile fileAccess, final long startPointer, final byte [] searchBytes, final int numOccurrences) throws IOException
    {
        final List<Long> bytePositions = new ArrayList<> ();
        for (long i = startPointer - searchBytes.length; i >= 0 && bytePositions.size () < numOccurrences; i--)
        {
            fileAccess.seek (i);
            final byte [] bytesToCompare = new byte [searchBytes.length];
            fileAccess.read (bytesToCompare);
            if (Arrays.equals (bytesToCompare, searchBytes))
                bytePositions.add (Long.valueOf (i));
        }

        // Reverse the byte positions list to put them in order
        Collections.reverse (bytePositions);
        return bytePositions;
    }


    /**
     * Reads and formats the Kontakt version number with which the file was created.
     *
     * @param in The input stream
     * @return The formatted version number, ends with a '?' if the patch level needs to be read
     *         separately.
     * @throws IOException
     */
    private String readVersion (final DataInput in) throws IOException
    {
        final byte [] buffer = new byte [4];
        in.readFully (buffer);

        if (!this.isBigEndian)
            StreamUtils.reverseArray (buffer);

        final StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < 3; i++)
            sb.append (Integer.toString (buffer[i])).append ('.');
        if (buffer[3] == -1)
            sb.append ('?');
        else
            sb.append (String.format ("%03d", Integer.valueOf (buffer[3])));
        return sb.toString ();
    }


    /**
     * Update the metadata info on all multi samples.
     *
     * @param multiSamples The multi samples to update
     * @param creation The formatted creation date
     * @param website The web site link
     * @param soundinfo The sound info
     */
    private static void updateMetadata (final List<IMultisampleSource> multiSamples, final String creation, final String website, final SoundinfoDocument soundinfo)
    {
        String additionalInfo = "Creation: " + creation;
        if (website != null)
            additionalInfo += "\nWebsite : " + website;

        for (final IMultisampleSource multiSample: multiSamples)
        {
            // Update the author
            final String soundAuthor = soundinfo.getAuthor ();
            if (soundAuthor != null && !soundAuthor.isBlank ())
                multiSample.setCreator (soundAuthor);

            // Update the category and keywords
            final Set<String> soundCategories = soundinfo.getCategories ();
            if (!soundCategories.isEmpty ())
                multiSample.setCategory (soundCategories.iterator ().next ());
            Collections.addAll (soundCategories, multiSample.getKeywords ());
            multiSample.setKeywords (TagDetector.detectKeywords (soundCategories.toArray (new String [soundCategories.size ()])));

            // Update the description
            String description = multiSample.getDescription ();
            description = description == null ? "" : "\n" + description;
            multiSample.setDescription (additionalInfo + description);
        }
    }
}
