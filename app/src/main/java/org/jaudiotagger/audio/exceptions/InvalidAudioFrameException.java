/**
 * @author : Paul Taylor
 *
 * Version @version:$Id$
 * Date :${DATE}
 *
 * Jaikoz Copyright Copyright (C) 2003 -2005 JThink Ltd
 */
package org.jaudiotagger.audio.exceptions;

/**
 * Thrown if portion of file thought to be an AudioFrame is found to not be.
 */
public class InvalidAudioFrameException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 7213597113547233971L;

	public InvalidAudioFrameException(String message)
    {
        super(message);
    }
}
