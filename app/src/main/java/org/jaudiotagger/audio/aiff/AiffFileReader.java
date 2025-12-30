package org.jaudiotagger.audio.aiff;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.AudioFileReader2;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Reads Audio and Metadata information contained in Aiff file.
 */
public class AiffFileReader extends AudioFileReader2
{
    @Override
    protected GenericAudioHeader getEncodingInfo(Path path) throws CannotReadException, IOException
    {
        return new AiffInfoReader(path.toString()).read(path);
    }

    @Override
    protected Tag getTag(Path path) throws CannotReadException, IOException
    {
        return new AiffTagReader(path.toString()).read(path);
    }
}
