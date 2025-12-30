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
package org.jaudiotagger.audio.wav;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.NoWritePermissionsException;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.iff.ChunkHeader;
import org.jaudiotagger.audio.iff.ChunkSummary;
import org.jaudiotagger.audio.iff.IffHeaderChunk;
import org.jaudiotagger.audio.iff.PaddingChunkSummary;
import org.jaudiotagger.audio.wav.chunk.WavChunkSummary;
import org.jaudiotagger.audio.wav.chunk.WavInfoIdentifier;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.wav.WavInfoTag;
import org.jaudiotagger.tag.wav.WavTag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

import static org.jaudiotagger.audio.iff.IffHeaderChunk.SIGNATURE_LENGTH;
import static org.jaudiotagger.audio.iff.IffHeaderChunk.SIZE_LENGTH;

/**
 * Write Wav Tag.
 */
public class WavTagWriter
{
    //For logging
    private String loggingName;
    public WavTagWriter(String loggingName)
    {
        this.loggingName = loggingName;
    }

    // Logger Object
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.wav");

    /**
     * Read existing metadata
     *
     * @param path
     * @return tags within Tag wrapper
     * @throws IOException
     * @throws CannotWriteException
     */
    public WavTag getExistingMetadata(Path path) throws IOException, CannotWriteException
    {
        try
        {
            //Find WavTag (if any)
            WavTagReader im = new WavTagReader(loggingName);
            return im.read(path);
        }
        catch (CannotReadException ex)
        {
            throw new CannotWriteException("Failed to read file "+path);
        }
    }

    /**
     * Seek in file to start of LIST Metadata chunk
     *
     * @param fc
     * @param existingTag
     * @throws IOException
     * @throws CannotWriteException
     */
    public ChunkHeader seekToStartOfListInfoMetadata(FileChannel fc, WavTag existingTag) throws IOException, CannotWriteException
    {
        fc.position(existingTag.getInfoTag().getStartLocationInFile());
        final ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        chunkHeader.readHeader(fc);
        fc.position(fc.position() - ChunkHeader.CHUNK_HEADER_SIZE);

        if (!WavChunkType.LIST.getCode().equals(chunkHeader.getID()))
        {
            throw new CannotWriteException(loggingName +" Unable to find List chunk at original location has file been modified externally");
        }
        return chunkHeader;
    }

    public ChunkHeader seekToStartOfListInfoMetadataForChunkSummaryHeader(FileChannel fc, ChunkSummary cs) throws IOException, CannotWriteException
    {
        fc.position(cs.getFileStartLocation());
        final ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        chunkHeader.readHeader(fc);
        fc.position(fc.position() - ChunkHeader.CHUNK_HEADER_SIZE);

        if (!WavChunkType.LIST.getCode().equals(chunkHeader.getID()))
        {
            throw new CannotWriteException(loggingName +" Unable to find List chunk at original location has file been modified externally");
        }
        return chunkHeader;
    }

    /**
     * Seek in file to start of Id3 Metadata chunk
     *
     * @param fc
     * @param existingTag
     * @throws IOException
     * @throws CannotWriteException
     */
    public ChunkHeader seekToStartOfId3MetadataForChunkSummaryHeader(FileChannel fc, WavTag existingTag) throws IOException, CannotWriteException
    {
        logger.info(loggingName+":seekToStartOfIdMetadata:"+existingTag.getStartLocationInFileOfId3Chunk());
        fc.position(existingTag.getStartLocationInFileOfId3Chunk());
        final ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        chunkHeader.readHeader(fc);
        fc.position(fc.position() - ChunkHeader.CHUNK_HEADER_SIZE);
        if (
                (!WavChunkType.ID3.getCode().equals(chunkHeader.getID())) &&
                (!WavChunkType.ID3_UPPERCASE.getCode().equals(chunkHeader.getID()))
            )
        {
            throw new CannotWriteException(loggingName + " Unable to find ID3 chunk at original location has file been modified externally:"+chunkHeader.getID());
        }

        if(WavChunkType.ID3_UPPERCASE.getCode().equals(chunkHeader.getID()))
        {
            logger.severe(loggingName+":on save ID3 chunk will be correctly set with id3 id");
        }
        return chunkHeader;
    }

    public ChunkHeader seekToStartOfId3MetadataForChunkSummaryHeader(FileChannel fc, ChunkSummary chunkSummary) throws IOException, CannotWriteException
    {
        logger.severe(loggingName+":seekToStartOfIdMetadata:"+chunkSummary.getFileStartLocation());
        fc.position(chunkSummary.getFileStartLocation());
        final ChunkHeader chunkHeader = new ChunkHeader(ByteOrder.LITTLE_ENDIAN);
        chunkHeader.readHeader(fc);
        fc.position(fc.position() - ChunkHeader.CHUNK_HEADER_SIZE);
        if (
                (!WavChunkType.ID3.getCode().equals(chunkHeader.getID())) &&
                        (!WavChunkType.ID3_UPPERCASE.getCode().equals(chunkHeader.getID()))
                )
        {
            throw new CannotWriteException(loggingName + " Unable to find ID3 chunk at original location has file been modified externally:"+chunkHeader.getID());
        }

        if(WavChunkType.ID3_UPPERCASE.getCode().equals(chunkHeader.getID()))
        {
            logger.severe(loggingName+":on save ID3 chunk will be correctly set with id3 id");
        }
        return chunkHeader;
    }
    /**
     * Delete any existing metadata tags from files
     *
     * @param tag
     * @param file
     * @throws IOException
     * @throws CannotWriteException
     */
    public void delete (Tag tag, Path file) throws CannotWriteException
    {
        logger.info(loggingName + ":Deleting metadata from file");
        try(FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.READ))
        {
            WavTag existingTag = getExistingMetadata(file);

            //have both tags
            if (existingTag.isExistingId3Tag() && existingTag.isExistingInfoTag())
            {
                BothTagsFileStructure fs = checkExistingLocations(existingTag, fc);
                //We can delete both chunks in one go
                if (fs.isContiguous)
                {
                    //Quick method
                    if (fs.isAtEnd)
                    {
                        if (fs.isInfoTagFirst)
                        {
                            fc.truncate(existingTag.getInfoTag().getStartLocationInFile());
                        }
                        else
                        {
                            fc.truncate(existingTag.getStartLocationInFileOfId3Chunk());
                        }
                    }
                    //Slower
                    else
                    {
                        if (fs.isInfoTagFirst)
                        {
                            final int lengthTagChunk = (int) (existingTag.getEndLocationInFileOfId3Chunk() - existingTag.getInfoTag().getStartLocationInFile());
                            deleteTagChunk(fc, (int) existingTag.getEndLocationInFileOfId3Chunk(), lengthTagChunk);
                        }
                        else
                        {
                            final int lengthTagChunk = (int) (existingTag.getInfoTag().getEndLocationInFile().intValue() - existingTag.getStartLocationInFileOfId3Chunk());
                            deleteTagChunk(fc, (int) existingTag.getInfoTag().getEndLocationInFile().intValue(), lengthTagChunk);
                        }
                    }
                }
                //Tricky to delete both because once one is deleted affects the location of the other
                else
                {
                    WavInfoTag existingInfoTag = existingTag.getInfoTag();
                    ChunkHeader infoChunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
                    ChunkHeader id3ChunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);

                    //If one of these two at end of file delete first then remove the other as a chunk
                    if (isInfoTagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
                    {
                        fc.truncate(existingInfoTag.getStartLocationInFile());
                        deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
                    }
                    else if (isID3TagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
                    {
                        fc.truncate(existingTag.getStartLocationInFileOfId3Chunk());
                        deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
                    }
                    //Id3 tag comes first so we must remove Info tag first
                    else if(existingTag.getInfoTag().getStartLocationInFile() > existingTag.getStartLocationInFileOfId3Chunk())
                    {
                        deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
                        deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
                    }
                    //Info tag comes first so we must remove Id3 tag first
                    else
                    {
                        deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
                        deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
                    }
                }
            }
            //Delete Info if exists
            else if (existingTag.isExistingInfoTag())
            {
                WavInfoTag existingInfoTag = existingTag.getInfoTag();
                ChunkHeader chunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
                //and it is at end of the file
                if (existingInfoTag.getEndLocationInFile() == fc.size())
                {
                    fc.truncate(existingInfoTag.getStartLocationInFile());
                }
                else
                {
                    deleteInfoTagChunk(fc, existingTag, chunkHeader);
                }
            }
            else if (existingTag.isExistingId3Tag())
            {
                ChunkHeader chunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);
                //and it is at end of the file
                if (isID3TagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
                {
                    fc.truncate(existingTag.getStartLocationInFileOfId3Chunk());
                }
                else
                {
                    deleteId3TagChunk(fc, existingTag, chunkHeader);
                }
            }
            else
            {
                //Nothing to delete
            }

            rewriteRiffHeaderSize(fc);
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(file + ":" + ioe.getMessage());
        }
    }

    /**
     * Delete existing Info Tag
     *
       * @param fc
     * @param existingTag
     * @param chunkHeader
     * @throws IOException
     */
    private void deleteInfoTagChunk(final FileChannel fc, final WavTag existingTag, final ChunkHeader chunkHeader) throws IOException
    {
        final WavInfoTag existingInfoTag = existingTag.getInfoTag();
        final int lengthTagChunk = (int) chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE;
        deleteTagChunk(fc, existingInfoTag.getEndLocationInFile().intValue(), lengthTagChunk);
    }

    /**
     * Delete existing Id3 Tag
     *
     * @param fc
     * @param existingTag
     * @param chunkHeader
     * @throws IOException
     */
    private void deleteId3TagChunk(FileChannel fc, final WavTag existingTag, final ChunkHeader chunkHeader) throws IOException
    {
        final int lengthTagChunk = (int) chunkHeader.getSize() + ChunkHeader.CHUNK_HEADER_SIZE;
        if (Utils.isOddLength(existingTag.getEndLocationInFileOfId3Chunk()))
        {
            deleteTagChunk(fc, (int) existingTag.getEndLocationInFileOfId3Chunk() + 1, lengthTagChunk + 1);
        }
        else
        {
            deleteTagChunk(fc, (int) existingTag.getEndLocationInFileOfId3Chunk(), lengthTagChunk);
        }

    }

    /**
     * Delete Tag Chunk
     * <p/>
     * Can be used when chunk is not the last chunk
     * <p/>
     * Continually copy a 4mb chunk, write the chunk and repeat until the rest of the file after the tag
     * is rewritten
     *
     * @param fc
     * @param endOfExistingChunk
     * @param lengthTagChunk
     * @throws IOException
     */
    private void deleteTagChunk(final FileChannel fc, int endOfExistingChunk, final int lengthTagChunk) throws IOException
    {
        //Position for reading after the tag
        fc.position(endOfExistingChunk);

        final ByteBuffer buffer = ByteBuffer.allocate((int) TagOptionSingleton.getInstance().getWriteChunkSize());
        while (fc.read(buffer) >= 0 || buffer.position() != 0)
        {
            buffer.flip();
            final long readPosition = fc.position();
            fc.position(readPosition - lengthTagChunk - buffer.limit());
            fc.write(buffer);
            fc.position(readPosition);
            buffer.compact();
        }
        //Truncate the file after the last chunk
        final long newLength = fc.size() - lengthTagChunk;
        logger.severe(loggingName + "Shortening by:"+ lengthTagChunk + " Setting new length to:" + newLength);
        fc.truncate(newLength);
    }

    /**
     *
     * @param tag
     * @param file
     * @throws CannotWriteException
     */
    public void write(final Tag tag, Path file) throws CannotWriteException
    {
        logger.config(loggingName + " Writing tag to file:start");

        WavSaveOptions wso = TagOptionSingleton.getInstance().getWavSaveOptions();
        WavTag existingTag = null;
        try
        {
            existingTag = getExistingMetadata(file);
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(file + ":" + ioe.getMessage());
        }

        //TODO in some case we can fix the files, as we can only open the file if we have successfully
        //retrieved audio data
        if(existingTag.isBadChunkData())
        {
            throw new CannotWriteException("Unable to make changes to this file because contains bad chunk data");
        }

        try(FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.READ))
        {
            final WavTag wavTag = (WavTag) tag;
            if (wso == WavSaveOptions.SAVE_BOTH)
            {
                saveBoth(wavTag, fc, existingTag);
            }
            else if (wso == WavSaveOptions.SAVE_ACTIVE)
            {
                saveActive(wavTag, fc, existingTag);
            }
            else if (wso == WavSaveOptions.SAVE_EXISTING_AND_ACTIVE)
            {
                saveActiveExisting(wavTag, fc, existingTag);
            }
            else if (wso == WavSaveOptions.SAVE_BOTH_AND_SYNC)
            {
                wavTag.syncTagBeforeWrite();
                saveBoth(wavTag, fc, existingTag);
            }
            else if (wso == WavSaveOptions.SAVE_EXISTING_AND_ACTIVE_AND_SYNC)
            {
                wavTag.syncTagBeforeWrite();
                saveActiveExisting(wavTag, fc, existingTag);
            }
            //Invalid Option, should never happen
            else
            {
                throw new RuntimeException(loggingName + " No setting for:WavSaveOptions");
            }

            //If we had non-standard padding check it still exists and if so remove it
            if(existingTag.isNonStandardPadding())
            {
                for(ChunkSummary cs: existingTag.getChunkSummaryList())
                {
                    //Note, can only delete a single padding section
                    if(cs instanceof PaddingChunkSummary)
                    {
                        boolean isPaddingData = true;
                        fc.position(cs.getFileStartLocation());
                        ByteBuffer paddingData = ByteBuffer.allocate((int)cs.getChunkSize());
                        fc.read(paddingData);
                        paddingData.flip();
                        while(paddingData.position() < paddingData.limit())
                        {
                            if(paddingData.get()!=0)
                            {
                                isPaddingData =false;
                            }
                        }

                        if(isPaddingData)
                        {
                            fc.position(cs.getFileStartLocation());
                            deletePaddingChunk(fc, (int)cs.getEndLocation(), (int)cs.getChunkSize() + ChunkHeader.CHUNK_HEADER_SIZE);
                        }
                        break;
                    }
                }
            }

            rewriteRiffHeaderSize(fc);
        }
        catch(AccessDeniedException ade)
        {
            throw new NoWritePermissionsException(file + ":" + ade.getMessage());
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(file + ":" + ioe.getMessage());
        }


        logger.severe(loggingName + " Writing tag to file:Done");
    }

    private void deletePaddingChunk(final FileChannel fc, int endOfExistingChunk, final int lengthTagChunk) throws IOException
    {
        //Position for reading after the tag
        fc.position(endOfExistingChunk);

        final ByteBuffer buffer = ByteBuffer.allocate((int) TagOptionSingleton.getInstance().getWriteChunkSize());
        while (fc.read(buffer) >= 0 || buffer.position() != 0)
        {
            buffer.flip();
            final long readPosition = fc.position();
            fc.position(readPosition - lengthTagChunk - buffer.limit());
            fc.write(buffer);
            fc.position(readPosition);
            buffer.compact();
        }
        //Truncate the file after the last chunk
        final long newLength = fc.size() - lengthTagChunk;
        logger.config(loggingName + "-------------Setting new length to:" + newLength);
        fc.truncate(newLength);
    }

    /**
     * Rewrite RAF header to reflect new file size
     *
     * @param fc
     * @throws IOException
     */
    private void rewriteRiffHeaderSize(FileChannel fc) throws IOException
    {
        fc.position(IffHeaderChunk.SIGNATURE_LENGTH);
        ByteBuffer bb = ByteBuffer.allocateDirect(IffHeaderChunk.SIZE_LENGTH);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int size = ((int) fc.size()) - SIGNATURE_LENGTH - SIZE_LENGTH;
        bb.putInt(size);
        bb.flip();
        fc.write(bb);
    }

    /**
     * Write LISTINFOChunk of specified size to current file location
     * ensuring it is on even file boundary
     *
     * @param fc       random access file
     * @param bb        data to write
     * @param chunkSize chunk size
     * @throws java.io.IOException
     */
    private void writeInfoDataToFile(FileChannel fc, final ByteBuffer bb, final long chunkSize) throws IOException
    {
        if (Utils.isOddLength(fc.position()))
        {
            writePaddingToFile(fc, 1);
        }
        //Write LIST header
        final ByteBuffer listHeaderBuffer = ByteBuffer.allocate(ChunkHeader.CHUNK_HEADER_SIZE);
        listHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
        listHeaderBuffer.put(WavChunkType.LIST.getCode().getBytes(StandardCharsets.US_ASCII));
        listHeaderBuffer.putInt((int) chunkSize);
        listHeaderBuffer.flip();
        fc.write(listHeaderBuffer);

        //Now write actual data
        fc.write(bb);
        writeExtraByteIfChunkOddSize(fc, chunkSize);
    }

    /**
     * Write new Info chunk and dont worry about the size of existing chunk just use size of new chunk
     *
     * @param fc
     * @param bb
     * @throws IOException
     */
    private void writeInfoDataToFile(final FileChannel fc, final ByteBuffer bb) throws IOException
    {
        writeInfoDataToFile(fc, bb, bb.limit());
    }

    /**
     * Write Id3Chunk of specified size to current file location
     * ensuring it is on even file boundary
     *
     * @param fc       random access file
     * @param bb        data to write
     * @throws java.io.IOException
     */
    private void writeId3DataToFile(final FileChannel fc, final ByteBuffer bb) throws IOException
    {
        if(Utils.isOddLength(fc.position()))
        {
            writePaddingToFile(fc, 1);
        }

        //Write ID3Data header
        final ByteBuffer listBuffer = ByteBuffer.allocate(ChunkHeader.CHUNK_HEADER_SIZE);
        listBuffer.order(ByteOrder.LITTLE_ENDIAN);
        listBuffer.put(WavChunkType.ID3.getCode().getBytes(StandardCharsets.US_ASCII));
        listBuffer.putInt(bb.limit());
        listBuffer.flip();
        fc.write(listBuffer);

        //Now write actual data
        fc.write(bb);
    }

    /**
     * Write Padding bytes
     *
     * @param fc
     * @param paddingSize
     * @throws IOException
     */
    private void writePaddingToFile(final FileChannel  fc, final int paddingSize) throws IOException
    {
        fc.write(ByteBuffer.allocateDirect(paddingSize));
    }

    class InfoFieldWriterOrderComparator implements Comparator<TagField>
    {
        public int compare(TagField field1,TagField field2)
        {
            WavInfoIdentifier code1 = WavInfoIdentifier.getByFieldKey(FieldKey.valueOf(field1.getId()));
            WavInfoIdentifier code2 = WavInfoIdentifier.getByFieldKey(FieldKey.valueOf(field2.getId()));
            int order1 = Integer.MAX_VALUE;
            int order2 = Integer.MAX_VALUE;
            if(code1!=null)
            {
                order1 = code1.getPreferredWriteOrder();
            }
            if(code2!=null)
            {
                order2 = code2.getPreferredWriteOrder();
            }
            return order1 - order2;
        }
    }

    private void writeField( TagTextField tagTextField, String code, ByteArrayOutputStream baos)
    {
        try
        {
            baos.write(code.getBytes(StandardCharsets.US_ASCII));
            logger.config(loggingName + " Writing:" + code + ":" + tagTextField.getContent());

            byte[] contentConvertedToBytes = tagTextField.getContent().getBytes(StandardCharsets.ISO_8859_1);
            baos.write(Utils.getSizeLEInt32(contentConvertedToBytes.length));
            baos.write(contentConvertedToBytes);

            //Write extra byte if data length not equal
            if (Utils.isOddLength(contentConvertedToBytes.length))
            {
                baos.write(0);
            }
        }
        catch (IOException ioe)
        {
            //Should never happen as not writing to file at this point
            throw new RuntimeException(ioe);
        }
    }
    /**
     * Converts INfO tag to {@link java.nio.ByteBuffer}.
     *
     * @param tag tag
     * @return byte buffer containing the tag data
     * @throws java.io.UnsupportedEncodingException
     */
    public ByteBuffer convertInfoChunk(final WavTag tag)
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WavInfoTag wif = tag.getInfoTag();

        //Write the Info chunks
        List<TagField> fields = wif.getAll();
        Collections.sort(fields, new InfoFieldWriterOrderComparator());

        boolean isTrackRewritten = false;

        for(TagField nextField:fields)
        {
            //Find mapping to LIST field and write it
            TagTextField next = (TagTextField) nextField;
            WavInfoIdentifier wii = WavInfoIdentifier.getByFieldKey(FieldKey.valueOf(next.getId()));
            writeField( next, wii.getCode(), baos);

            //Add a duplicated record for Twonky if option enabled
            if(wii==WavInfoIdentifier.TRACKNO)
            {
                if(TagOptionSingleton.getInstance().isWriteWavForTwonky())
                {
                    isTrackRewritten =true;
                    writeField( next, WavInfoIdentifier.TWONKY_TRACKNO.getCode(), baos);
                }
            }
        }

        //Write any existing unrecognized tuples
        Iterator<TagTextField> ti = wif.getUnrecognisedFields().iterator();
        while(ti.hasNext())
        {
            TagTextField next = ti.next();

            if(next.getId().equals(WavInfoIdentifier.TWONKY_TRACKNO.getCode()))
            {
                //Write only if has option set and not already written
                if(!isTrackRewritten && TagOptionSingleton.getInstance().isWriteWavForTwonky())
                {
                    isTrackRewritten =true;
                    writeField( next, WavInfoIdentifier.TWONKY_TRACKNO.getCode(), baos);
                }
            }
            else
            {
                //Write back unrecognised field
                writeField( next, next.getId(), baos);
            }
        }

        final ByteBuffer infoBuffer = ByteBuffer.wrap(baos.toByteArray());
        infoBuffer.rewind();

        //Now Write INFO header
        final ByteBuffer infoHeaderBuffer = ByteBuffer.allocate(SIGNATURE_LENGTH);
        infoHeaderBuffer.put(WavChunkType.INFO.getCode().getBytes(StandardCharsets.US_ASCII));
        infoHeaderBuffer.flip();

        //Construct a single ByteBuffer from both
        ByteBuffer listInfoBuffer = ByteBuffer.allocateDirect(infoHeaderBuffer.limit() + infoBuffer.limit());
        listInfoBuffer.put(infoHeaderBuffer);
        listInfoBuffer.put(infoBuffer);
        listInfoBuffer.flip();
        return listInfoBuffer;
    }

    /**
     * Converts ID3tag to {@link ByteBuffer}.
     *
     * @param tag tag containing ID3tag
     * @return byte buffer containing the tag data
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer convertID3Chunk(final WavTag tag, WavTag existingTag)
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            long existingTagSize = existingTag.getSizeOfID3TagOnly();

            //If existingTag is uneven size lets make it even
            if( existingTagSize > 0)
            {
                if((existingTagSize & 1)!=0)
                {
                    existingTagSize++;
                }
            }

            //#270
            if (tag.getID3Tag() == null)
            {
                tag.setID3Tag(WavTag.createDefaultID3Tag());
            }

            //Write Tag to buffer
            tag.getID3Tag().write(baos, (int)existingTagSize);

            //If the tag is now odd because we needed to increase size and the data made it odd sized
            //we redo adding a padding byte to make it even
            if((baos.toByteArray().length & 1)!=0)
            {
                int newSize = baos.toByteArray().length + 1;
                baos = new ByteArrayOutputStream();
                tag.getID3Tag().write(baos, newSize);
            }
            final ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
            buf.rewind();
            return buf;
        }
        catch (IOException ioe)
        {
            //Should never happen as not writing to file at this point
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Used when writing both tags to work out the best way to do it
     */
    class BothTagsFileStructure
    {
        boolean isInfoTagFirst = false;
        boolean isContiguous   = false;
        boolean isAtEnd        = false;

        public String toString()
        {
            return "IsInfoTagFirst:"+isInfoTagFirst
                    +":isContiguous:"+isContiguous
                    +":isAtEnd:"+isAtEnd;
        }
    }

    /**
     * Identify where both metadata chunks are in relation to each other and other chunks
     * @param wavTag
     * @param fc
     * @return
     * @throws IOException
     */
    private BothTagsFileStructure checkExistingLocations(WavTag wavTag, FileChannel fc) throws IOException
    {
        BothTagsFileStructure fs = new BothTagsFileStructure();
        if(wavTag.getInfoTag().getStartLocationInFile() < wavTag.getID3Tag().getStartLocationInFile())
        {
            fs.isInfoTagFirst = true;
            //Must allow for odd size chunks
            if(Math.abs(wavTag.getInfoTag().getEndLocationInFile() - wavTag.getStartLocationInFileOfId3Chunk()) <=1)
            {
                fs.isContiguous = true;
                if(isID3TagAtEndOfFileAllowingForPaddingByte(wavTag, fc))
                {
                    fs.isAtEnd = true;
                }
            }
        }
        else
        {
            //Must allow for odd size chunks
            if(Math.abs(wavTag.getID3Tag().getEndLocationInFile() - wavTag.getInfoTag().getStartLocationInFile()) <=1)
            {
                fs.isContiguous = true;
                if(isInfoTagAtEndOfFileAllowingForPaddingByte(wavTag, fc))
                {
                    fs.isAtEnd = true;
                }
            }
        }
        return fs;
    }

    /**
     * Write Info chunk to current location which is last chunk of file
     *
     * @param fc
     * @param existingInfoTag
     * @param newTagBuffer
     * @throws CannotWriteException
     * @throws IOException
     */
    private void writeInfoChunk(FileChannel fc, final WavInfoTag existingInfoTag, ByteBuffer newTagBuffer)
            throws CannotWriteException, IOException
    {
        long newInfoTagSize = newTagBuffer.limit();
        //We have enough existing space in chunk so just keep existing chunk size
        if (existingInfoTag.getSizeOfTag() >= newInfoTagSize)
        {
            writeInfoDataToFile(fc, newTagBuffer, existingInfoTag.getSizeOfTag());
            //To ensure old data from previous tag are erased
            if (existingInfoTag.getSizeOfTag() > newInfoTagSize)
            {
                writePaddingToFile(fc, (int) (existingInfoTag.getSizeOfTag() - newInfoTagSize));
            }
        }
        //New tag is larger so set chunk size to accommodate it
        else
        {
            writeInfoDataToFile(fc, newTagBuffer, newInfoTagSize);
        }
    }

    /**
     * Chunk must also start on an even byte so if our chinksize is odd we need
     * to write another byte
     *
     * @param fc
     * @param size
     * @throws IOException
     */
    private void writeExtraByteIfChunkOddSize(FileChannel fc, long size )
            throws IOException
    {
        if (Utils.isOddLength(size))
        {
            writePaddingToFile(fc, 1);
        }
    }



    /**
     *
     * @param existingTag
     * @param fc
     * @return trueif ID3Tag at end of the file
     *
     * @throws IOException
     */
    private boolean isID3TagAtEndOfFileAllowingForPaddingByte(WavTag existingTag, FileChannel fc) throws IOException
    {
        return ((existingTag.getID3Tag().getEndLocationInFile() == fc.size())||
                (((existingTag.getID3Tag().getEndLocationInFile() & 1) != 0) && existingTag.getID3Tag().getEndLocationInFile() + 1 == fc.size()));
    }

    /**
     *
     * @param existingTag
     * @param fc
     * @return
     * @throws IOException
     */
    private boolean isInfoTagAtEndOfFileAllowingForPaddingByte(WavTag existingTag, FileChannel fc) throws IOException
    {
        return ((existingTag.getInfoTag().getEndLocationInFile() == fc.size())||
                (((existingTag.getInfoTag().getEndLocationInFile() & 1) != 0) && existingTag.getInfoTag().getEndLocationInFile() + 1 == fc.size()));
    }


    /**
     * Save both Info and ID3 chunk
     *
     * @param wavTag
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    private void saveBoth(WavTag wavTag, FileChannel fc,  final WavTag existingTag )
            throws CannotWriteException, IOException
    {
        final ByteBuffer infoTagBuffer = convertInfoChunk(wavTag);
        final long newInfoTagSize = infoTagBuffer.limit();

        final ByteBuffer id3TagBuffer = convertID3Chunk(wavTag, existingTag);

        //Easiest just to delete all metadata (gets rid of duplicates)
        if(WavChunkSummary.isOnlyMetadataTagsAfterStartingMetadataTag(existingTag))
        {
            deleteExistingMetadataTagsToEndOfFile(fc, existingTag);

            if(TagOptionSingleton.getInstance().getWavSaveOrder()==WavSaveOrder.INFO_THEN_ID3)
            {
                writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
                writeId3ChunkAtFileEnd(fc, id3TagBuffer);
            }
            else
            {
                writeId3ChunkAtFileEnd(fc, id3TagBuffer);
                writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
            }
        }
        //Correctly aligned so we can delete each one in turn
        else if(!existingTag.isIncorrectlyAlignedTag())
        {
            if(existingTag.getMetadataChunkSummaryList().size()>0)
            {
                ListIterator<ChunkSummary> li = existingTag.getMetadataChunkSummaryList().listIterator(existingTag.getMetadataChunkSummaryList().size());
                while (li.hasPrevious())
                {
                    ChunkSummary next = li.previous();
                    logger.config(">>>>Deleting--"+next.getChunkId()+"---"+next.getFileStartLocation()+"--"+next.getEndLocation());
                    if (Utils.isOddLength(next.getEndLocation()))
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) ((next.getEndLocation() + 1) - next.getFileStartLocation()));
                    }
                    else
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) (next.getEndLocation() - next.getFileStartLocation()));
                    }
                }
            }
            if(TagOptionSingleton.getInstance().getWavSaveOrder()==WavSaveOrder.INFO_THEN_ID3)
            {
                writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
                writeId3ChunkAtFileEnd(fc, id3TagBuffer);
            }
            else
            {
                writeId3ChunkAtFileEnd(fc, id3TagBuffer);
                writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
            }

        }
        else
        {
            throw new CannotWriteException(loggingName + " Metadata tags are corrupted and not at end of file so cannot be fixed");
        }
    }

    /**
     * Remove id3 and list chunk if exist
     *
     * TODO What about if file has multiple id3 or list chunks
     *
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    public void removeAllMetadata(FileChannel fc,WavTag existingTag)
            throws CannotWriteException, IOException
    {
        if (existingTag.getStartLocationInFileOfId3Chunk() > existingTag.getInfoTag().getStartLocationInFile())
        {
            ChunkHeader id3ChunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);
            deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
            ChunkHeader infoChunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
            deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
        }
        //Not contiguous, delete last (info) tag first than add new id3 tag
        else if (existingTag.getInfoTag().getStartLocationInFile() > existingTag.getStartLocationInFileOfId3Chunk())
        {
            ChunkHeader infoChunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
            deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
            ChunkHeader id3ChunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);
            deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
        }
    }

    /**
     * Write both tags in the order preferred by the options
     *
     * @param fc
     * @param infoTagBuffer
     * @param id3TagBuffer
     * @throws IOException
     */
    public void writeBothTags(FileChannel fc, ByteBuffer infoTagBuffer, ByteBuffer id3TagBuffer)
            throws IOException
    {
        if(TagOptionSingleton.getInstance().getWavSaveOrder()==WavSaveOrder.INFO_THEN_ID3)
        {
            writeInfoDataToFile(fc, infoTagBuffer);
            writeId3DataToFile(fc, id3TagBuffer);
        }
        else
        {
            writeId3DataToFile(fc, id3TagBuffer);
            writeInfoDataToFile(fc, infoTagBuffer);
        }
    }


    /**
     * Find existing ID3 tag, remove and write new ID3 tag at end of file
     *
     * @param fc
     * @param existingTag
     * @param infoTagBuffer
     * @throws CannotWriteException
     * @throws IOException
     */
    public void replaceInfoChunkAtFileEnd(FileChannel fc, WavTag existingTag, ByteBuffer infoTagBuffer) throws CannotWriteException, IOException
    {
        ChunkHeader infoChunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
        if (isInfoTagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
        {
            logger.severe("writinginfo");
            writeInfoChunk(fc, existingTag.getInfoTag(), infoTagBuffer);
        }
        else
        {
            deleteInfoChunkAndCreateNewOneAtFileEnd( fc, existingTag, infoChunkHeader, infoTagBuffer);
        }
    }

    /**
     * Remove existing INFO tag wherever it is
     *
     * @param fc
     * @param existingTag
     * @throws IOException
     */
    public void deleteOrTruncateId3Tag(FileChannel fc, WavTag existingTag) throws CannotWriteException, IOException
    {
        if (isID3TagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
        {
            fc.truncate(existingTag.getStartLocationInFileOfId3Chunk());
        }
        else
        {
            ChunkHeader id3ChunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);
            deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
        }
    }

    /**
     * Delete Existing Id3 chunk wherever it is , then write Id3 chunk at end of file
     *
     * @param fc
     * @param existingTag
     * @param id3ChunkHeader
     * @param infoTagBuffer
     * @throws IOException
     */
    public void deleteInfoChunkAndCreateNewOneAtFileEnd(FileChannel fc, WavTag existingTag, ChunkHeader id3ChunkHeader, ByteBuffer infoTagBuffer)
            throws IOException
    {
        deleteInfoTagChunk(fc, existingTag, id3ChunkHeader);
        fc.position(fc.size());
        writeInfoDataToFile(fc, infoTagBuffer);
    }
    /**
     *
     * @param wavTag
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    public void saveInfo(WavTag wavTag, FileChannel fc,  final WavTag existingTag )
            throws CannotWriteException, IOException
    {
        final ByteBuffer infoTagBuffer = convertInfoChunk(wavTag);
        final long newInfoTagSize = infoTagBuffer.limit();

        //Easiest just to delete all metadata (gets rid of duplicates)
        if(WavChunkSummary.isOnlyMetadataTagsAfterStartingMetadataTag(existingTag))
        {
            deleteExistingMetadataTagsToEndOfFile(fc, existingTag);
            writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
        }
        //Correctly aligned so we can delete each one in turn
        else if(!existingTag.isIncorrectlyAlignedTag())
        {
            if(existingTag.getMetadataChunkSummaryList().size()>0)
            {
                ListIterator<ChunkSummary> li = existingTag.getMetadataChunkSummaryList().listIterator(existingTag.getMetadataChunkSummaryList().size());
                while (li.hasPrevious())
                {
                    ChunkSummary next = li.previous();
                    logger.config(">>>>Deleting--"+next.getChunkId()+"---"+next.getFileStartLocation()+"--"+next.getEndLocation());
                    if (Utils.isOddLength(next.getEndLocation()))
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) ((next.getEndLocation() + 1) - next.getFileStartLocation()));
                    }
                    else
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) (next.getEndLocation() - next.getFileStartLocation()));
                    }
                }
            }
            writeInfoChunkAtFileEnd(fc, infoTagBuffer, newInfoTagSize);
        }
        else
        {
            throw new CannotWriteException(loggingName + " Metadata tags are corrupted and not at end of file so cannot be fixed");
        }
    }

    /**
     * Write Info chunk at end of file
     *
     * @param fc
     * @param infoTagBuffer
     * @throws IOException
     */
    private void writeInfoChunkAtFileEnd(FileChannel fc, ByteBuffer infoTagBuffer, long newInfoTagSize)
            throws IOException
    {
        fc.position(fc.size());
        writeInfoDataToFile(fc, infoTagBuffer, newInfoTagSize);
    }



    /**
     *
     * @param wavTag
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    private void saveId3(WavTag wavTag, FileChannel fc,  final WavTag existingTag )
            throws CannotWriteException, IOException
    {
        final ByteBuffer id3TagBuffer = convertID3Chunk(wavTag, existingTag);

        //Easiest just to delete all metadata (gets rid of duplicates)
        if(WavChunkSummary.isOnlyMetadataTagsAfterStartingMetadataTag(existingTag))
        {
            deleteExistingMetadataTagsToEndOfFile(fc, existingTag);
            writeId3ChunkAtFileEnd(fc, id3TagBuffer);
        }
        //Correctly aligned so we can delete each one in turn
        else if(!existingTag.isIncorrectlyAlignedTag())
        {
            if(existingTag.getMetadataChunkSummaryList().size()>0)
            {
                ListIterator<ChunkSummary> li = existingTag.getMetadataChunkSummaryList().listIterator(existingTag.getMetadataChunkSummaryList().size());
                while (li.hasPrevious())
                {
                    ChunkSummary next = li.previous();
                    logger.config(">>>>Deleting--"+next.getChunkId()+"---"+next.getFileStartLocation()+"--"+next.getEndLocation());
                    if (Utils.isOddLength(next.getEndLocation()))
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) ((next.getEndLocation() + 1) - next.getFileStartLocation()));
                    }
                    else
                    {
                        deleteTagChunk(fc, (int) next.getEndLocation(), (int) (next.getEndLocation() - next.getFileStartLocation()));
                    }
                }
            }
            writeId3ChunkAtFileEnd(fc, id3TagBuffer);
        }
        else
        {
            throw new CannotWriteException(loggingName + " Metadata tags are corrupted and not at end of file so cannot be fixed");
        }
    }

    /**
     * Find existing ID3 tag, remove and write new ID3 tag at end of file
     *
     * @param fc
     * @param existingTag
     * @param id3TagBuffer
     * @throws CannotWriteException
     * @throws IOException
     */
    public void replaceId3ChunkAtFileEnd(FileChannel fc, WavTag existingTag, ByteBuffer id3TagBuffer) throws CannotWriteException, IOException
    {
        ChunkHeader id3ChunkHeader = seekToStartOfId3MetadataForChunkSummaryHeader(fc, existingTag);
        if (isID3TagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
        {
            writeId3DataToFile(fc, id3TagBuffer);
        }
        else
        {
            deleteId3ChunkAndCreateNewOneAtFileEnd( fc, existingTag, id3ChunkHeader, id3TagBuffer);
        }
    }
    /**
     * Remove existing INFO tag wherever it is
     *
     * @param fc
     * @param existingTag
     * @throws IOException
     */
    public void deleteOrTruncateInfoTag(FileChannel fc, WavTag existingTag) throws CannotWriteException, IOException
    {
        ChunkHeader infoChunkHeader = seekToStartOfListInfoMetadata(fc, existingTag);
        if (isInfoTagAtEndOfFileAllowingForPaddingByte(existingTag, fc))
        {
            fc.truncate(existingTag.getInfoTag().getStartLocationInFile());
        }
        else
        {
            deleteInfoTagChunk(fc, existingTag, infoChunkHeader);
        }
    }

    /**
     * Write Id3 chunk at end of file
     *
     * @param fc
     * @param id3TagBuffer
     * @throws IOException
     */
    private void writeId3ChunkAtFileEnd(FileChannel fc, ByteBuffer id3TagBuffer)
            throws IOException
    {
        fc.position(fc.size());
        writeId3DataToFile(fc, id3TagBuffer);
    }

    /**
     * Delete Existing Id3 chunk wherever it is , then write Id3 chunk at end of file
     *
     * @param fc
     * @param existingTag
     * @param id3ChunkHeader
     * @param id3TagBuffer
     * @throws IOException
     */
    private void deleteId3ChunkAndCreateNewOneAtFileEnd(FileChannel fc, WavTag existingTag, ChunkHeader id3ChunkHeader, ByteBuffer id3TagBuffer)
            throws IOException
    {
        deleteId3TagChunk(fc, existingTag, id3ChunkHeader);
        fc.position(fc.size());
        writeId3DataToFile(fc, id3TagBuffer);
    }
    /**
     * Save Active chunk only, if a non-active metadata chunk exists will be removed
     *
     * @param wavTag
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    private void saveActive(WavTag wavTag, FileChannel fc,  final WavTag existingTag )
            throws CannotWriteException, IOException
    {
        //Info is Active Tag
        if (wavTag.getActiveTag() instanceof WavInfoTag)
        {
            saveInfo(wavTag, fc, existingTag);
        }
        //ID3 is Active Tag
        else
        {
            saveId3(wavTag, fc, existingTag);
        }
    }

    /**
     * Save Active chunk and existing chunks even if not the active chunk
     *
     * @param wavTag
     * @param fc
     * @param existingTag
     * @throws CannotWriteException
     * @throws IOException
     */
    private void saveActiveExisting(WavTag wavTag, FileChannel fc,  final WavTag existingTag )
            throws CannotWriteException, IOException
    {
        if(wavTag.getActiveTag() instanceof WavInfoTag)
        {
            if(existingTag.isExistingId3Tag())
            {
                saveBoth(wavTag, fc, existingTag);
            }
            else
            {
                saveActive(wavTag, fc,  existingTag );
            }
        }
        else
        {
            if(existingTag.isExistingInfoTag())
            {
                saveBoth(wavTag, fc,  existingTag );
            }
            else
            {
                saveActive(wavTag, fc,  existingTag );
            }
        }
    }

    /** If Info/ID3 Metadata tags are corrupted and only metadata tags later in the file then just truncate metadata tags and start again
     *
     * @param fc
     * @param existingTag
     * @throws IOException
     */
    private void deleteExistingMetadataTagsToEndOfFile(final FileChannel fc, final WavTag existingTag) throws IOException
    {
        ChunkSummary precedingChunk = WavChunkSummary.getChunkBeforeFirstMetadataTag(existingTag);
        //Preceding chunk ends on odd boundary
        if(!Utils.isOddLength(precedingChunk.getEndLocation()))
        {
            fc.truncate(precedingChunk.getEndLocation());
        }
        //Preceding chunk ends on even boundary
        else
        {
            fc.truncate(precedingChunk.getEndLocation() + 1);
        }
    }
}