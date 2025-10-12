package org.jaudiotagger.utils;

import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/** Shift Data to allow metadata to be fitted inside existing file
 */
public class ShiftData
{
    /**
     * Shift the remainder of data from current position to position + offset
     * Reads/writes starting from end of file in chunks so works on large files on low memory systems
     *
     * @param  fc
     * @param  offset (if negative writes the data earlier (i,e smaller file)
     * @throws IOException
     * @throws CannotWriteException
     */
    public static void shiftDataByOffsetToMakeSpace(SeekableByteChannel fc, int offset) throws IOException
    {
        long origFileSize = fc.size();
        long startPos = fc.position();
        long amountToBeWritten = fc.size() - startPos;
        int chunkSize = (int) TagOptionSingleton.getInstance().getWriteChunkSize();
        long count = amountToBeWritten / chunkSize;
        long mod = amountToBeWritten % chunkSize;

        //Buffer to hold a chunk
        ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        //Start from end of file
        long readPos = fc.size() - chunkSize;
        long writePos = (fc.size() - chunkSize) + offset;

        for (int i = 0; i < count; i++)
        {
            //Read Data Into Buffer starting from end of file
            fc.position(readPos);
            fc.read(chunkBuffer);

            //Now write to new location
            chunkBuffer.flip();
            fc.position(writePos);
            fc.write(chunkBuffer);

            //Rewind so can use in next iteration of loop
            chunkBuffer.rewind();

            readPos -= chunkSize;
            writePos -= chunkSize;
        }

        if (mod > 0)
        {
            chunkBuffer = ByteBuffer.allocate((int) mod);
            fc.position(startPos);
            fc.read(chunkBuffer);

            //Now write to new location
            chunkBuffer.flip();
            fc.position(startPos + offset);
            fc.write(chunkBuffer);
        }

        if(fc instanceof SeekableByteChannel)
        {
            if(offset < 0)
            {
                fc.truncate(origFileSize + offset);
            }
        }
    }

    /**
     * Used by ID3 to shrink space by shrinkBy bytes before current position
     * @param fc
     * @param shrinkBy
     * @throws IOException
     */
    public static void shiftDataByOffsetToShrinkSpace(SeekableByteChannel fc, int shrinkBy) throws IOException
    {
        long startPos = fc.position();
        long amountToBeWritten = fc.size() - startPos;
        int chunkSize = (int) TagOptionSingleton.getInstance().getWriteChunkSize();
        long count = amountToBeWritten / chunkSize;
        long mod = amountToBeWritten % chunkSize;

        //Buffer to hold a chunk
        ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);

        //Start from start of data that needs to be shifted
        long readPos  = startPos;
        long writePos = startPos - shrinkBy;

        for (int i = 0; i < count; i++)
        {
            //Read Data Into Buffer starting from start of data that has to be copied
            fc.position(readPos);
            fc.read(chunkBuffer);

            //Now write to new location
            chunkBuffer.flip();
            fc.position(writePos);
            fc.write(chunkBuffer);

            //Rewind so can use in next iteration of loop
            chunkBuffer.rewind();

            readPos += chunkSize;
            writePos += chunkSize;
        }

        if (mod > 0)
        {
            chunkBuffer = ByteBuffer.allocate((int) mod);
            fc.position(readPos);
            fc.read(chunkBuffer);

            //Now write to new location
            chunkBuffer.flip();
            fc.position(writePos);
            fc.write(chunkBuffer);
        }

        if(fc instanceof SeekableByteChannel)
        {
            fc.truncate(fc.position());
        }
    }
}
