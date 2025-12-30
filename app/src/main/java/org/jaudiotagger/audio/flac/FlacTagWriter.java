/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
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
package org.jaudiotagger.audio.flac;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.NoWritePermissionsException;
import org.jaudiotagger.audio.flac.metadatablock.*;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.utils.ShiftData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Write Flac Tag
 *
 * For best compatibility with other applications we write blocks (where they exist) in the following order:
 *
 *  STREAM
 *  VORBIS_COMMENT
 *  PICTURE
 *  SEEK
 *  CUESHEET
 *  APPLICATION
 *  PADDING
 */
public class FlacTagWriter
{
    // Logger Object
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.flac");
    private FlacTagCreator tc = new FlacTagCreator();

    /**
     *
     * Remove VORBIS_COMMENT or PICTURE blocks from file
     *
     * @param tag
     * @param file
     * @throws IOException
     * @throws CannotWriteException
     */
    public void delete(Tag tag, Path file) throws CannotWriteException
    {
        //This will save the file without any Comment or PictureData blocks  
        FlacTag emptyTag = new FlacTag(null, new ArrayList<MetadataBlockDataPicture>());
        write(emptyTag, file);
    }

    /**
     * Makes writing tag a bit simpler
     */
    private static class MetadataBlockInfo
    {
        private List<MetadataBlock> blocks = new ArrayList<>();

        private MetadataBlock streamInfoBlock;
        private List<MetadataBlock> metadataBlockPadding        = new ArrayList<MetadataBlock>(1);
        private List<MetadataBlock> metadataBlockApplication    = new ArrayList<MetadataBlock>(1);
        private List<MetadataBlock> metadataBlockSeekTable      = new ArrayList<MetadataBlock>(1);
        private List<MetadataBlock> metadataBlockCueSheet       = new ArrayList<MetadataBlock>(1);

        public  List<MetadataBlock> getListOfNonMetadataBlocks()
        {
            for(MetadataBlock next:metadataBlockSeekTable)
            {
                blocks.add(next);
            }

            for(MetadataBlock next:metadataBlockCueSheet)
            {
                blocks.add(next);
            }

            for(MetadataBlock next:metadataBlockApplication)
            {
                blocks.add(next);
            }
            return blocks;
        }

        /**
         * Count of non-metadata blocks
         *
         * Doesnt include STREAM or PADDING
         *
         * @param blockInfo
         * @return
         */
        private int getOtherBlockCount(MetadataBlockInfo blockInfo)
        {
            int count = blockInfo.metadataBlockApplication.size();
            count+=blockInfo.metadataBlockSeekTable.size();
            count+=blockInfo.metadataBlockCueSheet.size();
            return count;
        }

        /**
         * @return space currently available for writing all Flac metadatablocks except for STREAM which is fixed size
         */
        public int computeAvailableRoom()
        {
            int length = 0;

            for (MetadataBlock aMetadataBlockApplication : metadataBlockApplication)
            {
                length += aMetadataBlockApplication.getLength();
            }

            for (MetadataBlock aMetadataBlockSeekTable : metadataBlockSeekTable)
            {
                length += aMetadataBlockSeekTable.getLength();
            }

            for (MetadataBlock aMetadataBlockCueSheet : metadataBlockCueSheet)
            {
                length += aMetadataBlockCueSheet.getLength();
            }

            //Note when reading metadata has been put into padding as well for purposes of write
            for (MetadataBlock aMetadataBlockPadding : metadataBlockPadding)
            {
                length += aMetadataBlockPadding.getLength();
            }

            return length;
        }

        /**
         * @return space required to write the metadata blocks that are part of Flac but are not part of tagdata
         * in the normal sense.
         */
        public int computeNeededRoom()
        {
            int length = 0;

            for (MetadataBlock aMetadataBlockApplication : metadataBlockApplication)
            {
                length += aMetadataBlockApplication.getLength();
            }


            for (MetadataBlock aMetadataBlockSeekTable : metadataBlockSeekTable)
            {
                length += aMetadataBlockSeekTable.getLength();
            }

            for (MetadataBlock aMetadataBlockCueSheet : metadataBlockCueSheet)
            {
                length += aMetadataBlockCueSheet.getLength();
            }

            return length;
        }
    }

    /**
     * @param tag
     * @param file
     * @throws CannotWriteException
     * @throws IOException
     */
    public void write(Tag tag, Path file) throws CannotWriteException
    {
        logger.config(file + " Writing tag");
        try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.READ))
        {
            MetadataBlockInfo blockInfo = new MetadataBlockInfo();

            //Read existing data
            FlacStreamReader flacStream = new FlacStreamReader(fc, file.toString() + " ");
            try
            {
                flacStream.findStream();
            }
            catch (CannotReadException cre)
            {
                throw new CannotWriteException(cre.getMessage());
            }

            boolean isLastBlock = false;
            while (!isLastBlock)
            {
                try
                {
                    MetadataBlockHeader mbh = MetadataBlockHeader.readHeader(fc);
                    if (mbh.getBlockType() != null)
                    {
                        switch (mbh.getBlockType())
                        {
                            case STREAMINFO:
                            {
                                blockInfo.streamInfoBlock = new MetadataBlock(mbh, new MetadataBlockDataStreamInfo(mbh, fc));
                                break;
                            }

                            case VORBIS_COMMENT:
                            case PADDING:
                            case PICTURE:
                            {
                                //All these will be replaced by the new metadata so we just treat as padding in order
                                //to determine how much space is already allocated in the file
                                fc.position(fc.position() + mbh.getDataLength());
                                MetadataBlockData mbd = new MetadataBlockDataPadding(mbh.getDataLength());
                                blockInfo.metadataBlockPadding.add(new MetadataBlock(mbh, mbd));
                                break;
                            }

                            case APPLICATION:
                            {
                                MetadataBlockData mbd = new MetadataBlockDataApplication(mbh, fc);
                                blockInfo.metadataBlockApplication.add(new MetadataBlock(mbh, mbd));
                                break;
                            }

                            case SEEKTABLE:
                            {
                                MetadataBlockData mbd = new MetadataBlockDataSeekTable(mbh, fc);
                                blockInfo.metadataBlockSeekTable.add(new MetadataBlock(mbh, mbd));
                                break;
                            }

                            case CUESHEET:
                            {
                                MetadataBlockData mbd = new MetadataBlockDataCueSheet(mbh, fc);
                                blockInfo.metadataBlockCueSheet.add(new MetadataBlock(mbh, mbd));
                                break;
                            }

                            default:
                            {
                                //TODO What are the consequences of doing this ?
                                fc.position(fc.position() + mbh.getDataLength());
                                break;
                            }
                        }
                    }
                    isLastBlock = mbh.isLastBlock();
                }
                catch (CannotReadException cre)
                {
                    throw new CannotWriteException(cre.getMessage());
                }
            }

            //Number of bytes in the existing file available before audio data
            int availableRoom = blockInfo.computeAvailableRoom();

            //Minimum Size of the New tag data without padding
            int newTagSize = tc.convertMetadata(tag).limit();

            //Other blocks required size
            int otherBlocksRequiredSize = blockInfo.computeNeededRoom();

            //Number of bytes required for new tagdata and other metadata blocks
            int neededRoom = newTagSize + otherBlocksRequiredSize;

            //Go to start of Flac within file
            fc.position(flacStream.getStartOfFlacInFile());

            //There is enough room to fit the tag without moving the audio just need to
            //adjust padding accordingly need to allow space for padding header if padding required
            logger.config(file + ":Writing tag available bytes:" + availableRoom + ":needed bytes:" + neededRoom);
            if ((availableRoom == neededRoom) || (availableRoom > neededRoom + MetadataBlockHeader.HEADER_LENGTH))
            {
                logger.config(file + ":Room to Rewrite");
                writeAllNonAudioData(tag, fc, blockInfo, flacStream, availableRoom - neededRoom);
            }
            //Need to move audio
            else
            {
                logger.config(file + ":Audio must be shifted "+ "NewTagSize:" + newTagSize + ":AvailableRoom:" + availableRoom + ":MinimumAdditionalRoomRequired:"+(neededRoom - availableRoom));
                //As we are having to move both anyway may as well put in the default padding
                insertUsingChunks(file, tag, fc, blockInfo, flacStream, neededRoom + FlacTagCreator.DEFAULT_PADDING, availableRoom);
            }
        }
        catch (AccessDeniedException ade)
        {
            logger.log(Level.SEVERE, ade.getMessage(), ade);
            throw new NoWritePermissionsException(file + ":" + ade.getMessage());
        }
        catch (IOException ioe)
        {
            logger.log(Level.SEVERE, ioe.getMessage(), ioe);
            throw new CannotWriteException(file + ":" + ioe.getMessage());
        }
    }

    /**Add Padding Block
     *
     * @param paddingSize
     * @return
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer addPaddingBlock(int paddingSize) throws UnsupportedEncodingException
    {
        //Padding
        logger.config("padding:" + paddingSize);
        ByteBuffer buf = ByteBuffer.allocate(paddingSize);
        if (paddingSize > 0)
        {
            int paddingDataSize = paddingSize - MetadataBlockHeader.HEADER_LENGTH;
            MetadataBlockHeader paddingHeader = new MetadataBlockHeader(true, BlockType.PADDING, paddingDataSize);
            MetadataBlockDataPadding padding = new MetadataBlockDataPadding(paddingDataSize);
            buf.put(paddingHeader.getBytes());
            buf.put(padding.getBytes());
            buf.rewind();
        }
        return buf;
    }

    /**
     * Write all blocks except audio
     *
     * @param tag
     * @param fc
     * @param blockInfo
     * @param flacStream
     * @param padding
     *
     * @throws IOException
     */
    private void writeAllNonAudioData(Tag tag, FileChannel fc, MetadataBlockInfo blockInfo, FlacStreamReader flacStream, int padding) throws IOException
    {
        //Jump over Id3 (if exists) and flac header
        fc.position(flacStream.getStartOfFlacInFile() + FlacStreamReader.FLAC_STREAM_IDENTIFIER_LENGTH);

        //Write Stream Block
        writeStreamBlock(fc, blockInfo);

        //Write tag (vorbiscomment, picture)
        fc.write(tc.convertMetadata(tag, padding>0 || blockInfo.getOtherBlockCount(blockInfo)>0));

        //Write other non metadata blocks
        List<MetadataBlock> blocks = blockInfo.getListOfNonMetadataBlocks();
        if(blocks.size() > 1)
        {
            for (int i = 0; i < blocks.size() - 1; i++)
            {
                fc.write(ByteBuffer.wrap(blocks.get(i).getHeader().getBytesWithoutIsLastBlockFlag()));
                fc.write(blocks.get(i).getData().getBytes());
            }
        }

        //Write last non-metadata block
        if(blocks.size()>0)
        {
            if (padding > 0)
            {
                fc.write(ByteBuffer.wrap(blocks.get(blocks.size() - 1).getHeader().getBytesWithoutIsLastBlockFlag()));
            }
            else
            {
                fc.write(ByteBuffer.wrap(blocks.get(blocks.size() - 1).getHeader().getBytesWithLastBlockFlag()));
            }
            fc.write(blocks.get(blocks.size() - 1).getData().getBytes());
        }

        //Write padding
        if(padding > 0)
        {
            fc.write(addPaddingBlock(padding));
        }
    }

    /**
     * Insert metadata into space that is not large enough
     *
     * We do this by reading/writing chunks of data allowing it to work on low memory systems
     *
     * Chunk size defined by TagOptionSingleton.getInstance().getWriteChunkSize()
     *
     * @param tag
     * @param fc
     * @param blockInfo
     * @param flacStream
     * @param neededRoom
     * @param availableRoom
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void insertUsingChunks(Path file, Tag tag, FileChannel fc, MetadataBlockInfo blockInfo, FlacStreamReader flacStream, int neededRoom, int availableRoom) throws IOException, UnsupportedEncodingException
    {
        //Find end of metadata blocks (start of Audio), i.e start of Flac + 4 bytes for 'fLaC', 4 bytes for streaminfo header and
        //34 bytes for streaminfo and then size of all the other existing blocks
        long audioStart =flacStream.getStartOfFlacInFile()
                + FlacStreamReader.FLAC_STREAM_IDENTIFIER_LENGTH
                + MetadataBlockHeader.HEADER_LENGTH
                + MetadataBlockDataStreamInfo.STREAM_INFO_DATA_LENGTH
                + availableRoom;

        //Extra Space Required for larger metadata block
        int extraSpaceRequired = neededRoom - availableRoom;
        logger.config(file + " Audio needs shifting:"+extraSpaceRequired);

        fc.position(audioStart);
        ShiftData.shiftDataByOffsetToMakeSpace(fc, extraSpaceRequired);

        //Jump over Id3 (if exists) and Flac Header
        fc.position(flacStream.getStartOfFlacInFile() + FlacStreamReader.FLAC_STREAM_IDENTIFIER_LENGTH);
        writeAllNonAudioData(tag, fc, blockInfo, flacStream, FlacTagCreator.DEFAULT_PADDING);
    }

    private void writeStreamBlock(FileChannel fc, MetadataBlockInfo blockInfo) throws IOException
    {
        //Write StreamInfo, we always write this first even if wasn't first in original spec
        fc.write(ByteBuffer.wrap(blockInfo.streamInfoBlock.getHeader().getBytesWithoutIsLastBlockFlag()));
        fc.write(blockInfo.streamInfoBlock.getData().getBytes());
    }
}