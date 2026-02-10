/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphaël Slinckx <raphael@slinckx.net>
 * Copyright (c) 2004-2005 Christian Laireiter <liree@web.de>
 * Copyright (c) 2026 Sergio Camacho <sergio@secam.dev>
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
package org.jaudiotagger.audio.opus;

import android.util.Log;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.ogg.util.OggCRCFactory;
import org.jaudiotagger.audio.ogg.util.OggPageHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3v1Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Write VorbisComment Tag within an ogg opus file
 * VorbisComment holds the tag information within an ogg file
 */
public class OggOpusTagWriter
{
    private final OggOpusCommentTagCreator tc = new OggOpusCommentTagCreator();
    private final OggOpusTagReader reader = new OggOpusTagReader();

    public void delete(RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotReadException, CannotWriteException
    {
        try
        {
            reader.read(raf);
        }
        catch (CannotReadException e)
        {
            write(VorbisCommentTag.createNewTag(), raf, tempRaf);
            return;
        }

        VorbisCommentTag emptyTag = VorbisCommentTag.createNewTag();

        //Go back to start of file
        raf.seek(0);
        write(emptyTag, raf, tempRaf);
    }

    public void write(Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotReadException, CannotWriteException, IOException
    {

        //1st Page:Identification Header
        OggPageHeader pageHeader = OggPageHeader.read(raf);
        raf.seek(pageHeader.getStartByte());

        //Write 1st page (unchanged) and place writer pointer at end of data
        rafTemp.getChannel().transferFrom(raf.getChannel(), 0, pageHeader.getPageLength() + OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + pageHeader.getSegmentTable().length);
        rafTemp.skipBytes(pageHeader.getPageLength() + OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + pageHeader.getSegmentTable().length);

        //2nd page: Comment
        OggPageHeader secondPageHeader = OggPageHeader.read(raf);

        //2nd Page: Store the end of Header
        long secondPageHeaderEndPos = raf.getFilePointer();

        //Get header sizes
        raf.seek(0);
        int opusHeaderSize = reader.readOggOpusHeaderSizes(raf);

        //Convert the OggVorbisComment header to raw packet data
        ByteBuffer newComment = tc.convert(tag);

        //Compute new comment length(this may need to be spread over multiple pages)
        int newCommentLength = newComment.capacity();

        // TODO: fix all the different cases
        // TODO: also fix replay gain to proper spec

        //  New Vorbis Comment fits on one page
        if (isCommentHeaderFitsOnASinglePage(newCommentLength))
        {
            //  Original VorbisComment only one page
            if (!secondPageHeader.isLastPacketIncomplete())
            {
                replaceSecondPageOnly(opusHeaderSize, newCommentLength, secondPageHeader, newComment, secondPageHeaderEndPos, raf, rafTemp);
            }
            //  Original 2nd page spanned multiple pages so more work to do
            // TODO: idk if this works
            else
            {
                Log.d(this.getClass().getSimpleName(), "Header on one page2");

                replaceSecondPageAndRenumberPageSeqs(newCommentLength, newCommentLength, secondPageHeader, newComment, raf, rafTemp);
            }
        }
        //  New VorbisComment does not fit on one page
        //  Bit more complicated, have to create more than one new page and renumber subsequent audio
        else
        {
            replacePagesAndRenumberPageSeqs(opusHeaderSize, newCommentLength, secondPageHeader, newComment, raf, rafTemp);
        }
    }

    /**
     * Calculate checkSum over the Page
     *
     * @param page raw page data to calculate checksum from
     */
    private void calculateChecksumOverPage(ByteBuffer page) {
        //CRC should be zero before calculating it
        page.putInt(OggPageHeader.FIELD_PAGE_CHECKSUM_POS, 0);

        //Compute CRC over the  page  //TODO shouldn't really use array();
        byte[] crc = OggCRCFactory.computeCRC(page.array());
        for (int i = 0; i < crc.length; i++)
        {
            page.put(OggPageHeader.FIELD_PAGE_CHECKSUM_POS + i, crc[i]);
        }

        //Rewind to start of Page
        page.rewind();
    }

    /**
     * Create a second Page, and add comment header to it, but page is incomplete may want to add addition header and need to calculate CRC
     *
     * @param newCommentLength length of new VorbisComment data
     * @param secondPageHeader original second page header
     * @param newComment new VorbisComment data
     * @return new second page raw data
     */
    private ByteBuffer startCreateBasicSecondPage(
            int newCommentLength,
            OggPageHeader secondPageHeader,
            ByteBuffer newComment
    ) throws IOException {
        byte[] segmentTable = createSegmentTable(newCommentLength);
        int newSecondPageHeaderLength = OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + segmentTable.length;

        ByteBuffer secondPageBuffer = ByteBuffer.allocate(newCommentLength + newSecondPageHeaderLength);
        secondPageBuffer.order(ByteOrder.LITTLE_ENDIAN);

        //Build the new 2nd page header, can mostly be taken from the original up to the segment length OggS capture
        secondPageBuffer.put(secondPageHeader.getRawHeaderData(), 0, OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH - 1);

        //Number of Page Segments
        secondPageBuffer.put((byte) segmentTable.length);

        //Page segment table
        for (byte aSegmentTable : segmentTable) {
            secondPageBuffer.put(aSegmentTable);
        }

        //Add New VorbisComment
        secondPageBuffer.put(newComment);
        return secondPageBuffer;
    }


    /**
     * Usually can use this method, previously comment and setup header all fit on page 2
     * and they still do, so just replace this page. And copy further pages as is.
     *
     * @param commentHeaderSize original comment header size
     * @param newCommentLength length of second page with new data
     * @param secondPageHeader original second page header
     * @param newComment new VorbisComment data
     * @param secondPageHeaderEndPos original second page header end pos
     * @param raf audio file
     * @param rafTemp temp audio file
     */
    private void replaceSecondPageOnly(
            int commentHeaderSize,
            int newCommentLength,
            OggPageHeader secondPageHeader,
            ByteBuffer newComment,
            long secondPageHeaderEndPos,
            RandomAccessFile raf,
            RandomAccessFile rafTemp) throws IOException, CannotWriteException {
        ByteBuffer secondPageBuffer = startCreateBasicSecondPage(newCommentLength, secondPageHeader, newComment);

        raf.seek(secondPageHeaderEndPos);
        //Skip comment header
        raf.skipBytes(commentHeaderSize);
        //Read in setup header and extra packets
        raf.getChannel().read(secondPageBuffer);
        calculateChecksumOverPage(secondPageBuffer);
        if(rafTemp.getChannel().write(secondPageBuffer) < 1){
            throw new CannotWriteException("No header data written to file");
        }
        rafTemp.getChannel().transferFrom(raf.getChannel(), rafTemp.getFilePointer(), raf.length() - raf.getFilePointer());
    }

    /**
     * Previously comment and/or setup header was on a number of pages now can just replace this page fitting all
     * on 2nd page, and renumber subsequent sequence pages
     *
     * @param newCommentLength length of new VorbisComment data
     * @param newSecondPageLength length of second page with new data
     * @param secondPageHeader original second page header
     * @param newComment new VorbisComment data
     * @param raf audio file
     * @param rafTemp temp audio file
     */
    private void replaceSecondPageAndRenumberPageSeqs(int newCommentLength, int newSecondPageLength, OggPageHeader secondPageHeader, ByteBuffer newComment, RandomAccessFile raf, RandomAccessFile rafTemp) throws IOException, CannotReadException, CannotWriteException
    {
        ByteBuffer secondPageBuffer = startCreateBasicSecondPage(newCommentLength, secondPageHeader, newComment);

        //Add setup header and packets
        int pageSequence = secondPageHeader.getPageSequence();

        calculateChecksumOverPage(secondPageBuffer);
        if(rafTemp.getChannel().write(secondPageBuffer) < 1){
            throw new CannotWriteException("No header data written to file");
        }
        writeRemainingPages(pageSequence, raf, rafTemp);
    }

    /**
     * CommentHeader extends over multiple pages OR Comment Header doesn't but it's got larger causing some extra
     * packets to be shifted onto another page.
     *
     * @param originalHeaderSizes
     * @param newCommentLength
     * @param secondPageHeader
     * @param newComment
     * @param raf
     * @param rafTemp
     * @throws IOException
     * @throws CannotReadException
     * @throws CannotWriteException
     */
    private void replacePagesAndRenumberPageSeqs(int originalHeaderSizes, int newCommentLength, OggPageHeader secondPageHeader, ByteBuffer newComment, RandomAccessFile raf, RandomAccessFile rafTemp) throws IOException, CannotReadException, CannotWriteException
    {
        int pageSequence = secondPageHeader.getPageSequence();

        //We need to work out how to split the new comment length over the pages
        int noOfCompletePagesNeededForComment = newCommentLength / OggPageHeader.MAXIMUM_PAGE_DATA_SIZE;

        //Create the Pages
        int newCommentOffset = 0;
        if (noOfCompletePagesNeededForComment > 0)
        {
            for (int i = 0; i < noOfCompletePagesNeededForComment; i++)
            {
                //Create ByteBuffer for the New page
                byte[] segmentTable = this.createSegments(OggPageHeader.MAXIMUM_PAGE_DATA_SIZE, false);
                int pageHeaderLength = OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + segmentTable.length;
                ByteBuffer pageBuffer = ByteBuffer.allocate(pageHeaderLength + OggPageHeader.MAXIMUM_PAGE_DATA_SIZE);
                pageBuffer.order(ByteOrder.LITTLE_ENDIAN);

                //Now create the page basing it on the existing 2nd page header
                pageBuffer.put(secondPageHeader.getRawHeaderData(), 0, OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH - 1);
                //Number of Page Segments
                pageBuffer.put((byte) segmentTable.length);
                //Page segment table
                for (byte aSegmentTable : segmentTable)
                {
                    pageBuffer.put(aSegmentTable);
                }
                //Get next bit of Comment
                ByteBuffer nextPartOfComment = newComment.slice();
                nextPartOfComment.limit(OggPageHeader.MAXIMUM_PAGE_DATA_SIZE);
                pageBuffer.put(nextPartOfComment);

                //Recalculate Page Sequence Number
                pageBuffer.putInt(OggPageHeader.FIELD_PAGE_SEQUENCE_NO_POS, pageSequence);
                pageSequence++;

                //Set Header Flag to indicate continuous (except for first flag)
                if (i != 0)
                {
                    pageBuffer.put(OggPageHeader.FIELD_HEADER_TYPE_FLAG_POS, OggPageHeader.HeaderTypeFlag.CONTINUED_PACKET.getFileValue());
                }
                calculateChecksumOverPage(pageBuffer);
                if(rafTemp.getChannel().write(pageBuffer) < 1){
                    throw new CannotWriteException("No page data written to file");
                }
                newCommentOffset += OggPageHeader.MAXIMUM_PAGE_DATA_SIZE;
                newComment.position(newCommentOffset);
            }
        }

        int lastPageCommentPacketSize = newCommentLength % OggPageHeader.MAXIMUM_PAGE_DATA_SIZE;

        //End of comment and setup header cannot fit on the last page
        if (!isCommentHeaderFitsOnASinglePage(lastPageCommentPacketSize))
        {

            //Write the last part of comment only (its possible it might be the only comment)
            {
                byte[] segmentTable = createSegments(lastPageCommentPacketSize, true);
                int pageHeaderLength = OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + segmentTable.length;
                ByteBuffer pageBuffer = ByteBuffer.allocate(lastPageCommentPacketSize + pageHeaderLength);
                pageBuffer.order(ByteOrder.LITTLE_ENDIAN);
                pageBuffer.put(secondPageHeader.getRawHeaderData(), 0, OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH - 1);
                pageBuffer.put((byte) segmentTable.length);
                for (byte aSegmentTable : segmentTable)
                {
                    pageBuffer.put(aSegmentTable);
                }
                newComment.position(newCommentOffset);
                pageBuffer.put(newComment.slice());
                pageBuffer.putInt(OggPageHeader.FIELD_PAGE_SEQUENCE_NO_POS, pageSequence);

                if(noOfCompletePagesNeededForComment>0)
                {
                    pageBuffer.put(OggPageHeader.FIELD_HEADER_TYPE_FLAG_POS, OggPageHeader.HeaderTypeFlag.CONTINUED_PACKET.getFileValue());
                }
                pageSequence++;
                calculateChecksumOverPage(pageBuffer);
                if(rafTemp.getChannel().write(pageBuffer) < 1){
                    throw new CannotWriteException("No page data written to file");
                }
            }

        }
        else
        {
            //End of Comment and SetupHeader and extra packets can fit on one page

            //Create last header page
            newComment.position(newCommentOffset);
            ByteBuffer lastComment = newComment.slice();
            ByteBuffer lastHeaderBuffer = startCreateBasicSecondPage(lastPageCommentPacketSize, secondPageHeader, lastComment);

            //Page Sequence No
            lastHeaderBuffer.putInt(OggPageHeader.FIELD_PAGE_SEQUENCE_NO_POS, pageSequence);

            //Set Header Flag to indicate continuous (contains end of comment)
            lastHeaderBuffer.put(OggPageHeader.FIELD_HEADER_TYPE_FLAG_POS, OggPageHeader.HeaderTypeFlag.CONTINUED_PACKET.getFileValue());
            calculateChecksumOverPage(lastHeaderBuffer);
            if(rafTemp.getChannel().write(lastHeaderBuffer) < 1){
                throw new CannotWriteException("No header data written to file");
            }
        }

        //Write the rest of the original file
        writeRemainingPages(pageSequence, raf, rafTemp);
    }

    /**
     * Write all the remaining pages as they are except that the page sequence needs to be modified.
     *
     * @param pageSequence
     * @param raf
     * @param rafTemp
     */
    public void writeRemainingPages(int pageSequence, RandomAccessFile raf, RandomAccessFile rafTemp) throws IOException, CannotReadException, CannotWriteException
    {
        long startAudio = raf.getFilePointer();
        long startAudioWritten = rafTemp.getFilePointer();

        //TODO there is a risk we wont have enough memory to create these buffers
        ByteBuffer bb       = ByteBuffer.allocate((int) (raf.length() - raf.getFilePointer()));
        ByteBuffer bbTemp   = ByteBuffer.allocate((int)(raf.length() - raf.getFilePointer()));

        //Read in the rest of the data into bytebuffer and rewind it to start
        raf.getChannel().read(bb);
        bb.rewind();
        long bytesToDiscard = 0;
        while(bb.hasRemaining())
        {
            OggPageHeader nextPage;
            try
            {
                nextPage = OggPageHeader.read(bb);
            }
            catch(CannotReadException cre)
            {
                //Go back to where were
                bb.position(bb.position() - OggPageHeader.CAPTURE_PATTERN.length);
                //#117:Ogg file with invalid ID3v1 tag at end remove and save
                if(Utils.readThreeBytesAsChars(bb).equals(AbstractID3v1Tag.TAG))
                {
                    bytesToDiscard = bb.remaining() + AbstractID3v1Tag.TAG.length();
                    break;
                }
                else
                {
                    throw cre;
                }
            }
            //Create buffer large enough for next page (header and data) and set byte order to LE so we can use
            //putInt method
            ByteBuffer nextPageHeaderBuffer = ByteBuffer.allocate(nextPage.getRawHeaderData().length + nextPage.getPageLength());
            nextPageHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
            nextPageHeaderBuffer.put(nextPage.getRawHeaderData());
            ByteBuffer data = bb.slice();
            data.limit(nextPage.getPageLength());
            nextPageHeaderBuffer.put(data);
            nextPageHeaderBuffer.putInt(OggPageHeader.FIELD_PAGE_SEQUENCE_NO_POS, ++pageSequence);
            calculateChecksumOverPage(nextPageHeaderBuffer);
            bb.position(bb.position() + nextPage.getPageLength());

            nextPageHeaderBuffer.rewind();
            bbTemp.put(nextPageHeaderBuffer);
        }
        //Now just write as a single IO operation
        bbTemp.flip();
        if(rafTemp.getChannel().write(bbTemp) < 1){
            throw new CannotWriteException("No page data written to file");
        }
        //Check we have written all the data (minus any invalid Tag at end)
        if ((raf.length() - startAudio) != ((rafTemp.length() + bytesToDiscard) - startAudioWritten))
        {
            throw new CannotWriteException("File written counts don't match, file not written:"
                    +"origAudioLength:"+(raf.length() - startAudio)
                    +":newAudioLength:"+((rafTemp.length() + bytesToDiscard) - startAudioWritten)
                    +":bytesDiscarded:"+bytesToDiscard);
        }
    }

    /**
     * This method creates a new segment table for the second page (header).
     *
     * @param newCommentLength  The length of the Vorbis Comment
     * @return new segment table.
     */
    private byte[] createSegmentTable(int newCommentLength)
    {
        byte[] newStart;

        newStart = createSegments(newCommentLength, false);
        return newStart;
    }


    /**
     * This method creates a byte array of values whose sum should
     * be the value of <code>length</code>.<br>
     *
     * @param length     Size of the page which should be
     *                   represented as 255 byte packets.
     * @param quitStream If true and a length is a multiple of 255 we need another
     *                   segment table entry with the value of 0. Else it's the last stream of the
     *                   table which is already ended.
     * @return Array of packet sizes. However only the last packet will
     *         differ from 255.
     *
     */
    //TODO if pass is data of max length (65025 bytes) and have quitStream==true
    //this will return 256 segments which is illegal, should be checked somewhere
    private byte[] createSegments(int length, boolean quitStream)
    {
        //It is valid to have nil length packets
        if (length == 0)
        {
            byte[] result = new byte[1];
            result[0] = (byte) 0x00;
            return result;
        }

        byte[] result = new byte[length / OggPageHeader.MAXIMUM_SEGMENT_SIZE + ((length % OggPageHeader.MAXIMUM_SEGMENT_SIZE == 0 && !quitStream) ? 0 : 1)];
        int i = 0;
        for (; i < result.length - 1; i++)
        {
            result[i] = (byte) 0xFF;
        }
        result[result.length - 1] = (byte) (length - (i * OggPageHeader.MAXIMUM_SEGMENT_SIZE));
        return result;
    }

    /**
     * @param commentLength length of new comment
     * @return true if there is enough room to fit the comment and the setup headers on one page taking into
     *         account the maximum no of segments allowed per page and zero lacing values.
     */
    private boolean isCommentHeaderFitsOnASinglePage(int commentLength)
    {
        int totalDataSize = 0;

        if (commentLength == 0)
        {
            totalDataSize++;
        }
        else
        {
            totalDataSize = (commentLength / OggPageHeader.MAXIMUM_SEGMENT_SIZE) + 1;
            if (commentLength % OggPageHeader.MAXIMUM_SEGMENT_SIZE == 0)
            {
                totalDataSize++;
            }
        }

        return totalDataSize <= OggPageHeader.MAXIMUM_NO_OF_SEGMENT_SIZE;
    }

}