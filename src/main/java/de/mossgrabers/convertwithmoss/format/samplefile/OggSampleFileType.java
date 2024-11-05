// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.samplefile;

import java.util.List;

import de.mossgrabers.convertwithmoss.core.model.IFileBasedSampleData;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;


/**
 * Detects recursively wave files in folders, which can be the source for a multi-sample. Wave files
 * must end with <i>.wav</i>. All wave files in a folder are considered to belong to one
 * multi-sample.
 *
 * @author Jürgen Moßgraber
 */
public class OggSampleFileType implements SampleFileType
{
    private static final String [] ENDINGS = new String []
    {
        ".ogg"
    };


    /** {@inheritDoc} */
    @Override
    public String getName ()
    {
        return "OGG (*.ogg)";
    }


    /** {@inheritDoc} */
    @Override
    public String [] getFileEndings ()
    {
        return ENDINGS;
    }


    /** {@inheritDoc} */
    @Override
    public void fillInstrumentData (final ISampleZone zone, final IFileBasedSampleData sampleData)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public boolean hasInstrumentData (final List<IFileBasedSampleData> sampleData)
    {
        return false;
    }
}