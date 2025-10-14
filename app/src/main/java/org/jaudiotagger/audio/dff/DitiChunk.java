package org.jaudiotagger.audio.dff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * DITI Chunk. Carry the Title.
 */
public class DitiChunk extends BaseChunk
{

    public DitiChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        //skipToChunkEnd(fc);
    }

    @Override
    public String toString()
    {
        return DffChunkType.DITI.getCode();
    }


}
