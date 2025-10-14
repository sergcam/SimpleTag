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

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.iff.*;
import org.jaudiotagger.audio.wav.chunk.WavCorruptChunkType;
import org.jaudiotagger.audio.wav.chunk.WavId3Chunk;
import org.jaudiotagger.audio.wav.chunk.WavListChunk;
import org.jaudiotagger.logging.Hex;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.wav.WavInfoTag;
import org.jaudiotagger.tag.wav.WavTag;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * Read the Wav file chunks, until finds WavFormatChunk and then generates AudioHeader from it
 */
public class WavTagReader
{
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.wav");

    private String loggingName;
    public WavTagReader(String loggingName)
    {
        this.loggingName = loggingName;
    }


    /**
     * Read file and return tag metadata
     *
     * @param path
     * @return
     * @throws CannotReadException
     * @throws IOException
     */
    public WavTag read(Path path) throws CannotReadException, IOException
    {
        logger.config(loggingName + " Read Tag:start");
        WavTag tag = new WavTag(TagOptionSingleton.getInstance().getWavOptions());
        try(FileChannel fc = FileChannel.open(path))
        {
            if (WavRIFFHeader.isValidHeader(loggingName, fc))
            {
                while (fc.position() < fc.size())
                {
                    if (!readChunk(fc, tag))
                    {
                        break;
                    }
                }
            }
            else
            {
                throw new CannotReadException(loggingName+ " Wav RIFF Header not valid");
            }
        }

        //Ensure we have read audio data chunk okay
        List<ChunkSummary> chunks =  tag.getChunkSummaryList();
        boolean isMusicDataFound=false;
        for(ChunkSummary next:chunks)
        {
            if(next.getChunkId().equals(WavChunkType.DATA.getCode()))
            {
                isMusicDataFound=true;
            }
        }

        if(!isMusicDataFound)
        {
            throw new CannotReadException(loggingName+ " Unable to determine audio data");
        }

        createDefaultMetadataTagsIfMissing(tag);
        logger.config(loggingName + " Read Tag:end");
        return tag;
    }

    /**
     * So if the file doesn't contain (both) types of metadata we construct them so data can be
     * added and written back to file on save
     *
     * @param tag
     */
    private void createDefaultMetadataTagsIfMissing(WavTag tag)
    {
        if(!tag.isExistingId3Tag())
        {
            tag.setID3Tag(WavTag.createDefaultID3Tag());
        }
        if(!tag.isExistingInfoTag())
        {
            tag.setInfoTag(new WavInfoTag());
        }
    }

    /**
     * Reads Wavs Chunk that contain tag metadata
     *
     * If the same chunk exists more than once in the file we would just use the last occurence
     *
     * @param tag
     * @return
     * @throws IOException
     */
    protected boolean readChunk(FileChannel fc, WavTag tag)throws IOException, CannotReadException
    {
        Chunk chunk;
        ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        ChunkSummary cs;
        if (!chunkHeader.readHeader(fc))
        {
            return false;
        }

        String id = chunkHeader.getID();
        logger.info(loggingName + " Reading Chunk:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
        final WavChunkType chunkType = WavChunkType.get(id);
        if (chunkType != null)
        {
            switch (chunkType)
            {
                case LIST:
                    cs = new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize());
                    tag.addChunkSummary(cs);
                    tag.addMetadataChunkSummary(cs);

                    if(tag.getInfoTag()==null)
                    {
                        chunk = new WavListChunk(loggingName, Utils.readFileDataIntoBufferLE(fc, (int) chunkHeader.getSize()), chunkHeader, tag);
                        if (!chunk.readChunk())
                        {
                            logger.severe(loggingName + " LIST readChunkFailed");
                            return false;
                        }
                    }
                    else
                    {
                        fc.position(fc.position() + chunkHeader.getSize());
                        logger.warning(loggingName + " Ignoring LIST chunk because already have one:" + chunkHeader.getID()
                                + ":"  + Hex.asDecAndHex(chunkHeader.getStartLocationInFile() - 1)
                                + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    }
                    break;

                case ID3_UPPERCASE:
                    cs = new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize());
                    tag.addChunkSummary(cs);
                    tag.addMetadataChunkSummary(cs);
                    if(tag.getID3Tag()==null)
                    {
                        chunk = new WavId3Chunk(Utils.readFileDataIntoBufferLE(fc, (int) chunkHeader.getSize()), chunkHeader, tag, loggingName);
                        if (!chunk.readChunk())
                        {
                            logger.severe(loggingName + " ID3 readChunkFailed");
                            return false;
                        }

                        logger.severe(loggingName + " ID3 chunk should be id3:" + chunkHeader.getID() + ":"
                                + Hex.asDecAndHex(chunkHeader.getStartLocationInFile())
                                + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    }
                    else
                    {
                        fc.position(fc.position() + chunkHeader.getSize());
                        logger.warning(loggingName + " Ignoring id3 chunk because already have one:" + chunkHeader.getID() + ":"
                                + Hex.asDecAndHex(chunkHeader.getStartLocationInFile())
                                + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    }
                    break;

                case ID3:
                    cs = new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize());
                    tag.addChunkSummary(cs);
                    tag.addMetadataChunkSummary(cs);
                    if(tag.getID3Tag()==null)
                    {
                        chunk = new WavId3Chunk(Utils.readFileDataIntoBufferLE(fc, (int) chunkHeader.getSize()), chunkHeader, tag, loggingName);
                        if (!chunk.readChunk())
                        {
                            logger.severe(loggingName + " id3 readChunkFailed");
                            return false;
                        }
                    }
                    else
                    {
                        fc.position(fc.position() + chunkHeader.getSize());
                        logger.warning(loggingName + " Ignoring id3 chunk because already have one:" + chunkHeader.getID() + ":"
                                + Hex.asDecAndHex(chunkHeader.getStartLocationInFile())
                                + ":sizeIncHeader:"+ (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                    }
                    break;

                default:
                    tag.addChunkSummary(new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize()));
                    fc.position(fc.position() + chunkHeader.getSize());

            }
        }
        else if(id.substring(1,3).equals(WavCorruptChunkType.CORRUPT_ID3_EARLY.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt id3 chunk, starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());
            if(tag.getInfoTag()==null && tag.getID3Tag() == null)
            {
                tag.setIncorrectlyAlignedTag(true);
            }
            fc.position(fc.position() - (ChunkHeader.CHUNK_HEADER_SIZE - 1));
            return true;
        }
        else if(id.substring(0,3).equals(WavCorruptChunkType.CORRUPT_ID3_LATE.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt id3 chunk, starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());
            if(tag.getInfoTag()==null && tag.getID3Tag() == null)
            {
                tag.setIncorrectlyAlignedTag(true);
            }
            fc.position(fc.position() -  (ChunkHeader.CHUNK_HEADER_SIZE + 1));
            return true;
        }
        else if(id.substring(1,3).equals(WavCorruptChunkType.CORRUPT_LIST_EARLY.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt LIST Chunk, starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());

            if(tag.getInfoTag()==null && tag.getID3Tag() == null)
            {
                tag.setIncorrectlyAlignedTag(true);
            }
            fc.position(fc.position() -  (ChunkHeader.CHUNK_HEADER_SIZE - 1));
            return true;
        }
        else if(id.substring(0,3).equals(WavCorruptChunkType.CORRUPT_LIST_LATE.getCode()))
        {
            logger.severe(loggingName + " Found Corrupt LIST Chunk (2), starting at Odd Location:"+chunkHeader.getID()+":"+chunkHeader.getSize());
            if(tag.getInfoTag()==null && tag.getID3Tag() == null)
            {
                tag.setIncorrectlyAlignedTag(true);
            }
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
            while(restOfFile.get()==0)
            {
                ;
            }
            logger.severe(loggingName + "Found Null Padding, starting at " + chunkHeader.getStartLocationInFile()+ ", size:" + restOfFile.position() + ChunkHeader.CHUNK_HEADER_SIZE);
            fc.position(chunkHeader.getStartLocationInFile() + restOfFile.position() + ChunkHeader.CHUNK_HEADER_SIZE - 1);
            tag.addChunkSummary(new PaddingChunkSummary(chunkHeader.getStartLocationInFile(), restOfFile.position() - 1));
            tag.setNonStandardPadding(true);
            return true;
        }
        //Unknown chunk type just skip
        else
        {
            if(chunkHeader.getSize() < 0)
            {
                logger.severe(loggingName + " Size of Chunk Header is negative, skipping to file end:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                tag.addChunkSummary(new BadChunkSummary(chunkHeader.getStartLocationInFile(), fc.size() - fc.position()));
                tag.setBadChunkData(true);
                fc.position(fc.size());
            }
            else if(fc.position() + chunkHeader.getSize() <= fc.size())
            {
                logger.severe(loggingName + " Skipping chunk bytes:" + chunkHeader.getSize() + " for " + chunkHeader.getID());
                tag.addChunkSummary(new ChunkSummary(chunkHeader.getID(), chunkHeader.getStartLocationInFile(), chunkHeader.getSize()));
                fc.position(fc.position() + chunkHeader.getSize());
            }
            else
            {
                logger.severe(loggingName + " Size of Chunk Header larger than data, skipping to file end:" + id + ":starting at:" + Hex.asDecAndHex(chunkHeader.getStartLocationInFile()) + ":sizeIncHeader:" + (chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE));
                tag.addChunkSummary(new BadChunkSummary(chunkHeader.getStartLocationInFile(), fc.size() - fc.position()));
                tag.setBadChunkData(true);
                fc.position(fc.size());
            }
        }
        IffHeaderChunk.ensureOnEqualBoundary(fc, chunkHeader);
        return true;
    }
}
