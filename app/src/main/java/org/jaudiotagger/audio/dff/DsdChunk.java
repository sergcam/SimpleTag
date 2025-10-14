package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.dsf.DsfChunkType;
import org.jaudiotagger.audio.generic.Utils;

import java.nio.ByteBuffer;

/**
 * DSD Chunk
 */
public class DsdChunk
{
    public static final int CHUNKSIZE_LENGTH = 8;
    public static final int SIGNATURE_LENGTH = 4;

    public static final int DSD_HEADER_LENGTH = CHUNKSIZE_LENGTH;

    public static DsdChunk readChunk(ByteBuffer dataBuffer)
    {
        String type = Utils.readFourBytesAsChars(dataBuffer);
        if (DsfChunkType.DSD.getCode().equals(type))
        {
            return new DsdChunk(dataBuffer);
        }
        return null;
    }

    private DsdChunk(ByteBuffer dataBuffer)
    {

    }

    @Override
    public String toString()
    {
        return DffChunkType.DSD.getCode();
    }
}
