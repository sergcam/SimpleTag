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

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.ogg.util.OggPageHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Read Vorbis Comment Tag within opus ogg
 * Opus is the audio stream within an ogg file, Opus uses slightly modified VorbisComments as its tag
 */
public class OggOpusTagReader
{
    private final VorbisCommentReader vorbisCommentReader;

    public OggOpusTagReader()
    {
        vorbisCommentReader = new VorbisCommentReader();
    }

    

    /**
     * Read the Comment Tag from file, within an OggOpus file the VorbisCommentTag is mandatory
     *
     * @param raf audio file
     * @return Tag object built from VorbisComment
     * @throws CannotReadException unable to read file
     */
    public Tag read(RandomAccessFile raf) throws CannotReadException, IOException
    {
        //  Starting to read ogg vorbis tag from file
        byte[] rawVorbisCommentData = readRawPacketData(raf);

        //  Begin tag reading
        return vorbisCommentReader.read(rawVorbisCommentData, false, null);
    }

    /**
     * Retrieve the raw VorbisComment packet data, does not include the OggVorbis header
     *
     * @param raf audio file
     * @return byte array of raw comment data
     * @throws CannotReadException if unable to find vorbiscomment header
     */
    public byte[] readRawPacketData(RandomAccessFile raf) throws CannotReadException, IOException
    {
        //1st page = audio codec infos, not touching this
        OggPageHeader pageHeader = OggPageHeader.read(raf);
        //Skip over data to end of page header 1
        raf.seek(raf.getFilePointer() + pageHeader.getPageLength());

        //2nd page = comment, may extend to additional pages or not
        pageHeader = OggPageHeader.read(raf);

        //Now at start of packets on page 2 , check this is the vorbis comment header 
        byte[] b = new byte[OpusHeader.FIELD_CAPTURE_PATTERN_LENGTH];
        raf.read(b);
        if (isOpusCommentHeader(b))
        {
            //Convert the comment raw data which maybe over many pages back into raw packet
            return convertToVorbisCommentPacket(pageHeader, raf);

        }
        else {
            throw new CannotReadException("Cannot find comment block (no OpusTags header)");
        }
    }


    /**
     * Is this a Vorbis Comment header, check
     * Note this check only applies to Vorbis Comments embedded within an OggVorbis File which is why within here
     *
     * @param headerData comment header data to check
     * @return true if the headerData matches an opus VorbisComment header i.e equals exactly "OpusTags"
     */
    public boolean isOpusCommentHeader(byte[] headerData)
    {
        String opus = new String(headerData, OpusHeader.FIELD_CAPTURE_PATTERN_POS, OpusHeader.FIELD_CAPTURE_PATTERN_LENGTH, StandardCharsets.ISO_8859_1);
        return opus.equals(OpusHeader.CAPTURE_PATTERN);
    }

    /**
     * The Vorbis Comment may span multiple pages so we we need to identify the pages they contain and then
     * extract the packet data from the pages
     * @param startVorbisCommentPage first page of multipage VorbisComment
     * @param raf audio file
     * @throws CannotReadException if unable to find vorbiscomment header
     * @return byte array of raw comment data
     */
    private byte[] convertToVorbisCommentPacket(OggPageHeader startVorbisCommentPage, RandomAccessFile raf) throws IOException, CannotReadException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[startVorbisCommentPage.getPacketList().get(0).getLength() - (OpusHeader.FIELD_CAPTURE_PATTERN_LENGTH)];
        raf.read(b);
        baos.write(b);

        //There is only the VorbisComment packet on page if it has completed on this page we can return
        if (!startVorbisCommentPage.isLastPacketIncomplete())
        {
            //  Comments finish on 2nd Page because this packet is complete
            return baos.toByteArray();
        }

        //The VorbisComment extends to the next page, so should be at end of page already
        //so carry on reading pages until we get to the end of comment
        while (true)
        {
            //  Reading next page
            OggPageHeader nextPageHeader = OggPageHeader.read(raf);
            b = new byte[nextPageHeader.getPacketList().get(0).getLength()];
            raf.read(b);
            baos.write(b);

            //There is only the VorbisComment packet on page if it has completed on this page we can return
            if (!nextPageHeader.isLastPacketIncomplete())
            {
                return baos.toByteArray();
            }
        }
    }


    /**
     * Calculate the size of the packet data for the comment and setup headers
     *
     * @param raf audio file
     * @return Object containing comment header size and start pos
     * @throws CannotReadException if unable to find vorbiscomment header
     */
    public int readOggOpusHeaderSizes(RandomAccessFile raf) throws CannotReadException, IOException
    {
        //Stores file pointers so return file in same state
        long filePointer = raf.getFilePointer();

//        long commentHeaderStartPosition;
        int commentHeaderSize = 0;

        //1st page = codec infos
        OggPageHeader pageHeader = OggPageHeader.read(raf);
        //Skip over data to end of page header 1
        raf.seek(raf.getFilePointer() + pageHeader.getPageLength());

        //2nd page = comment, may extend to additional pages or not , may also have setup header
        pageHeader = OggPageHeader.read(raf);
//        commentHeaderStartPosition = raf.getFilePointer() - (OggPageHeader.OGG_PAGE_HEADER_FIXED_LENGTH + pageHeader.getSegmentTable().length);

        //Now at start of packets on page 2 , check this is the vorbis comment header
        byte[] b = new byte[OpusHeader.FIELD_CAPTURE_PATTERN_LENGTH];
        raf.read(b);

        if (!isOpusCommentHeader(b))
        {
            throw new CannotReadException("Cannot find comment block (no OpusTags header)");
        }
        raf.seek(raf.getFilePointer() - (OpusHeader.FIELD_CAPTURE_PATTERN_LENGTH));

        //Calculate Comment Size (not inc header)
        while (true)
        {
            List<OggPageHeader.PacketStartAndLength> packetList = pageHeader.getPacketList();
            commentHeaderSize += packetList.get(0).getLength();
            raf.skipBytes(packetList.get(0).getLength());

            //If this page contains multiple packets or if this last packet is complete then the Comment header
            //end son this page and we can break
            if (!pageHeader.isLastPacketIncomplete())
            {
                //done comment size
                break;
            }
            pageHeader = OggPageHeader.read(raf);
        }

        //  Reset filePointer to location that it was in at start of method
        raf.seek(filePointer);
        return commentHeaderSize;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}


