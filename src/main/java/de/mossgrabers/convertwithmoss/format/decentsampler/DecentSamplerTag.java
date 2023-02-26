// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2023
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.decentsampler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * The Decent Sampler preset format consists of several XML tags.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DecentSamplerTag
{
    /** The root tag. */
    public static final String                    DECENTSAMPLER   = "DecentSampler";

    /** The effects tag. */
    public static final String                    EFFECTS         = "effects";
    /** The modulation effects tag. */
    public static final String                    MOD_EFFECT      = "effect";
    /** The effects tag. */
    public static final String                    EFFECTS_EFFECT  = "effect";

    /** The user interface tag. */
    public static final String                    UI              = "ui";
    /** The tabulator tag. */
    public static final String                    TAB             = "tab";
    /** The labeled knob tag. */
    public static final String                    LABELED_KNOB    = "labeled-knob";
    /** The binding tag. */
    public static final String                    BINDING         = "binding";
    /** The tags tag. */
    public static final String                    TAGS            = "tags";
    /** The tag tag. */
    public static final String                    TAG             = "tag";

    /** The groups tag. */
    public static final String                    GROUPS          = "groups";
    /** The groups tag. */
    public static final String                    GLOBAL_TUNING   = "globalTuning";
    /** The group tag. */
    public static final String                    GROUP           = "group";
    /** The sample tag. */
    public static final String                    SAMPLE          = "sample";
    /** The sequence mode tag. */
    public static final String                    SEQ_MODE        = "seqMode";

    /** The global tuning attribute. */
    public static final String                    GROUP_TUNING    = "groupTuning";
    /** The group name tag. */
    public static final String                    GROUP_NAME      = "name";

    /** The sample-key tag. */
    public static final String                    PATH            = "path";
    /** The volume tag on different levels. */
    public static final String                    VOLUME          = "volume";
    /** The start tag sample attribute. */
    public static final String                    START           = "start";
    /** The end tag sample attribute. */
    public static final String                    END             = "end";
    /** The tuning tag sample attribute. */
    public static final String                    TUNING          = "tuning";
    /** The sequence position tag sample attribute. */
    public static final String                    SEQ_POSITION    = "seqPosition";
    /** The root note tag sample attribute. */
    public static final String                    ROOT_NOTE       = "rootNote";
    /** The pitch key tracking tag sample attribute. */
    public static final String                    PITCH_KEY_TRACK = "pitchKeyTrack";
    /** The trigger group / sample attribute. */
    public static final String                    TRIGGER         = "trigger";
    /** The low note tag sample attribute. */
    public static final String                    LO_NOTE         = "loNote";
    /** The high note tag sample attribute. */
    public static final String                    HI_NOTE         = "hiNote";
    /** The low velocity tag sample attribute. */
    public static final String                    LO_VEL          = "loVel";
    /** The high velocity tag sample attribute. */
    public static final String                    HI_VEL          = "hiVel";

    /** The loop enabled tag sample attribute. */
    public static final String                    LOOP_ENABLED    = "loopEnabled";
    /** The loop start tag sample attribute. */
    public static final String                    LOOP_START      = "loopStart";
    /** The loop end tag sample attribute. */
    public static final String                    LOOP_END        = "loopEnd";
    /** The loop crossfade tag sample attribute. */
    public static final String                    LOOP_CROSSFADE  = "loopCrossfade";

    /** The amplitude envelope attack attribute. */
    public static final String                    AMP_ENV_ATTACK  = "attack";
    /** The amplitude envelope decay attribute. */
    public static final String                    AMP_ENV_DECAY   = "decay";
    /** The amplitude envelope sustain attribute. */
    public static final String                    AMP_ENV_SUSTAIN = "sustain";
    /** The amplitude envelope release attribute. */
    public static final String                    AMP_ENV_RELEASE = "release";

    /** The supported top level tags. */
    public static final Set<String>               TOP_LEVEL_TAGS  = Set.of (EFFECTS, UI, GROUPS);
    /** The supported group tags. */
    public static final Set<String>               GROUP_TAGS      = Set.of (SAMPLE);
    /** The supported sample tags. */
    public static final Set<String>               SAMPLE_TAGS     = Set.of (PATH);

    /** Supported attributes of all tags. */
    private static final Map<String, Set<String>> ATTRIBUTES      = new HashMap<> ();

    static
    {
        ATTRIBUTES.put (DECENTSAMPLER, Collections.emptySet ());
        ATTRIBUTES.put (GROUPS, Set.of (GLOBAL_TUNING, SEQ_MODE, AMP_ENV_ATTACK, AMP_ENV_DECAY, AMP_ENV_SUSTAIN, AMP_ENV_RELEASE));
        ATTRIBUTES.put (GROUP, Set.of (GROUP_NAME, SEQ_POSITION, GROUP_TUNING, TUNING, VOLUME, AMP_ENV_ATTACK, AMP_ENV_DECAY, AMP_ENV_SUSTAIN, AMP_ENV_RELEASE, TRIGGER));
        ATTRIBUTES.put (SAMPLE, Set.of (PATH, ROOT_NOTE, LO_NOTE, HI_NOTE, LO_VEL, HI_VEL, START, END, TUNING, VOLUME, PITCH_KEY_TRACK, TRIGGER, LOOP_START, LOOP_END, LOOP_CROSSFADE, LOOP_ENABLED, AMP_ENV_ATTACK, AMP_ENV_DECAY, AMP_ENV_SUSTAIN, AMP_ENV_RELEASE));
    }


    /**
     * Private constructor for utility class.
     */
    private DecentSamplerTag ()
    {
        // Intentionally empty
    }


    /**
     * Get the supported attributes of a tag.
     *
     * @param tagName The name of the tag
     * @return The tags
     */
    public static Set<String> getAttributes (final String tagName)
    {
        return ATTRIBUTES.get (tagName);
    }
}
