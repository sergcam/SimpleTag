package org.jaudiotagger.audio.exceptions;

import java.io.IOException;

/**
 * Should be thrown when unable to create a file when it is expected it should be creatable. For example because
 * you dont have permission to write to the folder that it is in.
 */
public class UnableToCreateFileException extends IOException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3390375837765957908L;

	public UnableToCreateFileException(String message)
    {
        super(message);
    }
}
