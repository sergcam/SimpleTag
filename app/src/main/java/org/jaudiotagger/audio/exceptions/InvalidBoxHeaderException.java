package org.jaudiotagger.audio.exceptions;

/**
 * Thrown if when trying to read box id the length doesn't make any sense
 */
public class InvalidBoxHeaderException extends RuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8797541836152099722L;

	public InvalidBoxHeaderException(String message)
    {
        super(message);
    }
}
