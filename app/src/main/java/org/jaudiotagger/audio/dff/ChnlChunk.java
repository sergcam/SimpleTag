package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * CHNL Chunk. Retrive channels info.
 */
public class ChnlChunk extends BaseChunk
{
    private short numChannels;
    String IDs[];

    public ChnlChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        ByteBuffer audioData = Utils.readFileDataIntoBufferLE(fc, 2);
        numChannels = Short.reverseBytes(audioData.getShort());
        //System.out.println(" numChannels: "+numChannels);

        //System.out.println(" new postion: "+fc.position());

        IDs = new String[numChannels];
        for (int i = 0; i < numChannels; i++)
        {
            audioData = Utils.readFileDataIntoBufferLE(fc, 4);
            IDs[i] = Utils.readFourBytesAsChars(audioData);
        }
        //System.out.printf("Channels %s%n\n", Arrays.toString(IDs));
        //System.out.println(" new postion: "+fc.position());

        skipToChunkEnd(fc);
    }

    /**
     * @return the sampleRate
     */
    public Short getNumChannels()
    {
        return numChannels;
    }

    @Override
    public String toString()
    {
        return DffChunkType.CHNL.getCode();
    }


}
