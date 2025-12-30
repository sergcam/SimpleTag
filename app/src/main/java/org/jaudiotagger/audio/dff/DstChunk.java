package org.jaudiotagger.audio.dff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * DSD Chunk
 */
public class DstChunk extends BaseChunk
{

    public DstChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        skipToChunkEnd(fc);
    }

    @Override
    public String toString()
    {
        return DffChunkType.ID3.getCode();
    }


}
