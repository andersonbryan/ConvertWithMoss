// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.format.nki.type;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

import de.mossgrabers.convertwithmoss.core.IMultisampleSource;
import de.mossgrabers.convertwithmoss.core.IPerformanceSource;
import de.mossgrabers.convertwithmoss.ui.IMetadataConfig;


/**
 * Interface to read/write a specific Kontakt format.
 *
 * @author Jürgen Moßgraber
 */
public interface IKontaktFormat
{
    /**
     * Read and parse a file which uses this format type from the given random access file.
     *
     * @param sourceFolder The top source folder for the detection
     * @param sourceFile The source file which contains the XML document
     * @param fileAccess The random access file to read from
     * @return The parsed multi-sample sources
     * @param metadataConfig Default metadata
     * @throws IOException Error reading the file
     */
    List<IMultisampleSource> readNKI (File sourceFolder, File sourceFile, RandomAccessFile fileAccess, IMetadataConfig metadataConfig) throws IOException;


    /**
     * Read and parse a file which uses this format type from the given random access file.
     *
     * @param sourceFolder The top source folder for the detection
     * @param sourceFile The source file which contains the XML document
     * @param fileAccess The random access file to read from
     * @return The parsed multi-sample sources
     * @param metadataConfig Default metadata
     * @throws IOException Error reading the file
     */
    IPerformanceSource readNKM (File sourceFolder, File sourceFile, RandomAccessFile fileAccess, IMetadataConfig metadataConfig) throws IOException;


    /**
     * Write a new NKI file from the given multi-sample source.
     *
     * @param out Where to write the data
     * @param safeSampleFolderName The folder where the samples are placed
     * @param multisampleSource The source
     * @param sizeOfSamples The size of all samples
     * @throws IOException Error writing the file
     */
    void writeNKI (OutputStream out, String safeSampleFolderName, IMultisampleSource multisampleSource, int sizeOfSamples) throws IOException;
}
