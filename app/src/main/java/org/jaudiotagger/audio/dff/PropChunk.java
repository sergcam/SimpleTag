package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.nio.ByteBuffer;

/**
 * PROP Chunk.
 */
public class PropChunk
{
    public static final int CHUNKSIZE_LENGTH = 8;
    public static final int SIGNATURE_LENGTH = 4;
    public static final int PROP_HEADER_LENGTH = SIGNATURE_LENGTH + CHUNKSIZE_LENGTH;

    public static PropChunk readChunk(ByteBuffer dataBuffer)
    {
        String type = Utils.readFourBytesAsChars(dataBuffer);
        if (DffChunkType.PROP.getCode().equals(type))
        {
            return new PropChunk(dataBuffer);
        }
        return null;
    }

    private PropChunk(ByteBuffer dataBuffer)
    {
    }

    @Override
    public String toString()
    {
        return DffChunkType.PROP.getCode();
    }
}
