package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FS Chunk. Retrive samplerate.
 */
public class FrteChunk extends BaseChunk
{

    private int numFrames;
    private Short rate;

    public FrteChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        ByteBuffer audioData = Utils.readFileDataIntoBufferLE(fc, 4);
        numFrames = Integer.reverseBytes(audioData.getInt());

        audioData = Utils.readFileDataIntoBufferLE(fc, 2);
        rate = Short.reverseBytes(audioData.getShort());

        skipToChunkEnd(fc);

    }

    /**
     * @return the numFrames
     */
    public int getNumFrames()
    {
        return numFrames;
    }

    /**
     * @return the rate
     */
    public Short getRate()
    {
        return rate;
    }

    @Override
    public String toString()
    {
        return DffChunkType.FRTE.getCode();
    }
}

    
