// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.file.wav;

import de.mossgrabers.convertwithmoss.exception.CompressionNotSupportedException;
import de.mossgrabers.convertwithmoss.exception.ParseException;
import de.mossgrabers.convertwithmoss.file.riff.AbstractSpecificRIFFChunk;
import de.mossgrabers.convertwithmoss.file.riff.RawRIFFChunk;
import de.mossgrabers.convertwithmoss.file.riff.RiffID;


/**
 * Wrapper of a data chunk ("data") in a WAV file. Data is stored stereo interleaved little-endian.
 *
 * @author Jürgen Moßgraber
 */
public class DataChunk extends AbstractSpecificRIFFChunk
{
    /**
     * Constructor. Creates an empty data chunk.
     *
     * @param formatChunk The format chunk, necessary for the calculation (sample size and number of
     *            channels
     * @param lengthInSamples The length of the sample (number of samples)
     */
    public DataChunk (final FormatChunk formatChunk, final int lengthInSamples)
    {
        super (RiffID.DATA_ID, formatChunk.calculateDataSize (lengthInSamples));
    }


    /**
     * Constructor. Creates an empty data chunk.
     *
     * @param formatChunk The format chunk, necessary for the calculation (sample size and number of
     *            channels
     * @param data The data
     */
    public DataChunk (final FormatChunk formatChunk, final byte [] data)
    {
        super (RiffID.DATA_ID, data);
    }


    /**
     * Constructor.
     *
     * @param chunk The RIFF chunk which contains the data
     * @throws ParseException The raw chunk is not of the specific type or the length of data does
     *             not match the expected chunk size
     */
    public DataChunk (final RawRIFFChunk chunk) throws ParseException
    {
        super (RiffID.DATA_ID, chunk);
    }


    /**
     * Sets the data.
     *
     * @param data The data
     */
    public void setData (final byte [] data)
    {
        this.rawRiffChunk.setData (data);
    }


    /**
     * Calculates the length of the data in samples.
     *
     * @param formatChunk The format chunk, necessary for the calculation (sample size and number of
     *            channels
     * @return The length of the audio file in samples
     * @throws CompressionNotSupportedException If the compression/encoding used for the data is not
     *             supported
     */
    public int calculateLength (final FormatChunk formatChunk) throws CompressionNotSupportedException
    {
        final int compressionCode = formatChunk.getCompressionCode ();

        if (compressionCode == FormatChunk.WAVE_FORMAT_PCM || compressionCode == FormatChunk.WAVE_FORMAT_IEEE_FLOAT)
            return formatChunk.calculateLength (this.rawRiffChunk.getData ());

        if (compressionCode == FormatChunk.WAVE_FORMAT_EXTENSIBLE)
        {
            final int numberOfChannels = formatChunk.getNumberOfChannels ();
            if (numberOfChannels > 2)
                throw new CompressionNotSupportedException ("WAV files in Extensible format are only supported for stereo files.");
            return formatChunk.calculateLength (this.rawRiffChunk.getData ());
        }

        throw new CompressionNotSupportedException ("Unsupported data compression: " + FormatChunk.getCompression (compressionCode));
    }


    /** {@inheritDoc} */
    @Override
    public String infoText ()
    {
        final StringBuilder sb = new StringBuilder ();
        sb.append ("Size: ").append (this.rawRiffChunk.getData ().length + " Bytes");
        return sb.toString ();
    }
}
