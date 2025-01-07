// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2025
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.convertwithmoss.exception;

/**
 * Exception that indicates that only uncompressed WAV files are supported.
 *
 * @author Jürgen Moßgraber
 */
public class CompressionNotSupportedException extends Exception
{
    private static final long serialVersionUID = 263861724445830108L;


    /**
     * Constructor.
     *
     * @param message The message
     */
    public CompressionNotSupportedException (final String message)
    {
        super (message);
    }
}
