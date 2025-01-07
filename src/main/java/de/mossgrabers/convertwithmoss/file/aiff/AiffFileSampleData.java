// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.file.aiff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.mossgrabers.convertwithmoss.core.model.IMetadata;
import de.mossgrabers.convertwithmoss.core.model.ISampleLoop;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;
import de.mossgrabers.convertwithmoss.core.model.enumeration.LoopType;
import de.mossgrabers.convertwithmoss.core.model.implementation.AbstractFileSampleData;
import de.mossgrabers.convertwithmoss.core.model.implementation.DefaultSampleLoop;
import de.mossgrabers.convertwithmoss.file.wav.DataChunk;
import de.mossgrabers.convertwithmoss.file.wav.WaveFile;
import de.mossgrabers.tools.ui.Functions;


/**
 * The data of an AIFF sample file.
 *
 * @author Jürgen Moßgraber
 */
public class AiffFileSampleData extends AbstractFileSampleData
{
    private File     sourceFile = null;
    private AiffFile aiffFile   = null;


    /**
     * Constructor.
     *
     * @param file The file where the sample is stored
     * @throws IOException Could not read the file
     */
    public AiffFileSampleData (final File file) throws IOException
    {
        super (file);

        this.fixFileEnding ();
    }


    /**
     * Constructor for a sample stored in a ZIP file.
     *
     * @param zipFile The ZIP file which contains the WAV files
     * @param zipEntry The relative path in the ZIP where the file is stored
     * @throws IOException Could not read the file
     */
    public AiffFileSampleData (final File zipFile, final File zipEntry) throws IOException
    {
        super (zipFile, zipEntry);
    }


    private void fixFileEnding () throws IOException
    {
        if (!this.sampleFile.getName ().toLowerCase ().endsWith ("aiff"))
            return;

        // Ugly workaround for the SPI not accepting AIFF files with the ending 'aiff'
        final File tempFile = File.createTempFile ("temp", ".aif");
        Files.copy (this.sampleFile.toPath (), tempFile.toPath (), StandardCopyOption.REPLACE_EXISTING);
        this.sourceFile = this.sampleFile;
        this.sampleFile = tempFile;
    }


    /** {@inheritDoc} */
    @Override
    public void writeSample (final OutputStream outputStream) throws IOException
    {
        if (this.zipFile == null)
        {
            try (final InputStream inputStream = new FileInputStream (this.sampleFile))
            {
                this.readConvertWrite (inputStream, outputStream);
            }
            return;
        }

        try (final ZipFile zf = new ZipFile (this.zipFile))
        {
            final String path = this.zipEntry.getPath ().replace ('\\', '/');
            final ZipEntry entry = zf.getEntry (path);
            if (entry == null)
                throw new FileNotFoundException (Functions.getMessage ("IDS_NOTIFY_ERR_FILE_NOT_FOUND_IN_ZIP", path));
            try (final InputStream inputStream = zf.getInputStream (entry))
            {
                this.readConvertWrite (inputStream, outputStream);
            }
        }
    }


    private void readConvertWrite (final InputStream inputStream, final OutputStream outputStream) throws IOException
    {
        // Read the input AIFF file
        try (final InputStream in = new BufferedInputStream (inputStream); final AudioInputStream audioIn = AudioSystem.getAudioInputStream (in))
        {
            // Obtains the file types that the system can write from the audio input stream
            // specified. Check if WAV can be written
            final AudioFileFormat.Type [] supportedTypes = AudioSystem.getAudioFileTypes (audioIn);
            if (!Arrays.asList (supportedTypes).contains (AudioFileFormat.Type.AIFF))
                throw new IOException (Functions.getMessage ("IDS_ERR_AIFF_TO_WAV_NOT_SUPPORTED"));

            // Write the output WAV file
            AudioSystem.write (audioIn, AudioFileFormat.Type.WAVE, outputStream);
        }
        catch (final UnsupportedAudioFileException ex)
        {
            final String fileEnding = this.sampleFile.getName ().toLowerCase ();
            if (fileEnding.endsWith (".aiff") || fileEnding.endsWith (".aif"))
            {
                final AiffFile aiffFile = new AiffFile (this.sampleFile);
                final AiffCommonChunk commonChunk = aiffFile.getCommonChunk ();
                final WaveFile wavFile = new WaveFile (commonChunk.getNumChannels (), commonChunk.getSampleRate (), commonChunk.getSampleSize (), (int) commonChunk.getNumSampleFrames ());
                final DataChunk dataChunk = wavFile.getDataChunk ();
                final byte [] soundData = aiffFile.getSoundDataChunk ().getData ();
                final byte [] destinationData = dataChunk.getData ();
                System.arraycopy (soundData, 0, destinationData, 0, Math.min (destinationData.length, soundData.length));
                wavFile.write (outputStream);
                return;
            }

            throw new IOException (ex);
        }
        finally
        {
            // Remove the temporary file after usage
            if (this.sourceFile != null)
            {
                Files.delete (this.sourceFile.toPath ());
                this.sampleFile = this.sourceFile;
                this.sourceFile = null;
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    public void addZoneData (final ISampleZone zone, final boolean addRootKey, final boolean addLoops) throws IOException
    {
        final AiffFile aifFile = this.getAiffFile ();
        final AiffCommonChunk commonChunk = aifFile.getCommonChunk ();
        if (commonChunk == null)
            return;

        final int numberOfChannels = commonChunk.numChannels;
        if (numberOfChannels > 2)
            throw new IOException (Functions.getMessage ("IDS_NOTIFY_ERR_MONO", Integer.toString (numberOfChannels), this.sampleFile.getAbsolutePath ()));

        if (zone.getStart () < 0)
            zone.setStart (0);
        if (zone.getStop () <= 0)
            zone.setStop ((int) commonChunk.numSampleFrames);

        final AiffInstrumentChunk instrumentChunk = aifFile.getInstrumentChunk ();
        if (instrumentChunk == null)
            return;

        // Read the this.keyRoot if not set...
        if (addRootKey && zone.getKeyRoot () == -1)
            zone.setKeyRoot (instrumentChunk.baseNote);

        if (zone.getTune () == 0)
            zone.setTune (Math.clamp (instrumentChunk.detune / 100.0, -0.5, 0.5));

        if (addLoops)
            addLoops (instrumentChunk, aifFile.getMarkerChunk (), zone.getLoops ());
    }


    private static void addLoops (final AiffInstrumentChunk instrumentChunk, final AiffMarkerChunk markerChunk, final List<ISampleLoop> loops)
    {
        // Check if are already present
        if (!loops.isEmpty ())
            return;

        final AiffLoop sampleLoop = instrumentChunk.sustainLoop;
        if (sampleLoop.playMode == AiffLoop.NO_LOOPING || markerChunk == null)
            return;

        final DefaultSampleLoop loop = new DefaultSampleLoop ();
        switch (sampleLoop.playMode)
        {
            default:
            case AiffLoop.FORWARD_LOOPING:
                loop.setType (LoopType.FORWARDS);
                break;
            case AiffLoop.FORWARD_BACKWARD_LOOPING:
                loop.setType (LoopType.ALTERNATING);
                break;
        }

        final Map<Integer, AiffMarker> markers = markerChunk.getMarkers ();
        final AiffMarker startMarker = markers.get (Integer.valueOf (sampleLoop.beginLoopMarkerID));
        final AiffMarker endMarker = markers.get (Integer.valueOf (sampleLoop.endLoopMarkerID));
        if (startMarker != null && endMarker != null)
        {
            loop.setStart ((int) startMarker.position);
            loop.setEnd ((int) endMarker.position);
            loops.add (loop);
        }
    }


    /**
     * Get the underlying AIFF file.
     *
     * @return The file
     * @throws IOException Could not parse the file
     */
    public AiffFile getAiffFile () throws IOException
    {
        if (this.aiffFile == null)
        {
            if (this.zipFile == null)
                this.aiffFile = new AiffFile (this.sampleFile);
            else
            {
                this.aiffFile = new AiffFile ();

                try (final ZipFile zf = new ZipFile (this.zipFile))
                {
                    final String path = this.zipEntry.getPath ().replace ('\\', '/');
                    final ZipEntry entry = zf.getEntry (path);
                    if (entry == null)
                        throw new FileNotFoundException (Functions.getMessage ("IDS_NOTIFY_ERR_FILE_NOT_FOUND_IN_ZIP", path));
                    try (final InputStream in = zf.getInputStream (entry))
                    {
                        this.aiffFile.read (in);
                    }
                }
            }
        }

        return this.aiffFile;
    }


    /** {@inheritDoc} */
    @Override
    public void updateMetadata (final IMetadata metadata)
    {
        final AiffFile aifFile;
        try
        {
            aifFile = this.getAiffFile ();
        }
        catch (final IOException ex)
        {
            return;
        }

        final Map<String, String> aiffMetadata = aifFile.getMetadata ();

        final String author = aiffMetadata.get (AiffFile.AIFF_CHUNK_AUTHOR);
        if (author != null)
            metadata.setCreator (author);

        final StringBuilder sb = new StringBuilder ();

        final String copyright = aiffMetadata.get (AiffFile.AIFF_CHUNK_COPYRIGHT);
        if (copyright != null)
            sb.append (copyright).append ('\n');
        final String annotation = aiffMetadata.get (AiffFile.AIFF_CHUNK_ANNOTATION);
        if (annotation != null)
            sb.append (annotation).append ('\n');

        int i = 1;
        while (true)
        {
            final String comment = aiffMetadata.get ("Comment" + i);
            if (comment == null)
                break;
            sb.append (comment).append ('\n');
            i++;
        }

        final String description = sb.toString ().trim ();
        if (!description.isEmpty ())
            metadata.setDescription (description);
    }
}
