package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FS Chunk. Retrive samplerate.
 */
public class FsChunk extends BaseChunk
{
    private int sampleRate;

    public FsChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        ByteBuffer audioData = Utils.readFileDataIntoBufferLE(fc, 4);
        sampleRate = Integer.reverseBytes(audioData.getInt());

        skipToChunkEnd(fc);

    }

    /**
     * @return the sampleRate
     */
    public int getSampleRate()
    {
        return sampleRate;
    }

    @Override
    public String toString()
    {
        return DffChunkType.FS.getCode();
    }


}
