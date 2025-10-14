package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.generic.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * CMPR Chunk. Retrieve compression.
 */
public class CmprChunk extends BaseChunk
{
    private String compression;
    private String description;

    public CmprChunk(ByteBuffer dataBuffer)
    {
        super(dataBuffer);
    }

    @Override
    public void readDataChunch(FileChannel fc) throws IOException
    {

        super.readDataChunch(fc);

        ByteBuffer audioData = Utils.readFileDataIntoBufferLE(fc, 4);
        compression = Utils.readFourBytesAsChars(audioData);

        audioData = Utils.readFileDataIntoBufferLE(fc, 1);

        byte b = audioData.get();
        int blen = b & 255;

        audioData = Utils.readFileDataIntoBufferLE(fc, blen);
        byte[] buff = new byte[blen];

        audioData.get(buff);
        description = new String(buff, ISO_8859_1);

        //System.out.println(" new postion: "+fc.position());

        skipToChunkEnd(fc);
    }

    /**
     * @return the compression
     */
    public String getCompression()
    {
        return compression;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return DffChunkType.CMPR.getCode();
    }


}
