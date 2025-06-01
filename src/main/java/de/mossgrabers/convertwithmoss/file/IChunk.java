// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.file;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Interface to a chunk.
 *
 * @author Jürgen Moßgraber
 */
public interface IChunk
{
    /**
     * Get the chunk ID.
     *
     * @return The id
     */
    int getId ();


    /**
     * Get the size of the data contained in the chunk.
     *
     * @return The size in bytes
     */
    long getDataSize ();


    /**
     * Writes the data to the given output stream.
     *
     * @param out Where to write the data to
     * @throws IOException Could not write the data
     */
    void writeData (OutputStream out) throws IOException;


    /**
     * Write the chunk to an output stream.
     *
     * @param out The stream to write to
     * @throws IOException Could not write the chunk
     */
    void write (OutputStream out) throws IOException;


    /**
     * Format all values as a string for dumping it out.
     *
     * @return The formatted string
     */
    String infoText ();


    /**
     * Converts the first four letters of the string into an RIFF Identifier.
     *
     * @param text The string to be converted
     * @return ID representation of the string
     */
    public static int toId (final String text)
    {
        final byte [] bytes = text.getBytes ();
        return bytes[0] << 24 | bytes[1] << 16 | bytes[2] << 8 | bytes[3];
    }
}
