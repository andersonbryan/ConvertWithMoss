// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.nki.type.kontakt1;

import de.mossgrabers.convertwithmoss.format.nki.AbstractTagsAndAttributes;


/**
 * Tags used in the NKI NiSS XML structure.
 *
 * @author Jürgen Moßgraber
 * @author Philip Stolz
 */
public class NiSSTag extends AbstractTagsAndAttributes
{
    /** The optional root element. */
    public static final String NISS_ROOT_CONTAINER           = "NiSS_Bank";

    /** The program element. */
    public static final String NISS_PROGRAM                  = "NiSS_Program";

    /** A NiSS_Group element of a program. */
    public static final String NISS_GROUP                    = "NiSS_Group";

    /** A NiSS_Zone element of a program. */
    public static final String NISS_ZONE                     = "NiSS_Zone";

    /** The internal modulator element. */
    public static final String NISS_INT_MODULATOR_ELEMENT    = "NiSS_IntMod";

    /** The external modulator element. */
    public static final String NISS_EXT_MODULATOR_ELEMENT    = "NiSS_ExtMod";

    /** The sample file attribute. */
    public static final String NISS_SAMPLE_FILE_ATTRIBUTE    = "file";

    /** The extended sample file attribute. */
    public static final String NISS_SAMPLE_FILE_EX_ATTRIBUTE = "file_ex";

    /** The program volume parameter. */
    public static final String NISS_PROG_VOL_PARAM           = "masterVolume";

    /** The program tune parameter. */
    public static final String NISS_PROG_TUNE_PARAM          = "masterTune";

    /** The program pan parameter. */
    public static final String NISS_PROG_PAN_PARAM           = "masterPan";


    /** {@inheritDoc} */
    @Override
    public String program ()
    {
        return NISS_PROGRAM;
    }


    /** {@inheritDoc} */
    @Override
    public String group ()
    {
        return NISS_GROUP;
    }


    /** {@inheritDoc} */
    @Override
    public String zone ()
    {
        return NISS_ZONE;
    }


    /** {@inheritDoc} */
    @Override
    public String intModulatorElement ()
    {
        return NISS_INT_MODULATOR_ELEMENT;
    }


    /** {@inheritDoc} */
    @Override
    public String extModulatorElement ()
    {
        return NISS_EXT_MODULATOR_ELEMENT;
    }


    /** {@inheritDoc} */
    @Override
    public String sampleFileAttribute ()
    {
        return NISS_SAMPLE_FILE_ATTRIBUTE;
    }


    /** {@inheritDoc} */
    @Override
    public String sampleFileExAttribute ()
    {
        return NISS_SAMPLE_FILE_EX_ATTRIBUTE;
    }


    /** {@inheritDoc} */
    @Override
    public String progVolParam ()
    {
        return NISS_PROG_VOL_PARAM;
    }


    /** {@inheritDoc} */
    @Override
    public String progTuneParam ()
    {
        return NISS_PROG_TUNE_PARAM;
    }


    /** {@inheritDoc} */
    @Override
    public String progPanParam ()
    {
        return NISS_PROG_PAN_PARAM;
    }


    /** {@inheritDoc} */
    @Override
    public String rootContainer ()
    {
        return NISS_ROOT_CONTAINER;
    }


    /** {@inheritDoc} */
    @Override
    public double calculateTune (final double zoneTune, final double groupTune, final double progTune)
    {
        // All three tune values are stored logarithmically
        final double value = 12.0 * Math.log (zoneTune * groupTune * progTune) / Math.log (2);
        return Math.round (value * 100000) / 100000.0;
    }
}
