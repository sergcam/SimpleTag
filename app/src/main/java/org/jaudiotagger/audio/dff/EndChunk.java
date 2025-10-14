package org.jaudiotagger.audio.dff;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * DSD Chunk
 */
public class EndChunk extends BaseChunk
{
    private Long dataEnd;

    public EndChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);
        dataEnd = this.getChunkEnd();

        //skipToChunkEnd(fc);

    }

    /**
     * @return the point where data starts
     */
    public Long getDataStart()
    {
        return this.getChunkStart();
    }

    /**
     * @return the dataEnd (should be the end of file)
     */
    public Long getDataEnd()
    {
        return dataEnd;
    }

    @Override
    public String toString()
    {
        return DffChunkType.END.getCode() + " (END)";
    }


}
