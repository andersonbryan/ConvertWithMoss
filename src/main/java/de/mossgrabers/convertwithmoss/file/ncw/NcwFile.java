// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.file.ncw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.mossgrabers.convertwithmoss.file.StreamUtils;
import de.mossgrabers.convertwithmoss.file.wav.DataChunk;
import de.mossgrabers.convertwithmoss.file.wav.FormatChunk;
import de.mossgrabers.convertwithmoss.file.wav.WaveFile;
import de.mossgrabers.tools.ui.Functions;


/**
 * Kontakt Loss-less Compression Audio File.
 *
 * @author Jürgen Moßgraber
 */
public class NcwFile
{
    private static final int NUM_SAMPLES     = 512;
    private static final int FILE_MAGIC      = 0xD69EA801;
    private static final int BLOCK_MAGIC     = 0x3E9A0C16;

    private static final int FLAG_MID_SIDE   = 1;
    private static final int FLAG_IEEE_FLOAT = 2;

    // Kontakt 4
    private static final int VERSION1        = 0x130;
    // KOntakt 5
    private static final int VERSION2        = 0x131;

    private int              channels;
    private int              bitsPerSample;
    private int              sampleRate;
    private int              numberOfSamples;

    private int [] []        channelData;
    private float [] []      channelDataFloat;
    private final File       ncwFile;
    private final Object     lazyLoadingLock = new Object ();


    /**
     * Constructor. Reads the given NCW file.
     *
     * @param ncwFile The NCW file
     * @throws IOException Could not read the file
     */
    public NcwFile (final File ncwFile) throws IOException
    {
        this.ncwFile = ncwFile;
    }


    /**
     * Constructor. Reads the given NCW file.
     *
     * @param inputStream The stream from which to read the NCW file
     * @throws IOException Could not read the file
     */
    public NcwFile (final InputStream inputStream) throws IOException
    {
        this.ncwFile = null;
        this.read (inputStream);
    }


    /**
     * Get the number of channels.
     *
     * @return The number of channels
     * @throws IOException Could not read the file
     */
    public int getChannels () throws IOException
    {
        this.lazyLoading ();
        return this.channels;
    }


    /**
     * Get the number of samples of one channel.
     *
     * @return The number of samples
     * @throws IOException Could not read the file
     */
    public int getNumberOfSamples () throws IOException
    {
        this.lazyLoading ();
        return this.numberOfSamples;
    }


    /**
     * Get the bits per sample.
     *
     * @return Bits per sample
     * @throws IOException Could not read the file
     */
    public int getBitsPerSample () throws IOException
    {
        this.lazyLoading ();
        return this.bitsPerSample;
    }


    /**
     * Get the sample rate.
     *
     * @return The sample rate
     * @throws IOException Could not read the file
     */
    public int getSampleRate () throws IOException
    {
        this.lazyLoading ();
        return this.sampleRate;
    }


    /**
     * Write the decoded data as a WAV file.
     *
     * @param outputStream Where to write the WAV file
     * @throws IOException Could not write
     */
    public synchronized void writeWAV (final OutputStream outputStream) throws IOException
    {
        synchronized (this.lazyLoadingLock)
        {
            this.lazyLoading ();

            final boolean isFloat = this.channelDataFloat != null;

            final WaveFile wavFile = new WaveFile (this.channels, this.sampleRate, this.bitsPerSample, this.numberOfSamples);
            if (isFloat)
                wavFile.getFormatChunk ().setCompressionCode (FormatChunk.WAVE_FORMAT_IEEE_FLOAT);
            final DataChunk dataChunk = wavFile.getDataChunk ();

            final ByteArrayOutputStream bout = new ByteArrayOutputStream (this.channels * (this.bitsPerSample / 8) * this.numberOfSamples);
            if (isFloat)
                for (int i = 0; i < this.numberOfSamples; i++)
                    for (int channel = 0; channel < this.channels; channel++)
                        StreamUtils.writeFloatLE (bout, this.channelDataFloat[channel][i]);
            else
                for (int i = 0; i < this.numberOfSamples; i++)
                    for (int channel = 0; channel < this.channels; channel++)
                        StreamUtils.writeUnsigned (bout, this.channelData[channel][i], this.bitsPerSample, false);

            dataChunk.setData (bout.toByteArray ());
            wavFile.write (outputStream);

            // Dirty workaround to allow garbage collection
            this.channelData = null;
            this.channelDataFloat = null;
        }
    }


    private void lazyLoading () throws IOException
    {
        synchronized (this.lazyLoadingLock)
        {
            if (this.channelData != null || this.channelDataFloat != null)
                return;

            try (final FileInputStream stream = new FileInputStream (this.ncwFile))
            {
                this.read (stream);
            }
        }
    }


    /**
     * Reads a NCW file from a stream.
     *
     * @param inputStream The input stream which provides the NCW file
     * @throws IOException Could not read the file
     */
    private void read (final InputStream inputStream) throws IOException
    {
        final int numberOfBlocks = this.readHeader (inputStream);

        this.channelData = new int [this.channels] [];
        for (int i = 0; i < this.channels; i++)
            this.channelData[i] = new int [this.numberOfSamples];

        // Read all offsets
        final int [] offsets = new int [numberOfBlocks];
        for (int i = 0; i < numberOfBlocks; i++)
            offsets[i] = (int) StreamUtils.readUnsigned32 (inputStream, false);

        if (offsets[0] != 0)
            throw new IOException (Functions.getMessage ("IDS_NCW_FIRST_BLOCK_OFFSET_MUST_BE_ZERO"));

        for (int i = 1; i < numberOfBlocks; i++)
        {
            final int blockSize = offsets[i] - offsets[i - 1];
            final byte [] blockData = inputStream.readNBytes (blockSize);
            this.parseBlock (new ByteArrayInputStream (blockData), i - 1);
        }

        final int available = inputStream.available ();
        if (available > 0)
            throw new IOException (Functions.getMessage ("IDS_NCW_UNREAD_BYTES", Integer.toString (available)));
    }


    /**
     * Reads the NCW header.
     *
     * @param inputStream The input stream from which to read
     * @return The number of data blocks
     * @throws IOException Could not read the header
     */
    private int readHeader (final InputStream inputStream) throws IOException
    {
        final int fileMagic = (int) StreamUtils.readUnsigned32 (inputStream, false);
        if (fileMagic != FILE_MAGIC)
            throw new IOException (Functions.getMessage ("IDS_NCW_NOT_A_NCW_FILE"));

        final int version = (int) StreamUtils.readUnsigned32 (inputStream, false);
        if (version != VERSION1 && version != VERSION2)
            throw new IOException (Functions.getMessage ("IDS_NCW_UNKNOWN_VERSION", Integer.toHexString (version).toUpperCase ()));

        this.channels = StreamUtils.readUnsigned16 (inputStream, false);
        // The bits per sample: 16, 24 or 32
        this.bitsPerSample = StreamUtils.readUnsigned16 (inputStream, false);
        this.sampleRate = (int) StreamUtils.readUnsigned32 (inputStream, false);
        this.numberOfSamples = (int) StreamUtils.readUnsigned32 (inputStream, false);

        final long offsetBlockAddress = StreamUtils.readUnsigned32 (inputStream, false);
        final long offsetBlockData = StreamUtils.readUnsigned32 (inputStream, false);

        // Size of all blocks, not needed
        StreamUtils.readUnsigned32 (inputStream, false);

        // Padding - might contain content in the future!
        inputStream.skipNBytes (88);

        return (int) ((offsetBlockData - offsetBlockAddress) / 4);
    }


    /**
     * Parse 1 data block.
     *
     * @param inputStream Where to read the block from
     * @param blockIndex The index of the block
     * @throws IOException Could not read the block
     */
    private void parseBlock (final ByteArrayInputStream inputStream, final int blockIndex) throws IOException
    {
        boolean isMidSide = false;
        boolean isFloat = false;
        int offset = 0;
        int length = 0;

        for (int channel = 0; channel < this.channels; channel++)
        {
            final int blockMagic = (int) StreamUtils.readUnsigned32 (inputStream, false);
            if (blockMagic != BLOCK_MAGIC)
                throw new IOException (Functions.getMessage ("IDS_NCW_NOT_A_NCW_FILE"));

            final int baseValue = StreamUtils.readSigned32 (inputStream, false);
            final int bits = StreamUtils.readSigned16 (inputStream, false);
            final int flags = StreamUtils.readUnsigned16 (inputStream, false);
            if (flags > 3)
                throw new IOException (Functions.getMessage ("IDS_NCW_UNSUPPORTED_FLAGS", Integer.toString (flags)));
            if ((flags & FLAG_MID_SIDE) > 0)
                isMidSide = true;
            if ((flags & FLAG_IEEE_FLOAT) > 0)
                isFloat = true;

            // Padding
            inputStream.skipNBytes (4);

            final byte [] blockData = inputStream.readNBytes (NUM_SAMPLES * Math.abs (bits) / 8);
            final int [] samples;
            if (bits > 0)
                // Delta encoding compression
                samples = decodeDeltaBlock (baseValue, blockData, bits);
            else if (bits < 0)
                // Truncation encoding compression
                samples = decodeTruncatedBlock (blockData, Math.abs (bits));
            else
            {
                // No compression
                final int bytesPerSample = this.bitsPerSample / 8;
                samples = new int [NUM_SAMPLES];
                for (int i = 0; i < NUM_SAMPLES; i++)
                {
                    final byte [] sampleBytes = new byte [bytesPerSample];
                    System.arraycopy (blockData, i * bytesPerSample, sampleBytes, 0, bytesPerSample);
                    samples[i] = ByteBuffer.wrap (sampleBytes).order (ByteOrder.LITTLE_ENDIAN).getInt ();
                }
            }

            offset = blockIndex * NUM_SAMPLES;
            length = Math.min (NUM_SAMPLES, this.numberOfSamples - offset);
            System.arraycopy (samples, 0, this.channelData[channel], offset, length);
        }

        // Re-interpret read integer values as IEEE 754 float values
        if (isFloat)
        {
            if (this.channelDataFloat == null)
            {
                this.channelDataFloat = new float [this.channels] [];
                for (int channel = 0; channel < this.channels; channel++)
                    this.channelDataFloat[channel] = new float [this.numberOfSamples];
            }

            for (int channel = 0; channel < this.channels; channel++)
                for (int i = 0; i < length; i++)
                    this.channelDataFloat[channel][offset + i] = Float.intBitsToFloat (this.channelData[channel][offset + i]);
        }

        // Convert mid/side sample data into left/right sample data
        if (isMidSide)
        {
            if (this.channels != 2)
                throw new IOException (Functions.getMessage ("IDS_NCW_MID_SIDE_ONLY_SUPPORTED_FOR_STEREO"));

            for (int i = 0; i < length; i++)
                if (isFloat)
                {
                    final float mid = this.channelDataFloat[0][offset + i];
                    final float side = this.channelDataFloat[1][offset + i];
                    this.channelDataFloat[0][offset + i] = mid + side;
                    this.channelDataFloat[1][offset + i] = mid - side;
                }
                else
                {
                    final int mid = this.channelData[0][offset + i];
                    final int side = this.channelData[1][offset + i];
                    this.channelData[0][offset + i] = mid + side;
                    this.channelData[1][offset + i] = mid - side;
                }
        }
    }


    /**
     * Decode delta encoded integers. Each value is the offset to the next sample.
     *
     * @param baseSample The first sample value
     * @param deltas The deltas
     * @param precisionInBits The number of bits of 1 delta
     * @return The decoded 32 bit samples
     */
    private static int [] decodeDeltaBlock (final int baseSample, final byte [] deltas, final int precisionInBits)
    {
        final int [] deltaValues = readPackedValues (deltas, precisionInBits);
        final int [] samples = new int [NUM_SAMPLES];
        int prevBase = baseSample;
        for (int i = 0; i < NUM_SAMPLES; i++)
        {
            samples[i] = prevBase;
            prevBase += deltaValues[i];
        }
        return samples;
    }


    /**
     * Read the packed deltas.
     *
     * @param data The delta data to decode
     * @param precisionInBits The number of bits of 1 delta
     * @return The decoded delta values
     */
    private static int [] readPackedValues (final byte [] data, final int precisionInBits)
    {
        final int [] deltas = new int [NUM_SAMPLES];
        int bitAccumulator = 0;
        int bitsInAccumulator = 0;
        int byteIndex = 0;

        int count = 0;
        while (byteIndex < data.length)
        {
            // Accumulate more bits
            bitAccumulator |= (data[byteIndex] & 0xFF) << bitsInAccumulator;
            bitsInAccumulator += 8;
            byteIndex += 1;

            // Extract values as long as enough bits are available
            while (bitsInAccumulator >= precisionInBits)
            {
                int value = bitAccumulator & (1 << precisionInBits) - 1;
                if ((value & 1 << precisionInBits - 1) != 0)
                    value |= ~0 << precisionInBits;
                deltas[count] = value;
                count++;

                // Remove used bits
                bitAccumulator >>= precisionInBits;
                bitsInAccumulator -= precisionInBits;
            }
        }

        return deltas;
    }


    /**
     * Decode truncated samples.
     *
     * @param data The data to decode
     * @param precisionInBits The number of bits of 1 sample
     * @return The decoded samples
     */
    private static int [] decodeTruncatedBlock (final byte [] data, final int precisionInBits)
    {
        final int [] samples = new int [NUM_SAMPLES];
        int bitOffset = 0;

        int count = 0;
        while (bitOffset + precisionInBits <= data.length * 8)
        {
            final int byteOffset = bitOffset / 8;
            final int bitRemainder = bitOffset % 8;

            int value = 0;

            if (precisionInBits == 32)
            {
                // Calculate with long to prevent overflow

                long temp = 0;
                for (int i = 0; i < (precisionInBits + 7) / 8; i++)
                    temp |= (data[byteOffset + i] & 0xFF) << i * 8;

                value = (int) (temp >> bitRemainder & (1L << precisionInBits) - 1);
            }
            else
            {
                int temp = 0;
                for (int i = 0; i < (precisionInBits + 7) / 8; i++)
                    temp |= (data[byteOffset + i] & 0xFF) << i * 8;

                value = temp >> bitRemainder & (1 << precisionInBits) - 1;
                if ((value & 1 << precisionInBits - 1) != 0)
                    value |= ~0 << precisionInBits;
            }

            samples[count] = value;
            count++;

            bitOffset += precisionInBits;
        }

        return samples;
    }
}
