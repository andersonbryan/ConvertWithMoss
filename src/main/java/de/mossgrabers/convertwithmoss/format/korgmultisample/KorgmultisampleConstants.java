// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.korgmultisample;

/**
 * Tags and IDs used in the Korgmultisample format.
 *
 * @author Jürgen Moßgraber
 */
public class KorgmultisampleConstants
{
    /** The full 1st chunk. */
    public static final byte [] HEADER_CHUNK           =
    {
        0x08,
        0x01,
        0x12,
        0x12,
        0x0A,
        0x10,
        0x45,
        0x78,
        0x74,
        0x65,
        0x6E,
        0x64,
        0x65,
        0x64,
        0x46,
        0x69,
        0x6C,
        0x65,
        0x49,
        0x6E,
        0x66,
        0x6F,
        0x12,
        0x0F,
        0x0A,
        0x0B,
        0x4D,
        0x75,
        0x6C,
        0x74,
        0x69,
        0x53,
        0x61,
        0x6D,
        0x70,
        0x6C,
        0x65,
        0x18,
        0x01
    };

    /** Tag for Korg. */
    public static final String  TAG_KORG               = "Korg";
    /** Tag for File Info. */
    public static final String  TAG_FILE_INFO          = "ExtendedFileInfo";
    /** Tag for Multisample. */
    public static final String  TAG_MULTISAMPLE        = "MultiSample";
    /** Tag for Single Item. */
    public static final String  TAG_SINGLE_ITEM        = "SingleItem";
    /** Tag for Sample Builder. */
    public static final String  TAG_SAMPLE_BUILDER     = "Sample Builder";

    ////////////////////////////////////////////////////////////
    // Metadata in Chunk 2
    ////////////////////////////////////////////////////////////

    /** ID for editor version. */
    public static final int     ID_VERSION             = 0x1A;
    /** ID for timestamp. */
    public static final int     ID_TIME                = 0x21;
    /** ID for the application which created the file. */
    public static final int     ID_APPLICATION         = 0x32;
    /** ID for the version of application which created the file. */
    public static final int     ID_APPLICATION_VERSION = 0x3A;

    ////////////////////////////////////////////////////////////
    // Metadata in Chunk 3
    ////////////////////////////////////////////////////////////

    /** ID for Author. */
    public static final int     ID_AUTHOR              = 0x12;
    /** ID for Category. */
    public static final int     ID_CATEGORY            = 0x1A;
    /** ID for Comment. */
    public static final int     ID_COMMENT             = 0x22;
    /** ID for Sample. */
    public static final int     ID_SAMPLE              = 0x2A;
    /** ID for UUID. */
    public static final int     ID_UUID                = 0x3A;

    ////////////////////////////////////////////////////////////
    // Sample
    ////////////////////////////////////////////////////////////

    /** ID for Sample start. */
    public static final int     ID_START               = 0x10;
    /** ID for Sample Loop start. */
    public static final int     ID_LOOP_START          = 0x18;
    /** ID for Sample end. */
    public static final int     ID_END                 = 0x20;
    /** ID for Sample loop tuning start. */
    public static final int     ID_LOOP_TUNE           = 0x45;
    /** ID for Sample one-shot. */
    public static final int     ID_ONE_SHOT            = 0x48;
    /** ID for Sample volume boost. */
    public static final int     ID_BOOST_12DB          = 0x50;

    ////////////////////////////////////////////////////////////
    // Key Zone
    ////////////////////////////////////////////////////////////

    /** ID for Key Zone bottom key. */
    public static final int     ID_KEY_BOTTOM          = 0x10;
    /** ID for Key Zone top key. */
    public static final int     ID_KEY_TOP             = 0x18;
    /** ID for Key Zone root key. */
    public static final int     ID_KEY_ORIGINAL        = 0x20;
    /** ID for Key Zone fixed pitch. */
    public static final int     ID_FIXED_PITCH         = 0x28;
    /** ID for Key Zone sample tuning. */
    public static final int     ID_TUNE                = 0x35;
    /** ID for Key Zone level adjustment left channel. */
    public static final int     ID_LEVEL_LEFT          = 0x3D;
    /** ID for Key Zone level adjustment right channel . */
    public static final int     ID_LEVEL_RIGHT         = 0x45;
    /** ID for Key Zone color. */
    public static final int     ID_COLOR               = 0x50;


    /**
     * Private due to utility class.
     */
    private KorgmultisampleConstants ()
    {
        // Intentionally empty
    }
}
