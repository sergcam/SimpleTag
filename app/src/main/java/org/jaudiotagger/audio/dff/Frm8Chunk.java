package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.nio.ByteBuffer;

/**
 * DSD Chunk
 */
public class Frm8Chunk
{
    public static final int SIGNATURE_LENGTH = 4;
    public static final int CHUNKSIZE_LENGTH = 8;

    public static final int FRM8_HEADER_LENGTH = SIGNATURE_LENGTH + CHUNKSIZE_LENGTH;

    public static Frm8Chunk readChunk(ByteBuffer dataBuffer)
    {
        String frm8Signature = Utils.readFourBytesAsChars(dataBuffer);

        if (!DffChunkType.FRM8.getCode().equals(frm8Signature))
        {

            return null;
        }

        return new Frm8Chunk(dataBuffer);
    }

    private Frm8Chunk(ByteBuffer dataBuffer)
    {

    }

    @Override
    public String toString()
    {
        return DffChunkType.FRM8.getCode();
    }
}
