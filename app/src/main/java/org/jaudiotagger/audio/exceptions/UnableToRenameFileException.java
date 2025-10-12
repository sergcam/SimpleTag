package org.jaudiotagger.audio.exceptions;

import java.io.IOException;

/**
 * Should be thrown when unable to rename a file when it is expected it should rename. For example could occur on Vista
 * because you do not have Special Permission 'Delete' set to Denied.
 */
public class UnableToRenameFileException extends IOException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3942088615944301367L;

	public UnableToRenameFileException(String message)
    {
        super(message);
    }
}
