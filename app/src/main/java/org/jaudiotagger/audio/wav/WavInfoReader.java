/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.audio.wav;

import org.jaudiotagger.audio.SupportedFileFormat;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.iff.Chunk;
import org.jaudiotagger.audio.iff.ChunkHeader;
import org.jaudiotagger.audio.iff.IffHeaderChunk;
import org.jaudiotagger.audio.wav.chunk.WavCorruptChunkType;
import org.jaudiotagger.audio.wav.chunk.WavFactChunk;
import org.jaudiotagger.audio.wav.chunk.WavFormatChunk;
import org.jaudiotagger.logging.Hex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Read the Wav file chunks, until finds WavFormatChunk and then generates AudioHeader from it
 */
public class WavInfoReader
{
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.wav");
    private String loggingName;

    //So if we encounter bad chunk we know if we have managed to find good audio chunks first
    private boolean isFoundAudio   = false;
    private boolean isFoundFormat  = false;

    public WavInfoReader(String loggingName)
    {
        this.loggingName = loggingName;
    }

    public GenericAudioHeader read(Path path) throws CannotReadException, IOException
    {
        GenericAudioHeader info = new GenericAudioHeader();
        try(FileChannel fc = FileChannel.open(path))
        {
            if(WavRIFFHeader.isValidHeader(loggingName, fc))
            {
                while (fc.position() < fc.size())
                {
                    //Problem reading chunk and no way to workround it so exit loop
                    if (!readChunk(fc, info))
                    {
                        break;
                    }
                }
            }
            else
            {
                throw new CannotReadException(loggingName + " Wav RIFF Header not valid");
            }
        }

        if(isFoundFormat && isFoundAudio)
        {
            info.setFormat(SupportedFileFormat.WAV.getDisplayName());
            info.setLossless(true);
            calculateTrackLength(info);
            return info;
        }
        else
        {
            throw new CannotReadException(loggingName + " Unable to safetly read chunks for this file, appears to be corrupt");
        }
    }

    /**
     * Calculate track length, done it here because requires data from multiple chunks
     *
     * @param info
     * @throws CannotReadException
     */
    private void calculateTrackLength(GenericAudioHeader info) throws CannotReadException
    {
        //If we have fact chunk we can calculate accurately by taking total of samples (per channel) divided by the number
        //of samples taken per second (per channel)
        if(info.getNoOfSamples()!=null)
        {
            if(info.getSampleRateAsNumber()>0)
            {
                info.setPreciseLength((float)info.getNoOfSamples() / info.getSampleRateAsNumber());
            }
        }
        //Otherwise adequate to divide the total number of sampling bytes by the average byte rate
        else if(info.getAudioDataLength()> 0)
        {
            info.setPreciseLength((float)info.getAudioDataLength() / info.getByteRate());
        }
        else
        {
            throw new CannotReadException(loggingName + " Wav Data Header Missing");
        }
    }

    /**
     * Reads a Wav Chunk.
     */
    protected boolean readChunk(FileChannel fc, GenericAudioHeader info) throws IOException, CannotReadException
    {
        Chunk chunk;
        ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        if (!chunkHeader.readHeader(fc))
        {
            return false;
        }

        String id = chunkHeader.getID();
        logger.info(loggingName + " Reading Chunk:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
        final WavChunkType chunkType = WavChunkType.get(id);

        //If known chunkType
        if (chunkType != null)
        {
            switch (chunkType)
            {
                case FACT:
                {
                    ByteBuffer fmtChunkData = Utils.readFileDataIntoBufferLE(fc, (int) chunkHeader.getSize());
                    chunk = new WavFactChunk(fmtChunkData, chunkHeader, info);
                    if (!chunk.readChunk())
                    {
                        return false;
                    }
                    break;
                }

                case DATA:
                {
                    //We just need this value from header dont actually need to read data itself
                    info.setAudioDataLength(chunkHeader.getSize());
                    info.setAudioDataStartPosition(fc.position());
                    info.setAudioDataEndPosition(fc.position() + chunkHeader.getSize());
                    fc.position(fc.position() + chunkHeader.getSize());
                    isFoundAudio = true;
                    break;
                }

                case FORMAT:
                {
                    ByteBuffer fmtChunkData = Utils.readFileDataIntoBufferLE(fc, (int) chunkHeader.getSize());
                    chunk = new WavFormatChunk(fmtChunkData, chunkHeader, info);
                    if (!chunk.readChunk())
                    {
                        return false;
                    }
                    isFoundFormat = true;
                    break;
                }

                //Dont need to do anything with these just skip
                default:
                    if(fc.position() + chunkHeader.getSize() <= fc.size())
                    {
                        fc.position(fc.position() + chunkHeader.getSize());
                    }
                    else
                    {
                        if(isFoundAudio && isFoundFormat)
                        {
                            logger.severe(loggingName + " Size of Chunk Header larger than data, skipping to file end:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                            fc.position(fc.size());
                        }
                        else
                        {
                            logger.severe(loggingName + " Size of Chunk Header larger than data, cannot read file");
                            throw new CannotReadException(loggingName + " Size of Chunk Header larger than data, cannot read file");
                        }
                    }
            }
        }
        //Alignment problem that we can workround by going back one and retrying
        else if(id.substring(1,3).equals(WavCorruptChunkType.CORRUPT_LIST_EARLY.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt LIST Chunk, starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());
            fc.position(fc.position() -  (ChunkHeader.CHUNK_HEADER_SIZE - 1));
            return true;
        }
        //Alignment problem that we can workround by going forward one and retrying
        else if(id.substring(0,3).equals(WavCorruptChunkType.CORRUPT_LIST_LATE.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt LIST Chunk (2), starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());
            fc.position(fc.position() -  (ChunkHeader.CHUNK_HEADER_SIZE + 1));
            return true;
        }
        //Null Padding Detection (strictly invalid but seems to happen some time
        else if(id.equals("\0\0\0\0") && chunkHeader.getSize() == 0)
        {
            //Carry on reading until not null (TODO check not long)
            int fileRemainder = (int)((fc.size() - fc.position()));
            ByteBuffer restOfFile = ByteBuffer.allocate(fileRemainder);
            fc.read(restOfFile);
            restOfFile.flip();
            while(restOfFile.hasRemaining() && restOfFile.get()==0)
            {
                ;
            }
            logger.severe(loggingName + "Found Null Padding, starting at " + chunkHeader.getStartLocationInFile()+ ", size:" + restOfFile.position() + ChunkHeader.CHUNK_HEADER_SIZE);
            fc.position(chunkHeader.getStartLocationInFile() + restOfFile.position() + ChunkHeader.CHUNK_HEADER_SIZE - 1);
            return true;
        }
        //Unknown chunk type just skip
        else
        {
            if(chunkHeader.getSize() < 0)
            {
                //As long as we have found audio data and info we can just skip to the end
                if(isFoundAudio && isFoundFormat)
                {
                    logger.severe(loggingName + " Size of Chunk Header is negative, skipping to file end:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    fc.position(fc.size());
                }
                else
                {
                    String msg = loggingName + " Not a valid header, unable to read a sensible size:Header"
                            + chunkHeader.getID()+"Size:"+chunkHeader.getSize();
                    logger.severe(msg);
                    throw new CannotReadException(msg);
                }
            }
            else if(fc.position() + chunkHeader.getSize() <= fc.size())
            {
                logger.severe(loggingName + " Skipping chunk bytes:" + chunkHeader.getSize() + " for " + chunkHeader.getID());
                fc.position(fc.position() + chunkHeader.getSize());
            }
            else
            {
                //As long as we have found audio data and info we can just skip to the end
                if(isFoundAudio && isFoundFormat)
                {
                    logger.severe(loggingName + " Size of Chunk Header larger than data, skipping to file end:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    fc.position(fc.size());
                }
                else
                {
                    logger.severe(loggingName + " Size of Chunk Header larger than data, cannot read file");
                    throw new CannotReadException(loggingName + " Size of Chunk Header larger than data, cannot read file");
                }
            }
        }
        IffHeaderChunk.ensureOnEqualBoundary(fc, chunkHeader);
        return true;
    }



}
