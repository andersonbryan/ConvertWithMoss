// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.nki.type.nicontainer.chunkdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.mossgrabers.tools.StringUtils;


/**
 * A chunk which contains the data of a BNI preset.
 *
 * @author Jürgen Moßgraber
 */
public class BNIPresetChunkData extends AbstractChunkData
{
    private byte [] allBytes =
    {
        0,
        0
    };


    /** {@inheritDoc} */
    @Override
    public void read (final InputStream in) throws IOException
    {
        // Not used (0, 0)
        this.allBytes = in.readAllBytes ();
    }


    /** {@inheritDoc} */
    @Override
    public void write (final OutputStream out) throws IOException
    {
        out.write (this.allBytes);
    }


    /** {@inheritDoc} */
    @Override
    public String dump (final int level)
    {
        final int padding = level * 4;
        final StringBuilder sb = new StringBuilder ();
        sb.append (StringUtils.padLeftSpaces ("* Data: ", padding)).append (StringUtils.formatArray (this.allBytes)).append ('\n');
        return sb.toString ();
    }
}
