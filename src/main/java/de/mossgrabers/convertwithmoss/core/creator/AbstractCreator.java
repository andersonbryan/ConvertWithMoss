// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.core.creator;

import de.mossgrabers.convertwithmoss.core.AbstractCoreTask;
import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.INotifier;
import de.mossgrabers.convertwithmoss.core.MathUtils;
import de.mossgrabers.convertwithmoss.core.model.IGroup;
import de.mossgrabers.convertwithmoss.core.model.IMetadata;
import de.mossgrabers.convertwithmoss.core.model.ISampleData;
import de.mossgrabers.convertwithmoss.core.model.ISampleLoop;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;
import de.mossgrabers.convertwithmoss.exception.ParseException;
import de.mossgrabers.convertwithmoss.file.riff.RiffID;
import de.mossgrabers.convertwithmoss.file.wav.BroadcastAudioExtensionChunk;
import de.mossgrabers.convertwithmoss.file.wav.InstrumentChunk;
import de.mossgrabers.convertwithmoss.file.wav.SampleChunk;
import de.mossgrabers.convertwithmoss.file.wav.SampleChunk.SampleChunkLoop;
import de.mossgrabers.convertwithmoss.file.wav.WaveFile;
import de.mossgrabers.tools.XMLUtils;
import de.mossgrabers.tools.ui.BasicConfig;
import de.mossgrabers.tools.ui.Functions;
import de.mossgrabers.tools.ui.panel.BoxPanel;

import org.w3c.dom.Document;

import javafx.scene.control.CheckBox;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Base class for creator classes.
 *
 * @author Jürgen Moßgraber
 */
public abstract class AbstractCreator extends AbstractCoreTask implements ICreator
{
    private static final String FORWARD_SLASH               = "/";

    private static final String WRITE_BROADCAST_AUDIO_CHUNK = "WriteBroadcastAudioChunk";
    private static final String WRITE_INSTRUMENT_CHUNK      = "WriteInstrumentChunk";
    private static final String WRITE_SAMPLE_CHUNK          = "WriteSampleChunk";
    private static final String REMOVE_JUNK_CHUNK           = "RemoveJunkChunk";

    protected CheckBox          addBroadcastAudioChunk;
    protected CheckBox          addInstrumentChunk;
    protected CheckBox          addSampleChunk;
    protected CheckBox          removeJunkChunks;


    /**
     * Constructor.
     *
     * @param name The name of the object.
     * @param notifier The notifier
     */
    protected AbstractCreator (final String name, final INotifier notifier)
    {
        super (name, notifier);
    }


    /**
     * Create a new XML document.
     *
     * @return The document or not present if there is a configuration problem
     */
    protected Optional<Document> createXMLDocument ()
    {
        try
        {
            return Optional.of (XMLUtils.newDocument ());
        }
        catch (final ParserConfigurationException ex)
        {
            this.notifier.logError ("IDS_NOTIFY_ERR_PARSER", ex);
            return Optional.empty ();
        }
    }


    protected static int check (final int value, final int defaultValue)
    {
        return value < 0 ? defaultValue : value;
    }


    /**
     * Removes illegal characters from file names.
     *
     * @param filename A potential filename
     * @return The filename with illegal characters replaced by an underscore
     */
    protected static String createSafeFilename (final String filename)
    {
        return filename.replaceAll ("[\\\\/:*?\"<>|&\\.]", "_").trim ();
    }


    /**
     * Format the path and filename replacing all slashes with forward slashes.
     *
     * @param path A path
     * @param filename A filename
     * @return The formatted path
     */
    public static String formatFileName (final String path, final String filename)
    {
        return new StringBuilder ().append (path).append ('/').append (filename).toString ().replace ('\\', '/');
    }


    /**
     * Format a double attribute with a dot as the fraction separator.
     *
     * @param value The value to format
     * @param fractions The number of fractions to format
     * @return The formatted value
     */
    public static String formatDouble (final double value, final int fractions)
    {
        final String formatPattern = "%." + fractions + "f";
        return String.format (Locale.US, formatPattern, Double.valueOf (value));
    }


    /**
     * Create the given folder if it does not already exist
     *
     * @param folder The folder to create
     * @throws IOException If the folder could not be created
     */
    protected static void safeCreateDirectory (final File folder) throws IOException
    {
        if (folder.exists () || folder.mkdir ())
            return;

        // A parallel thread might already have created the directory and mkdir did return
        // false. Therefore check again before throwing an exception
        if (!folder.exists ())
            throw new IOException (Functions.getMessage ("IDS_NOTIFY_ERROR_SAMPLE_FOLDER", folder.getAbsolutePath ()));
    }


    /**
     * Converts an unsigned integer to a number of bytes with least significant bytes first.
     *
     * @param value The value to convert
     * @param numberOfBytes The number of bytes to write
     * @return The converted integer
     */
    protected static byte [] toBytesLSB (final long value, final int numberOfBytes)
    {
        final byte [] data = new byte [numberOfBytes];

        for (int i = 0; i < numberOfBytes; i++)
            data[i] = (byte) (value >> 8 * i & 0xFF);

        return data;
    }


    /**
     * Adds an UTF-8 text file to the compressed ZIP output stream.
     *
     * @param zipOutputStream The compressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param content The text content of the file to add
     * @throws IOException Could not add the file
     */
    protected void zipTextFile (final ZipOutputStream zipOutputStream, final String fileName, final String content) throws IOException
    {
        this.zipDataFile (zipOutputStream, fileName, content.getBytes (StandardCharsets.UTF_8));
    }


    /**
     * Adds a file (in form of an array of bytes) to a compressed ZIP output stream.
     *
     * @param zipOutputStream The compressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param data The content of the file to add
     * @throws IOException Could not add the file
     */
    protected void zipDataFile (final ZipOutputStream zipOutputStream, final String fileName, final byte [] data) throws IOException
    {
        zipOutputStream.putNextEntry (new ZipEntry (fileName));
        zipOutputStream.write (data);
        zipOutputStream.flush ();
        zipOutputStream.closeEntry ();
    }


    /**
     * Adds an UTF-8 text file to the uncompressed ZIP output stream.
     *
     * @param zipOutputStream The uncompressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param content The text content of the file to add
     * @throws IOException Could not add the file
     */
    protected void storeTextFile (final ZipOutputStream zipOutputStream, final String fileName, final String content) throws IOException
    {
        this.storeTextFile (zipOutputStream, fileName, content, null);
    }


    /**
     * Adds an UTF-8 text file to the uncompressed ZIP output stream.
     *
     * @param zipOutputStream The uncompressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param content The text content of the file to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @throws IOException Could not add the file
     */
    protected void storeTextFile (final ZipOutputStream zipOutputStream, final String fileName, final String content, final Date dateTime) throws IOException
    {
        this.storeDataFile (zipOutputStream, fileName, content.getBytes (StandardCharsets.UTF_8), dateTime);
    }


    /**
     * Adds a file (in form of an array of bytes) to an uncompressed ZIP output stream.
     *
     * @param zipOutputStream The uncompressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param data The content of the file to add
     * @throws IOException Could not add the file
     */
    protected void storeDataFile (final ZipOutputStream zipOutputStream, final String fileName, final byte [] data) throws IOException
    {
        this.storeDataFile (zipOutputStream, fileName, data, null);
    }


    /**
     * Adds a file (in form of an array of bytes) to an uncompressed ZIP output stream.
     *
     * @param zipOutputStream The uncompressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param data The content of the file to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @throws IOException Could not add the file
     */
    protected void storeDataFile (final ZipOutputStream zipOutputStream, final String fileName, final byte [] data, final Date dateTime) throws IOException
    {
        // The checksum needs to be calculated in advance before the data is written to the output
        // stream!
        final CRC32 crc = new CRC32 ();
        crc.update (data);
        putUncompressedEntry (zipOutputStream, fileName, data, crc, dateTime);
    }


    /**
     * Add all samples from all groups in the given compressed ZIP output stream.
     *
     * @param zipOutputStream The ZIP output stream to which to add the samples
     * @param relativeFolderName The relative folder under which to store the file in the ZIP
     * @param multisampleSource The multisample
     * @throws IOException Could not store the samples
     */
    protected void zipSampleFiles (final ZipOutputStream zipOutputStream, final String relativeFolderName, final IMultisampleSource multisampleSource) throws IOException
    {
        int outputCount = 0;
        final Set<String> alreadyStored = new HashSet<> ();
        for (final IGroup group: multisampleSource.getGroups ())
        {
            for (final ISampleZone zone: group.getSampleZones ())
            {
                this.notifyProgress ();
                outputCount++;
                if (outputCount % 80 == 0)
                    this.notifyNewline ();

                zipSamplefile (alreadyStored, zipOutputStream, zone, multisampleSource.getMetadata ().getCreationTime (), relativeFolderName);
            }
        }
    }


    /**
     * Adds a sample file to the compressed ZIP output stream.
     *
     * @param alreadyStored Set with the already files to prevent trying to add duplicated files
     * @param zipOutputStream The ZIP output stream
     * @param zone The zone to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @throws IOException Could not read the file
     */
    protected static void zipSamplefile (final Set<String> alreadyStored, final ZipOutputStream zipOutputStream, final ISampleZone zone, final Date dateTime) throws IOException
    {
        zipSamplefile (alreadyStored, zipOutputStream, zone, dateTime, null);
    }


    /**
     * Adds a sample file to the compressed ZIP output stream.
     *
     * @param alreadyStored Set with the already files to prevent trying to add duplicated files
     * @param zipOutputStream The ZIP output stream
     * @param zone The zone to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @param path Optional path (may be null), must not end with a slash
     * @throws IOException Could not read the file
     */
    protected static void zipSamplefile (final Set<String> alreadyStored, final ZipOutputStream zipOutputStream, final ISampleZone zone, final Date dateTime, final String path) throws IOException
    {
        final String name = checkSampleName (alreadyStored, zone, path);
        if (name == null)
            return;

        final ZipEntry entry = new ZipEntry (name);
        if (dateTime != null)
        {
            final long millis = dateTime.getTime ();
            entry.setCreationTime (FileTime.fromMillis (millis));
            entry.setTime (millis);
        }

        zipOutputStream.putNextEntry (entry);
        zone.getSampleData ().writeSample (zipOutputStream);
        zipOutputStream.closeEntry ();
    }


    /**
     * Add all samples from all groups in the given uncompressed ZIP output stream.
     *
     * @param zipOutputStream The ZIP output stream to which to add the samples
     * @param relativeFolderName The relative folder under which to store the file in the ZIP
     * @param multisampleSource The multisample
     * @throws IOException Could not store the samples
     */
    protected void storeSampleFiles (final ZipOutputStream zipOutputStream, final String relativeFolderName, final IMultisampleSource multisampleSource) throws IOException
    {
        int outputCount = 0;
        final Set<String> alreadyStored = new HashSet<> ();
        for (final IGroup group: multisampleSource.getGroups ())
        {
            for (final ISampleZone zone: group.getSampleZones ())
            {
                this.notifyProgress ();
                outputCount++;
                if (outputCount % 80 == 0)
                    this.notifyNewline ();

                storeSamplefile (alreadyStored, zipOutputStream, zone, multisampleSource.getMetadata ().getCreationTime (), relativeFolderName);
            }
        }
    }


    /**
     * Adds a sample file to an uncompressed ZIP output stream.
     *
     * @param alreadyStored Set with the already files to prevent trying to add duplicated files
     * @param zipOutputStream The ZIP output stream
     * @param zone The zone to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @throws IOException Could not read the file
     */
    protected static void storeSamplefile (final Set<String> alreadyStored, final ZipOutputStream zipOutputStream, final ISampleZone zone, final Date dateTime) throws IOException
    {
        storeSamplefile (alreadyStored, zipOutputStream, zone, null, null);
    }


    /**
     * Adds a sample file to the uncompressed ZIP output stream.
     *
     * @param alreadyStored Set with the already files to prevent trying to add duplicated files
     * @param zipOutputStream The ZIP output stream
     * @param zone The zone to add
     * @param dateTime The date and time to set as the creation date of the file entry
     * @param path Optional path (may be null), must not end with a slash
     * @throws IOException Could not read the file
     */
    protected static void storeSamplefile (final Set<String> alreadyStored, final ZipOutputStream zipOutputStream, final ISampleZone zone, final Date dateTime, final String path) throws IOException
    {
        final String name = checkSampleName (alreadyStored, zone, path);
        if (name == null)
            return;

        final CRC32 crc = new CRC32 ();
        try (final ByteArrayOutputStream bout = new ByteArrayOutputStream (); final OutputStream checkedOut = new CheckedOutputStream (bout, crc))
        {
            zone.getSampleData ().writeSample (checkedOut);
            putUncompressedEntry (zipOutputStream, name, bout.toByteArray (), crc, dateTime);
        }
    }


    /**
     * Writes all samples in WAV format from all groups into the given folder.
     *
     * @param sampleFolder The destination folder
     * @param multisampleSource The multisample
     * @return The written files
     * @throws IOException Could not store the samples
     */
    protected List<File> writeSamples (final File sampleFolder, final IMultisampleSource multisampleSource) throws IOException
    {
        return this.writeSamples (sampleFolder, multisampleSource, false, false, false, false);
    }


    /**
     * Writes all samples in WAV format from all groups into the given folder.
     *
     * @param sampleFolder The destination folder
     * @param multisampleSource The multisample
     * @param updateBroadcastAudioChunk Add or update the broadcast audio extension chunk if true
     * @param updateInstrumentChunk Add or update the instrument chunk if true
     * @param updateSampleChunk If true the sample chunk is add or updated if already present
     * @param removeJunkChunks If true remove JUNK, junk, FLLR and MD5 chunks
     * @return The written files
     * @throws IOException Could not store the samples
     */
    protected List<File> writeSamples (final File sampleFolder, final IMultisampleSource multisampleSource, final boolean updateBroadcastAudioChunk, final boolean updateInstrumentChunk, final boolean updateSampleChunk, final boolean removeJunkChunks) throws IOException
    {
        return this.writeSamples (sampleFolder, multisampleSource, updateBroadcastAudioChunk, updateInstrumentChunk, updateSampleChunk, removeJunkChunks, ".wav");
    }


    /**
     * Writes all samples in WAV format from all groups into the given folder.
     *
     * @param sampleFolder The destination folder
     * @param multisampleSource The multisample
     * @param updateBroadcastAudioChunk Add or update the broadcast audio extension chunk if true
     * @param updateInstrumentChunk Add or update the instrument chunk if true
     * @param updateSampleChunk If true the sample chunk is add or updated if already present
     * @param removeJunkChunks If true remove JUNK, junk, FLLR and MD5 chunks
     * @param fileEnding The suffix to use for the file
     * @return The written files
     * @throws IOException Could not store the samples
     */
    protected List<File> writeSamples (final File sampleFolder, final IMultisampleSource multisampleSource, final boolean updateBroadcastAudioChunk, final boolean updateInstrumentChunk, final boolean updateSampleChunk, final boolean removeJunkChunks, final String fileEnding) throws IOException
    {
        final List<File> writtenFiles = new ArrayList<> ();

        int outputCount = 0;
        for (final IGroup group: multisampleSource.getGroups ())
        {
            for (final ISampleZone zone: group.getSampleZones ())
            {
                final File file = new File (sampleFolder, zone.getName () + fileEnding);
                try (final FileOutputStream fos = new FileOutputStream (file))
                {
                    this.notifyProgress ();
                    outputCount++;
                    if (outputCount % 80 == 0)
                        this.notifyNewline ();

                    final ISampleData sampleData = zone.getSampleData ();
                    if (updateBroadcastAudioChunk || updateInstrumentChunk || updateSampleChunk || removeJunkChunks)
                        updateChunks (multisampleSource.getMetadata (), zone, sampleData, fos, updateBroadcastAudioChunk, updateInstrumentChunk, updateSampleChunk, removeJunkChunks);
                    else
                        sampleData.writeSample (fos);
                }
                writtenFiles.add (file);
            }
        }

        return writtenFiles;
    }


    /**
     * Writes the sample of the given zone and updates/adds their instrument and sample chunks.
     *
     * @param metadata The metadata to store in a BEXT chunk
     * @param zone The zone from which to take the data to store into the chunks
     * @param sampleData The sample which contains the WAV data
     * @param outputStream Where to write the result
     * @param updateBroadcastAudioChunk Add or update the broadcast audio extension chunk if true
     * @param updateInstrumentChunk Add or update the instrument chunk if true
     * @param updateSampleChunk If true the sample chunk is add or updated if already present
     * @param removeJunkChunks If true remove JUNK, junk, FLLR and MD5 chunks
     * @throws IOException Could not store the samples
     */
    private static void updateChunks (final IMetadata metadata, final ISampleZone zone, final ISampleData sampleData, final OutputStream outputStream, final boolean updateBroadcastAudioChunk, final boolean updateInstrumentChunk, final boolean updateSampleChunk, final boolean removeJunkChunks) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        sampleData.writeSample (baos);

        try (final InputStream inputStream = new ByteArrayInputStream (baos.toByteArray ()))
        {
            final WaveFile wavFile = new WaveFile ();
            wavFile.read (inputStream, true);

            if (updateBroadcastAudioChunk)
                updateBroadcastAudioChunk (metadata, wavFile);

            final int unityNote = MathUtils.clamp (zone.getKeyRoot (), 0, 127);
            if (updateInstrumentChunk)
                updateInstrumentChunk (zone, wavFile, unityNote);

            if (updateSampleChunk)
                updateSampleChunk (zone, wavFile, unityNote);

            if (removeJunkChunks)
            {
                wavFile.removeChunks (RiffID.JUNK_ID, RiffID.JUNK2_ID, RiffID.FILLER_ID, RiffID.MD5_ID);
            }

            wavFile.write (outputStream);
        }
        catch (final ParseException ex)
        {
            throw new IOException (ex);
        }
    }


    private static void updateSampleChunk (final ISampleZone zone, final WaveFile wavFile, final int unityNote)
    {
        final List<ISampleLoop> loops = zone.getLoops ();
        final SampleChunk sampleChunk = new SampleChunk (loops.size ());
        sampleChunk.setSamplePeriod ((int) (1000000000.0 / wavFile.getFormatChunk ().getSampleRate ()));

        final int cent = (int) (zone.getTune () * 100);

        // Needs to be inverted since this is the playback root!
        int rootNote = unityNote - cent / 100;
        int pitchOffset = cent % 100;
        // Pitch fraction can only be positive (0-99 cents)!
        if (pitchOffset < 0)
        {
            rootNote++;
            pitchOffset += 100;
        }
        sampleChunk.setMIDIUnityNote (rootNote);
        sampleChunk.setMIDIPitchFractionAsCents (pitchOffset);

        final List<SampleChunkLoop> chunkLoops = sampleChunk.getLoops ();
        for (int i = 0; i < loops.size (); i++)
        {
            final ISampleLoop sampleLoop = loops.get (i);
            final SampleChunkLoop sampleChunkLoop = chunkLoops.get (i);
            switch (sampleLoop.getType ())
            {
                case FORWARD:
                    sampleChunkLoop.setType (SampleChunk.LOOP_FORWARD);
                    break;
                case ALTERNATING:
                    sampleChunkLoop.setType (SampleChunk.LOOP_ALTERNATING);
                    break;
                case BACKWARDS:
                    sampleChunkLoop.setType (SampleChunk.LOOP_BACKWARDS);
                    break;
            }
            sampleChunkLoop.setStart (sampleLoop.getStart ());
            sampleChunkLoop.setEnd (sampleLoop.getEnd ());
        }

        wavFile.setSampleChunk (sampleChunk);
    }


    private static void updateInstrumentChunk (final ISampleZone zone, final WaveFile wavFile, final int unityNote)
    {
        InstrumentChunk instrumentChunk = wavFile.getInstrumentChunk ();
        if (instrumentChunk == null)
        {
            instrumentChunk = new InstrumentChunk ();
            wavFile.setInstrumentChunk (instrumentChunk);
        }

        instrumentChunk.setUnshiftedNote (unityNote);
        instrumentChunk.setFineTune (MathUtils.clamp ((int) (zone.getTune () * 100), -50, 50));
        instrumentChunk.setGain (MathUtils.clamp ((int) zone.getGain (), -127, 127));
        instrumentChunk.setLowNote (MathUtils.clamp (zone.getKeyLow (), 0, 127));
        instrumentChunk.setHighNote (MathUtils.clamp (zone.getKeyHigh (), 0, 127));
        instrumentChunk.setLowVelocity (MathUtils.clamp (zone.getVelocityLow (), 0, 127));
        instrumentChunk.setHighVelocity (MathUtils.clamp (zone.getVelocityHigh (), 0, 127));
    }


    private static void updateBroadcastAudioChunk (final IMetadata metadata, final WaveFile wavFile)
    {
        BroadcastAudioExtensionChunk broadcastAudioChunk = wavFile.getBroadcastAudioExtensionChunk ();
        if (broadcastAudioChunk == null)
        {
            broadcastAudioChunk = new BroadcastAudioExtensionChunk ();
            wavFile.setBroadcastAudioExtensionChunk (broadcastAudioChunk);
        }

        broadcastAudioChunk.setDescription (metadata.getDescription ());
        broadcastAudioChunk.setOriginator (metadata.getCreator ());
        broadcastAudioChunk.setOriginationDateTime (metadata.getCreationTime ());
    }


    /**
     * Creates an optional string from the XML document.
     *
     * @param document The XML document
     * @return The optional string; empty if an error occurs
     */
    protected Optional<String> createXMLString (final Document document)
    {
        try
        {
            return Optional.of (XMLUtils.toString (document));
        }
        catch (final TransformerException ex)
        {
            this.notifier.logError (ex);
            return Optional.empty ();
        }
    }


    /**
     * Creates full path from the sample name and relative path and adding the prefix path.
     *
     * @param alreadyStored All paths already added to the ZIP file
     * @param zone The sample zone to check
     * @param path The prefix path
     * @return The full path or null if already added to the ZIP
     */
    private static String checkSampleName (final Set<String> alreadyStored, final ISampleZone zone, final String path)
    {
        String filename = zone.getName () + ".wav";
        if (path != null)
            filename = path + FORWARD_SLASH + filename;
        if (alreadyStored.contains (filename))
            return null;
        alreadyStored.add (filename);
        return filename;
    }


    /**
     * Adds a new entry to an uncompressed ZIP output stream.
     *
     * @param zipOutputStream The uncompressed ZIP output stream
     * @param fileName The name to use for the file when added
     * @param data The content of the file to add
     * @param checksum The checksum
     * @param dateTime The date and time to set as the creation date of the file entry
     * @throws IOException Could not add the file
     */
    private static void putUncompressedEntry (final ZipOutputStream zipOutputStream, final String fileName, final byte [] data, final CRC32 checksum, final Date dateTime) throws IOException
    {
        final ZipEntry entry = new ZipEntry (fileName);
        entry.setSize (data.length);
        entry.setCompressedSize (data.length);
        entry.setCrc (checksum.getValue ());
        entry.setMethod (ZipOutputStream.STORED);
        if (dateTime != null)
        {
            final long millis = dateTime.getTime ();
            entry.setCreationTime (FileTime.fromMillis (millis));
            entry.setTime (millis);
        }

        zipOutputStream.putNextEntry (entry);
        zipOutputStream.write (data);
        zipOutputStream.closeEntry ();
    }


    private void notifyProgress ()
    {
        this.notifier.log ("IDS_NOTIFY_PROGRESS");
    }


    private void notifyNewline ()
    {
        this.notifier.log ("IDS_NOTIFY_LINE_FEED");
    }


    /**
     * Adds options add or update certain WAV chunks.
     *
     * @param panel The panel to add the widgets
     */
    protected void addWavChunkOptions (final BoxPanel panel)
    {
        panel.createSeparator ("@IDS_WAV_CHUNK_TITLE");
        this.addBroadcastAudioChunk = panel.createCheckBox ("@IDS_WAV_WRITE_BEXT_CHUNK");
        this.addInstrumentChunk = panel.createCheckBox ("@IDS_WAV_WRITE_INSTRUMENT_CHUNK");
        this.addSampleChunk = panel.createCheckBox ("@IDS_WAV_WRITE_SAMPLE_CHUNK");
        this.removeJunkChunks = panel.createCheckBox ("@IDS_WAV_CHUNK_REMOVE");
    }


    /**
     * Load the settings of the creator.
     *
     * @param configuration Where to read from
     * @param prefix The prefix to use for the identifier
     */
    public void loadWavChunkSettings (final BasicConfig configuration, final String prefix)
    {
        this.addBroadcastAudioChunk.setSelected (configuration.getBoolean (prefix + WRITE_BROADCAST_AUDIO_CHUNK, true));
        this.addInstrumentChunk.setSelected (configuration.getBoolean (prefix + WRITE_INSTRUMENT_CHUNK, true));
        this.addSampleChunk.setSelected (configuration.getBoolean (prefix + WRITE_SAMPLE_CHUNK, true));
        this.removeJunkChunks.setSelected (configuration.getBoolean (prefix + REMOVE_JUNK_CHUNK, true));
    }


    /**
     * Save the settings of the creator.
     *
     * @param configuration Where to store to
     * @param prefix The prefix to use for the identifier
     */
    public void saveWavChunkSettings (final BasicConfig configuration, final String prefix)
    {
        configuration.setBoolean (prefix + WRITE_BROADCAST_AUDIO_CHUNK, this.shouldWriteBroadcastAudioChunk ());
        configuration.setBoolean (prefix + WRITE_INSTRUMENT_CHUNK, this.shouldWriteInstrumentChunk ());
        configuration.setBoolean (prefix + WRITE_SAMPLE_CHUNK, this.shouldWriteSampleChunk ());
        configuration.setBoolean (prefix + REMOVE_JUNK_CHUNK, this.shouldRemoveJunkChunks ());
    }


    /**
     * Check if the broadcast audio chunk should be written.
     *
     * @return True if enabled
     */
    protected boolean shouldWriteBroadcastAudioChunk ()
    {
        return this.addBroadcastAudioChunk.isSelected ();
    }


    /**
     * Check if the instrument chunk should be written.
     *
     * @return True if enabled
     */
    protected boolean shouldWriteInstrumentChunk ()
    {
        return this.addInstrumentChunk.isSelected ();
    }


    /**
     * Check if the sample chunk should be written.
     *
     * @return True if enabled
     */
    protected boolean shouldWriteSampleChunk ()
    {
        return this.addSampleChunk.isSelected ();
    }


    /**
     * Check if some junk chunks should be removed.
     *
     * @return True if enabled
     */
    protected boolean shouldRemoveJunkChunks ()
    {
        return this.removeJunkChunks.isSelected ();
    }
}
