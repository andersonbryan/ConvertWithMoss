// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.samplefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.INotifier;
import de.mossgrabers.convertwithmoss.core.detector.AbstractDetectorTask;
import de.mossgrabers.convertwithmoss.core.detector.DefaultMultisampleSource;
import de.mossgrabers.convertwithmoss.core.model.IFileBasedSampleData;
import de.mossgrabers.convertwithmoss.core.model.IGroup;
import de.mossgrabers.convertwithmoss.core.model.IMetadata;
import de.mossgrabers.convertwithmoss.core.model.ISampleZone;
import de.mossgrabers.convertwithmoss.core.model.implementation.DefaultGroup;
import de.mossgrabers.convertwithmoss.core.model.implementation.DefaultSampleZone;
import de.mossgrabers.convertwithmoss.exception.CombinationNotPossibleException;
import de.mossgrabers.convertwithmoss.exception.MultisampleException;
import de.mossgrabers.convertwithmoss.file.AudioFileUtils;
import de.mossgrabers.convertwithmoss.format.KeyMapping;
import de.mossgrabers.convertwithmoss.ui.IMetadataConfig;
import de.mossgrabers.tools.FileUtils;


/**
 * Base class to detect recursively sample files in folders (e.g. WAV or AIFF), which can be the
 * source for a multi-sample. All sample files in a folder which have the same format are considered
 * to belong to one multi-sample.
 *
 * @author Jürgen Moßgraber
 */
public class SampleFileDetectorTask extends AbstractDetectorTask
{
    private final List<SampleFileType> sampleFileTypes;
    private final int                  crossfadeNotes;
    private final int                  crossfadeVelocities;
    private final String []            groupPatterns;
    private final boolean              isAscending;
    private final String []            monoSplitPatterns;
    private final String []            postfixTexts;
    private final boolean              ignoreLoops;


    /**
     * Configure the detector.
     *
     * @param notifier The notifier
     * @param consumer The consumer that handles the detected multi-sample sources
     * @param sourceFolder The top source folder for the detection
     * @param groupPatterns Detection patterns for groups
     * @param isAscending Are groups ordered ascending?
     * @param monoSplitPatterns Detection pattern for mono splits (to be combined to stereo files)
     * @param postfixTexts Post-fix text to remove
     * @param crossfadeNotes Number of notes to cross-fade
     * @param crossfadeVelocities The number of velocity steps to cross-fade ranges
     * @param metadata Additional metadata configuration parameters
     * @param sampleFileTypes The file endings of the sample files to look for
     * @param ignoreLoops Should loops be ignored?
     */
    public SampleFileDetectorTask (final INotifier notifier, final Consumer<IMultisampleSource> consumer, final File sourceFolder, final String [] groupPatterns, final boolean isAscending, final String [] monoSplitPatterns, final String [] postfixTexts, final int crossfadeNotes, final int crossfadeVelocities, final IMetadataConfig metadata, final List<SampleFileType> sampleFileTypes, final boolean ignoreLoops)
    {
        super (notifier, consumer, sourceFolder, metadata);

        this.sampleFileTypes = sampleFileTypes;
        this.groupPatterns = groupPatterns;
        this.isAscending = isAscending;
        this.monoSplitPatterns = monoSplitPatterns;
        this.postfixTexts = postfixTexts;
        this.crossfadeNotes = crossfadeNotes;
        this.crossfadeVelocities = crossfadeVelocities;
        this.ignoreLoops = ignoreLoops;
    }


    /** {@inheritDoc} */
    @Override
    protected void detect (final File folder)
    {
        this.notifier.log ("IDS_NOTIFY_ANALYZING", folder.getAbsolutePath ());
        if (this.waitForDelivery ())
            return;

        final List<IMultisampleSource> multisample = this.readPresetFile (folder);
        if (multisample.isEmpty ())
            return;

        // Check for task cancellation
        if (!this.isCancelled ())
            this.multisampleSourceConsumer.accept (multisample.get (0));
    }


    /** {@inheritDoc} */
    @Override
    protected List<IMultisampleSource> readPresetFile (final File folder)
    {
        final List<IMultisampleSource> sources = new ArrayList<> ();

        for (final SampleFileType sampleFileType: this.sampleFileTypes)
        {
            this.fileEndings = sampleFileType.getFileEndings ();

            final File [] files = this.listFiles (folder, this.fileEndings);
            if (files.length == 0)
                continue;

            this.notifier.log ("IDS_NOTIFY_FOUND_RAW_FILES", Integer.toString (files.length), sampleFileType.getName ());

            // Analyze all files
            int outputCount = 0;
            final List<IFileBasedSampleData> sampleData = new ArrayList<> (files.length);
            for (final File file: files)
            {
                // Check for task cancellation
                if (this.isCancelled ())
                    return Collections.emptyList ();

                try
                {
                    sampleData.add (createSampleData (file, this.notifier));
                    this.notifyProgress ();
                    outputCount++;
                    if (outputCount % 80 == 0)
                        this.notifyNewline ();
                }
                catch (final IOException ex)
                {
                    this.notifier.logError ("IDS_NOTIFY_SKIPPED", folder.getAbsolutePath (), file.getAbsolutePath (), ex.getMessage ());
                    return Collections.emptyList ();
                }
            }

            this.notifyNewline ();

            sources.addAll (this.createMultisample (sampleFileType, folder, sampleData));
        }

        return sources;
    }


    /**
     * Detect metadata, order samples and finally create the multi-sample.
     *
     * @param sampleFileType The sample file type
     * @param folder The folder which contains the sample files
     * @param sampleData The detected sample files
     * @return The multi-sample
     */
    private List<IMultisampleSource> createMultisample (final SampleFileType sampleFileType, final File folder, final List<IFileBasedSampleData> sampleData)
    {
        if (sampleData.isEmpty ())
            return Collections.emptyList ();

        try
        {
            // If there are instrument chunks, use them otherwise create the multi-sample from the
            // file names
            final List<IGroup> groups;
            String name;
            if (sampleFileType.hasInstrumentData (sampleData))
            {
                final IGroup group = new DefaultGroup ("Group");
                groups = Collections.singletonList (group);
                final List<String> filenames = new ArrayList<> (sampleData.size ());
                for (final IFileBasedSampleData fileSampleData: sampleData)
                {
                    final DefaultSampleZone sampleZone = new DefaultSampleZone (FileUtils.getNameWithoutType (new File (fileSampleData.getFilename ())), fileSampleData);
                    group.addSampleZone (sampleZone);
                    sampleFileType.fillInstrumentData (sampleZone, fileSampleData);
                    filenames.add (new File (fileSampleData.getFilename ()).getName ());
                }
                name = KeyMapping.findCommonPrefix (filenames);
                if (name.isBlank ())
                    name = folder.getName ();
            }
            else
            {
                final KeyMapping keyMapping = new KeyMapping (new ArrayList<> (sampleData), this.isAscending, this.crossfadeNotes, this.crossfadeVelocities, this.groupPatterns, this.monoSplitPatterns);
                if (this.metadataConfig.isPreferFolderName ())
                {
                    name = cleanupName (folder.getName (), this.postfixTexts);
                    if (name.isBlank ())
                        name = keyMapping.getName ();
                }
                else
                    name = keyMapping.getName ();
                groups = keyMapping.getSampleMetadata ();
            }

            if (name.isBlank ())
            {
                this.notifier.logError ("IDS_NOTIFY_NO_NAME");
                name = FileUtils.getNameWithoutType (sampleData.get (0).getFilename ());
            }

            name = cleanupName (name, this.postfixTexts);

            final String [] parts = this.createParts (folder, name);
            final DefaultMultisampleSource multisampleSource = new DefaultMultisampleSource (folder, parts, name, AudioFileUtils.subtractPaths (this.sourceFolder, folder));
            final IMetadata metadata = multisampleSource.getMetadata ();
            this.createMetadata (metadata, sampleData, parts);
            this.updateCreationDateTime (metadata, new File (sampleData.get (0).getFilename ()));
            multisampleSource.setGroups (groups);

            for (final IGroup group: groups)
                for (final ISampleZone zone: group.getSampleZones ())
                    zone.getSampleData ().addZoneData (zone, true, true);

            // Remove all loops if requested
            if (this.ignoreLoops)
            {
                for (final IGroup group: groups)
                    for (final ISampleZone zone: group.getSampleZones ())
                        zone.getLoops ().clear ();
            }

            this.notifier.log ("IDS_NOTIFY_DETECTED_GROUPS", Integer.toString (groups.size ()));
            if (this.waitForDelivery ())
                return Collections.emptyList ();

            return Collections.singletonList (multisampleSource);
        }
        catch (final IOException | MultisampleException | CombinationNotPossibleException ex)
        {
            this.notifier.logError ("IDS_NOTIFY_SAVE_FAILED", ex);
        }

        return Collections.emptyList ();
    }


    /**
     * Tests if the name is not empty and removes configured post-fix texts.
     *
     * @param name The name to check
     * @param postfixTexts The post-fix texts to remove
     * @return The cleaned up name or null if it is empty
     */
    private static String cleanupName (final String name, final String... postfixTexts)
    {
        String n = name;
        for (final String pt: postfixTexts)
            if (name.endsWith (pt))
            {
                n = n.substring (0, n.length () - pt.length ());
                break;
            }
        n = n.trim ();
        if (n.endsWith ("-") || n.endsWith ("_"))
            n = n.substring (0, n.length () - 1);
        return n;
    }


    private String [] createParts (final File folder, final String name)
    {
        final String [] parts = AudioFileUtils.createPathParts (folder, this.sourceFolder, name);
        if (parts.length <= 1)
            return parts;

        // Remove the samples folder
        final List<String> subpaths = new ArrayList<> (Arrays.asList (parts));
        subpaths.remove (1);
        return subpaths.toArray (new String [subpaths.size ()]);
    }
}
