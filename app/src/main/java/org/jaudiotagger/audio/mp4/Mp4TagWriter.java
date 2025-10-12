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
package org.jaudiotagger.audio.mp4;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.mp4.atom.*;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.Mp4TagCreator;
import org.jaudiotagger.utils.ShiftData;
import org.jaudiotagger.utils.tree.DefaultMutableTreeNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;


/**
 * Writes metadata from mp4, the metadata tags are held under the {@code ilst} atom as shown below, (note all free atoms are
 * optional).
 * <p/>
 * When writing changes the size of all the atoms up to {@code ilst} has to be recalculated, then if the size of
 * the
 * <p/>
 * If the size of the metadata has increased by more than the size of the {@code free} atom then the size of its parents
 * have to be recalculated. This means {@code meta}, {@code udta} and {@code moov} should be recalculated and the top
 * level {@code free} atom reduced accordingly.
 * <p/>
 * If there is not enough space even if using both of the {@code free} atoms, then the {@code mdat} atom has to be
 * shifted down accordingly to make space, and the {@code stco} atoms have to have their offsets to {@code mdat}
 * chunks table adjusted accordingly.
 * <p/>
 * Exceptions are that the meta/udta/ilst do not currently exist, in which udta/meta/ilst are created. Note it is valid
 * to have meta/ilst without udta but this is less common so we always try to write files according to the Apple/iTunes
 * specification. *
 * <p/>
 * <p/>
 * <pre>
 * |--- ftyp
 * |--- free
 * |--- moov
 * |......|
 * |......|----- mvdh
 * |......|----- trak (there may be more than one trak atom, e.g. Native Instrument STEM files)
 * |......|.......|
 * |......|.......|-- tkhd
 * |......|.......|-- mdia
 * |......|............|
 * |......|............|-- mdhd
 * |......|............|-- hdlr
 * |......|............|-- minf
 * |......|.................|
 * |......|.................|-- smhd
 * |......|.................|-- dinf
 * |......|.................|-- stbl
 * |......|......................|
 * |......|......................|-- stsd
 * |......|......................|-- stts
 * |......|......................|-- stsc
 * |......|......................|-- stsz
 * |......|......................|-- stco (important! may need to be adjusted.)
 * |......|
 * |......|----- udta
 * |..............|
 * |..............|-- meta
 * |....................|
 * |....................|-- hdlr
 * |....................|-- ilst
 * |....................|.. ..|
 * |....................|.....|---- @nam (Optional for each metadatafield)
 * |....................|.....|.......|-- data
 * |....................|.....|....... ecetera
 * |....................|.....|---- ---- (Optional for reverse dns field)
 * |....................|.............|-- mean
 * |....................|.............|-- name
 * |....................|.............|-- data
 * |....................|................ ecetera
 * |....................|-- free
 * |--- free
 * |--- mdat
 * </pre>
 */
public class Mp4TagWriter
{
    // Logger Object
    public static Logger logger = Logger.getLogger("org.jaudiotagger.tag.mp4");

    private Mp4TagCreator tc = new Mp4TagCreator();

    //For logging
    private String loggingName;
    public Mp4TagWriter(String loggingName)
    {
        this.loggingName = loggingName;
    }


    /**
     * Replace the {@code ilst} metadata.
     * <p/>
     * Because it is the same size as the original data nothing else has to be modified.
     *
     * @param fc
     * @param newIlstData
     * @throws CannotWriteException
     * @throws IOException
     */
    private void writeMetadataSameSize(SeekableByteChannel fc, Mp4BoxHeader ilstHeader, ByteBuffer newIlstData) throws IOException
    {
        logger.config("Writing:Option 1:Same Size");
        fc.position(ilstHeader.getFilePos());
        fc.write(newIlstData);
    }

    /**
     * When the size of the metadata has changed and it can't be compensated for by {@code free} atom
     * we have to adjust the size of the size field up to the moovheader level for the {@code udta} atom and
     * its child {@code meta} atom.
     *
     * @param moovHeader
     * @param moovBuffer
     * @param sizeAdjustment can be negative or positive     *
     * @param udtaHeader
     * @param metaHeader
     * @return
     * @throws java.io.IOException
     */
    private void adjustSizeOfMoovHeader(Mp4BoxHeader moovHeader, ByteBuffer moovBuffer, int sizeAdjustment, Mp4BoxHeader udtaHeader, Mp4BoxHeader metaHeader)
    {
        //Adjust moov header size, adjusts the underlying buffer
        moovHeader.setLength(moovHeader.getLength() + sizeAdjustment);

        //Edit the fields in moovBuffer (note moovbuffer doesnt include header)
        if (udtaHeader != null)
        {
            //Write the updated udta atom header to moov buffer
            udtaHeader.setLength(udtaHeader.getLength() + sizeAdjustment);
            moovBuffer.position((int) (udtaHeader.getFilePos() - moovHeader.getFilePos() - Mp4BoxHeader.HEADER_LENGTH));
            moovBuffer.put(udtaHeader.getHeaderData());
        }

        if (metaHeader != null)
        {
            //Write the updated udta atom header to moov buffer
            metaHeader.setLength(metaHeader.getLength() + sizeAdjustment);
            moovBuffer.position((int) (metaHeader.getFilePos() - moovHeader.getFilePos() - Mp4BoxHeader.HEADER_LENGTH));
            moovBuffer.put(metaHeader.getHeaderData());
        }
    }


    /**
     * Existing metadata larger than new metadata, so we can usually replace metadata and add/modify free atom.
     *
     * @param fc
     * @param moovHeader
     * @param udtaHeader
     * @param metaHeader
     * @param ilstHeader
     * @param mdatHeader
     * @param neroTagsHeader
     * @param moovBuffer
     * @param newIlstData
     * @param stcos
     * @param sizeOfExistingMetaLevelFreeAtom
     * @throws IOException
     * @throws CannotWriteException
     */
    private void writeOldMetadataLargerThanNewMetadata(SeekableByteChannel fc,  Mp4BoxHeader moovHeader, Mp4BoxHeader udtaHeader, Mp4BoxHeader metaHeader, Mp4BoxHeader ilstHeader, Mp4BoxHeader mdatHeader, Mp4BoxHeader neroTagsHeader, ByteBuffer moovBuffer, ByteBuffer newIlstData, List<Mp4StcoBox> stcos, int sizeOfExistingMetaLevelFreeAtom) throws IOException
    {
        logger.config("Writing:Option 1:Smaller Size");

        int ilstPositionRelativeToAfterMoovHeader = (int) (ilstHeader.getFilePos() - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH));
        //Create an amended freeBaos atom and write it if it previously existed as a free atom immediately
        //after ilst as a child of meta

        int sizeOfNewIlstAtom = newIlstData.limit();
        if (sizeOfExistingMetaLevelFreeAtom > 0)
        {
            logger.config("Writing:Option 2:Smaller Size have free atom:" + ilstHeader.getLength() + ":" + sizeOfNewIlstAtom);
            fc.position(ilstHeader.getFilePos());
            fc.write(newIlstData);

            //Write the modified free atom that comes after ilst
            //New ilst + new free should put at same position as Old Ilst + old free so nothing else to do
            int newFreeSize = sizeOfExistingMetaLevelFreeAtom + (ilstHeader.getLength() - sizeOfNewIlstAtom);
            Mp4FreeBox newFreeBox = new Mp4FreeBox(newFreeSize - Mp4BoxHeader.HEADER_LENGTH);
            fc.write(newFreeBox.getHeader().getHeaderData());
            fc.write(newFreeBox.getData());
        }
        //No free atom we need to create a new one or adjust top level free atom
        else
        {
            //We need to create a new one, so dont have to adjust all the headers but only works if the size
            //of tags has decreased by more 8 characters so there is enough room for the free boxes header we take
            //into account size of new header in calculating size of box
            int newFreeSize = (ilstHeader.getLength() - sizeOfNewIlstAtom) - Mp4BoxHeader.HEADER_LENGTH;
            if (newFreeSize > 0)
            {
                logger.config("Writing:Option 3:Smaller Size can create free atom");
                fc.position(ilstHeader.getFilePos());
                fc.write(newIlstData);

                //Create new free box
                //New ilst + new free should put at same postion as Old Ilst
                Mp4FreeBox newFreeBox = new Mp4FreeBox(newFreeSize);
                fc.write(newFreeBox.getHeader().getHeaderData());
                fc.write(newFreeBox.getData());
            }
            //Everything in this bit of tree has to be recalculated because data is only eight or less bytes smaller
            //so cannot be accommodated by creating a free atom
            else
            {
                logger.config("Writing:Option 4:Smaller Size <=8 cannot create free atoms");

                //This is where Moov atom currently ends (need for later)
                long endOfOriginalMoovAtom = moovHeader.getFileEndPos();

                //Size of new metadata will be this amount smaller
                int sizeReducedBy = ilstHeader.getLength() - sizeOfNewIlstAtom;

                //Edit stcos atoms within moov header, we need to adjust offsets by the amount mdat is going to be shifted
                //unless mdat is at start of file
                if (mdatHeader.getFilePos() > moovHeader.getFilePos())
                {
                    for (final Mp4StcoBox stoc : stcos) {
                        stoc.adjustOffsets(-sizeReducedBy);
                    }
                }

                //Edit and rewrite the moov, udta and meta header in moov buffer
                adjustSizeOfMoovHeader(moovHeader, moovBuffer, -sizeReducedBy, udtaHeader, metaHeader);

                //Write modified MoovHeader
                fc.position(moovHeader.getFilePos());
                fc.write(moovHeader.getHeaderData());

                //Write modified MoovBuffer upto start of ilst data
                moovBuffer.rewind();
                moovBuffer.limit(ilstPositionRelativeToAfterMoovHeader);
                fc.write(moovBuffer);

                //Write new ilst data
                fc.write(newIlstData);

                //Write rest of moov after the old ilst data, as we may have adjusted stcos atoms that occur after ilst
                moovBuffer.limit(moovBuffer.capacity());
                moovBuffer.position(ilstPositionRelativeToAfterMoovHeader + ilstHeader.getLength());
                fc.write(moovBuffer);

                //Delete the previous sizeReducedBy bytes from endOfOriginalMovAtom
                shiftData(fc, endOfOriginalMoovAtom, Math.abs(sizeReducedBy));
            }
        }
    }

    /**
     * Delete deleteSize from startDeleteFrom, shifting down the data that comes after
     *
     * @param fc
     * @param startDeleteFrom
     * @param deleteSize
     * @throws IOException
     */
    private void shiftData(final SeekableByteChannel fc, long startDeleteFrom, final int deleteSize) throws IOException
    {
        //Position for reading after the tag
        fc.position(startDeleteFrom);

        final ByteBuffer buffer = ByteBuffer.allocate((int) TagOptionSingleton.getInstance().getWriteChunkSize());
        while (fc.read(buffer) >= 0 || buffer.position() != 0)
        {
            buffer.flip();
            final long readPosition = fc.position();
            fc.position(readPosition - deleteSize - buffer.limit());
            fc.write(buffer);
            fc.position(readPosition);
            buffer.compact();
        }
        //Truncate the file after the last chunk
        final long newLength = fc.size() - deleteSize;
        logger.config(loggingName + "-------------Setting new length to:" + newLength);
        fc.truncate(newLength);
    }
    /**
     * We can fit the metadata in under the meta item just by using some of the padding available in the {@code free}
     * atom under the {@code meta} atom
     *
     * @param fc
     * @param sizeOfExistingMetaLevelFreeAtom
     * @param newIlstData
     * @param additionalSpaceRequiredForMetadata
     * @throws IOException
     * @throws CannotWriteException
     */
    private void writeNewMetadataLargerButCanUseFreeAtom(SeekableByteChannel fc, Mp4BoxHeader ilstHeader, int sizeOfExistingMetaLevelFreeAtom, ByteBuffer newIlstData, int additionalSpaceRequiredForMetadata) throws IOException, CannotWriteException
    {
        //Shrink existing free atom size
        int newFreeSize = sizeOfExistingMetaLevelFreeAtom - additionalSpaceRequiredForMetadata;

        logger.config("Writing:Option 5;Larger Size can use meta free atom need extra:" + newFreeSize + "bytes");
        fc.position(ilstHeader.getFilePos());
        fc.write(newIlstData);

        //Create an amended smaller freeBaos atom and write it to file
        Mp4FreeBox newFreeBox = new Mp4FreeBox(newFreeSize - Mp4BoxHeader.HEADER_LENGTH);
        fc.write(newFreeBox.getHeader().getHeaderData());
        fc.write(newFreeBox.getData());
    }

    /**
     * Write tag to file.
     *
     * @param tag     tag data
     * @param file     current file
     * @throws CannotWriteException
     * @throws IOException
     */
    public void write(Tag tag, Path file) throws CannotWriteException
    {
        logger.config("Started writing tag data");
        try(SeekableByteChannel fc = Files.newByteChannel(file, StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            int sizeOfExistingIlstAtom = 0;
            int sizeRequiredByNewIlstAtom;
            int positionOfNewIlstAtomRelativeToMoovAtom;
            int positionOfStartOfIlstAtomInMoovBuffer;
            int sizeOfExistingMetaLevelFreeAtom;
            int positionOfTopLevelFreeAtom;
            int sizeOfExistingTopLevelFreeAtom;
            //Found top level free atom that comes after moov and before mdat, (also true if no free atom ?)
            boolean topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata;
            Mp4BoxHeader topLevelFreeHeader;
            Mp4AtomTree atomTree;

            //Build AtomTree based on existing metadata
            try
            {
                atomTree = new Mp4AtomTree(fc, false);
            }
            catch (CannotReadException cre)
            {
                throw new CannotWriteException(cre.getMessage());
            }

            Mp4BoxHeader mdatHeader = atomTree.getBoxHeader(atomTree.getMdatNode());
            //Unable to find audio so no chance of saving any changes
            if (mdatHeader == null)
            {
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_CANNOT_FIND_AUDIO.getMsg());
            }

            //Go through every field constructing the data that will appear starting from ilst box
            ByteBuffer newIlstData = tc.convertMetadata(tag);
            newIlstData.rewind();
            sizeRequiredByNewIlstAtom = newIlstData.limit();

            //Moov Box header
            Mp4BoxHeader moovHeader = atomTree.getBoxHeader(atomTree.getMoovNode());
            List<Mp4StcoBox> stcos = atomTree.getStcos();
            Mp4BoxHeader ilstHeader = atomTree.getBoxHeader(atomTree.getIlstNode());
            Mp4BoxHeader udtaHeader = atomTree.getBoxHeader(atomTree.getUdtaNode());
            Mp4BoxHeader metaHeader = atomTree.getBoxHeader(atomTree.getMetaNode());
            Mp4BoxHeader hdlrMetaHeader = atomTree.getBoxHeader(atomTree.getHdlrWithinMetaNode());
            Mp4BoxHeader neroTagsHeader = atomTree.getBoxHeader(atomTree.getTagsNode());
            Mp4BoxHeader trakHeader = atomTree.getBoxHeader(atomTree.getTrakNodes().get(atomTree.getTrakNodes().size() - 1));
            ByteBuffer moovBuffer = atomTree.getMoovBuffer();


            //Work out if we/what kind of metadata hierarchy we currently have in the file
            //Udta
            if (udtaHeader != null)
            {
                //Meta
                if (metaHeader != null)
                {
                    //ilst - record where ilst is,and where it ends
                    if (ilstHeader != null)
                    {
                        sizeOfExistingIlstAtom = ilstHeader.getLength();

                        //Relative means relative to moov buffer after moov header
                        positionOfStartOfIlstAtomInMoovBuffer = (int) ilstHeader.getFilePos();
                        positionOfNewIlstAtomRelativeToMoovAtom = (int) (positionOfStartOfIlstAtomInMoovBuffer - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH));
                    }
                    else
                    {
                        //Place ilst immediately after existing hdlr atom
                        if (hdlrMetaHeader != null)
                        {
                            positionOfStartOfIlstAtomInMoovBuffer = (int) hdlrMetaHeader.getFileEndPos();
                            positionOfNewIlstAtomRelativeToMoovAtom = (int) (positionOfStartOfIlstAtomInMoovBuffer - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH));
                        }
                        //Place ilst after data fields in meta atom
                        //TODO Should we create a hdlr atom
                        else
                        {
                            positionOfStartOfIlstAtomInMoovBuffer = (int) metaHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH + Mp4MetaBox.FLAGS_LENGTH;
                            positionOfNewIlstAtomRelativeToMoovAtom = (int) ((positionOfStartOfIlstAtomInMoovBuffer) - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH));
                        }
                    }
                }
                else
                {
                    //There no ilst or meta header so we set to position where it would be if it existed
                    positionOfNewIlstAtomRelativeToMoovAtom = moovHeader.getLength() - Mp4BoxHeader.HEADER_LENGTH;
                    positionOfStartOfIlstAtomInMoovBuffer = (int) (moovHeader.getFileEndPos());
                }
            }
            //There no udta header so we are going to create a new structure, but we have to be aware that there might be
            //an existing meta box structure in which case we preserve it but with our new structure before it.
            else
            {
                //Create new structure just after the end of the last trak atom, as that means
                // all modifications to trak atoms and its children (stco atoms) are *explicitly* written
                // as part of the moov atom (and not just bulk copied via writeDataAfterIlst())
                if (metaHeader != null)
                {
                    positionOfStartOfIlstAtomInMoovBuffer = (int) trakHeader.getFileEndPos();
                    positionOfNewIlstAtomRelativeToMoovAtom = (int) (positionOfStartOfIlstAtomInMoovBuffer - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH));
                }
                else
                {
                    //There no udta,ilst or meta header so we set to position where it would be if it existed
                    positionOfStartOfIlstAtomInMoovBuffer = (int) (moovHeader.getFileEndPos());
                    positionOfNewIlstAtomRelativeToMoovAtom = moovHeader.getLength() - Mp4BoxHeader.HEADER_LENGTH;
                }
            }

            //Find size of Level-4 Free atom (if any) immediately after ilst atom
            sizeOfExistingMetaLevelFreeAtom = getMetaLevelFreeAtomSize(atomTree);


            //Level-1 free atom
            positionOfTopLevelFreeAtom = 0;
            sizeOfExistingTopLevelFreeAtom = 0;
            topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata = true;
            for (DefaultMutableTreeNode freeNode : atomTree.getFreeNodes())
            {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) freeNode.getParent();
                if (parentNode.isRoot())
                {
                    topLevelFreeHeader = ((Mp4BoxHeader) freeNode.getUserObject());
                    sizeOfExistingTopLevelFreeAtom = topLevelFreeHeader.getLength();
                    positionOfTopLevelFreeAtom = (int) topLevelFreeHeader.getFilePos();
                    break;
                }
            }

            if (sizeOfExistingTopLevelFreeAtom > 0)
            {
                if (positionOfTopLevelFreeAtom > mdatHeader.getFilePos())
                {
                    topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata = false;
                }
                else if (positionOfTopLevelFreeAtom < moovHeader.getFilePos())
                {
                    topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata = false;
                }
            }
            else
            {
                positionOfTopLevelFreeAtom = (int) mdatHeader.getFilePos();
            }

            logger.config("Read header successfully ready for writing");
            //The easiest option since no difference in the size of the metadata so all we have to do is
            //replace the ilst atom (and children)
            if (sizeOfExistingIlstAtom == sizeRequiredByNewIlstAtom)
            {
                writeMetadataSameSize(fc, ilstHeader, newIlstData);
            }
            //.. we just need to increase the size of the free atom below the meta atom, and replace the metadata
            //no other changes necessary and total file size remains the same
            else if (sizeOfExistingIlstAtom > sizeRequiredByNewIlstAtom)
            {
                writeOldMetadataLargerThanNewMetadata(
                        fc,
                        moovHeader,
                        udtaHeader,
                        metaHeader,
                        ilstHeader,
                        mdatHeader,
                        neroTagsHeader,
                        moovBuffer,
                        newIlstData,
                        stcos,
                        sizeOfExistingMetaLevelFreeAtom);
            }
            //Size of metadata has increased, the most complex situation, more atoms affected
            else
            {
                //We have enough space in existing meta level free atom
                int additionalSpaceRequiredForMetadata = sizeRequiredByNewIlstAtom - sizeOfExistingIlstAtom;
                if (additionalSpaceRequiredForMetadata <= (sizeOfExistingMetaLevelFreeAtom - Mp4BoxHeader.HEADER_LENGTH))
                {
                    writeNewMetadataLargerButCanUseFreeAtom(
                            fc,
                            ilstHeader,
                            sizeOfExistingMetaLevelFreeAtom,
                            newIlstData,
                            additionalSpaceRequiredForMetadata);
                }
                //There is not enough padding in the metadata free atom
                else
                {
                    int additionalMetaSizeThatWontFitWithinMetaAtom = additionalSpaceRequiredForMetadata - sizeOfExistingMetaLevelFreeAtom;

                    //Go up to position of start of Moov Header
                    fc.position(moovHeader.getFilePos());

                    //No existing Metadata
                    if (udtaHeader == null)
                    {
                        writeNoExistingUdtaAtom(
                                fc,
                                newIlstData,
                                moovHeader,
                                moovBuffer,
                                mdatHeader,
                                stcos,
                                sizeOfExistingTopLevelFreeAtom,
                                topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                neroTagsHeader
                        );
                    }
                    else if (metaHeader == null)
                    {
                        writeNoExistingMetaAtom(
                                udtaHeader,
                                fc,
                                newIlstData,
                                moovHeader,
                                moovBuffer,
                                mdatHeader,
                                stcos,
                                sizeOfExistingTopLevelFreeAtom,
                                topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                neroTagsHeader,
                                positionOfStartOfIlstAtomInMoovBuffer,
                                sizeOfExistingIlstAtom,
                                positionOfTopLevelFreeAtom,
                                additionalMetaSizeThatWontFitWithinMetaAtom);
                    }
                    //Has Existing Metadata
                    else
                    {
                        writeHaveExistingMetadata(udtaHeader,
                                metaHeader,
                                fc,
                                positionOfNewIlstAtomRelativeToMoovAtom,
                                moovHeader,
                                moovBuffer,
                                mdatHeader,
                                stcos,
                                sizeOfExistingTopLevelFreeAtom,
                                topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                newIlstData,
                                neroTagsHeader,
                                sizeOfExistingIlstAtom);
                    }
                }
            }
            //Ensure we have written correctly, reject if not
            checkFileWrittenCorrectly(mdatHeader, fc, stcos);
        }
        catch(IOException ioe)
        {
            throw new CannotWriteException(file + ":" + ioe.getMessage());
        }

    }


    /**
     * Replace tags atom (and children) by a {@code free} atom.
     *
     * @param fc
     * @param tagsHeader
     * @throws IOException
     */
    private void convertandWriteTagsAtomToFreeAtom(SeekableByteChannel fc, Mp4BoxHeader tagsHeader) throws IOException
    {
        Mp4FreeBox freeBox = new Mp4FreeBox(tagsHeader.getDataLength());
        fc.write(freeBox.getHeader().getHeaderData());
        fc.write(freeBox.getData());
    }

    /**
     * Determine the size of the {@code free} atom immediately after {@code ilst} atom at the same level (if any),
     * we can use this if {@code ilst} needs to grow or shrink because of more less metadata.
     *
     * @param atomTree
     * @return
     */
    private int getMetaLevelFreeAtomSize(Mp4AtomTree atomTree)
    {
        int oldMetaLevelFreeAtomSize;//Level 4 - Free
        oldMetaLevelFreeAtomSize = 0;

        for (DefaultMutableTreeNode freeNode : atomTree.getFreeNodes())
        {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) freeNode.getParent();
            DefaultMutableTreeNode brotherNode = freeNode.getPreviousSibling();
            if (!parentNode.isRoot())
            {
                Mp4BoxHeader parentHeader = ((Mp4BoxHeader) parentNode.getUserObject());
                Mp4BoxHeader freeHeader = ((Mp4BoxHeader) freeNode.getUserObject());

                //We are only interested in free atoms at this level if they come after the ilst node
                if (brotherNode != null)
                {
                    Mp4BoxHeader brotherHeader = ((Mp4BoxHeader) brotherNode.getUserObject());

                    if (parentHeader.getId().equals(Mp4AtomIdentifier.META.getFieldName()) && brotherHeader.getId().equals(Mp4AtomIdentifier.ILST.getFieldName()))
                    {
                        oldMetaLevelFreeAtomSize = freeHeader.getLength();
                        break;
                    }
                }
            }
        }
        return oldMetaLevelFreeAtomSize;
    }

    /**
     * Check file written correctly.
     *
     * @param mdatHeader
     * @param fc
     * @param stcos
     * @throws CannotWriteException
     * @throws IOException
     */
    private void checkFileWrittenCorrectly(Mp4BoxHeader mdatHeader, SeekableByteChannel fc, List<Mp4StcoBox> stcos) throws CannotWriteException, IOException
    {

        logger.config("Checking file has been written correctly");

        try
        {
            //Create a tree from the new file
            Mp4AtomTree newAtomTree;
            newAtomTree = new Mp4AtomTree(fc, false);

            //Check we still have audio data file, and check length
            Mp4BoxHeader newMdatHeader = newAtomTree.getBoxHeader(newAtomTree.getMdatNode());
            if (newMdatHeader == null)
            {
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_NO_DATA.getMsg());
            }
            if (newMdatHeader.getLength() != mdatHeader.getLength())
            {
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_DATA_CORRUPT.getMsg());
            }

            //Should always have udta atom after writing to file
            Mp4BoxHeader newUdtaHeader = newAtomTree.getBoxHeader(newAtomTree.getUdtaNode());
            if (newUdtaHeader == null)
            {
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_NO_TAG_DATA.getMsg());
            }

            //Should always have meta atom after writing to file
            Mp4BoxHeader newMetaHeader = newAtomTree.getBoxHeader(newAtomTree.getMetaNode());
            if (newMetaHeader == null)
            {
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_NO_TAG_DATA.getMsg());
            }

            // Check that we at the very least have the same number of chunk offsets
            final List<Mp4StcoBox> newStcos = newAtomTree.getStcos();
            if (newStcos.size() != stcos.size())
            {
                // at the very least, we have to have the same number of 'stco' atoms
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_INCORRECT_NUMBER_OF_TRACKS.getMsg(stcos.size(), newStcos.size()));
            }
            //Check offsets are correct, may not match exactly in original file so just want to make
            //sure that the discrepancy if any is preserved

            // compare the first new stco offset with mdat,
            // and ensure that all following ones have a constant shift

            int shift = 0;
            for (int i=0; i<newStcos.size(); i++)
            {
                final Mp4StcoBox newStco = newStcos.get(i);
                final Mp4StcoBox stco = stcos.get(i);
                logger.finer("stco:Original First Offset" + stco.getFirstOffSet());
                logger.finer("stco:Original Diff" + (int) (stco.getFirstOffSet() - mdatHeader.getFilePos()));
                logger.finer("stco:Original Mdat Pos" + mdatHeader.getFilePos());
                logger.finer("stco:New First Offset" + newStco.getFirstOffSet());
                logger.finer("stco:New Diff" + (int) ((newStco.getFirstOffSet() - newMdatHeader.getFilePos())));
                logger.finer("stco:New Mdat Pos" + newMdatHeader.getFilePos());

                if (i == 0)
                {
                    final int diff = (int) (stco.getFirstOffSet() - mdatHeader.getFilePos());
                    if ((newStco.getFirstOffSet() - newMdatHeader.getFilePos()) != diff)
                    {
                        int discrepancy = (int) ((newStco.getFirstOffSet() - newMdatHeader.getFilePos()) - diff);
                        throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_INCORRECT_OFFSETS.getMsg(discrepancy));
                    }
                    shift = stco.getFirstOffSet() - newStco.getFirstOffSet();
                }
                else {
                    if (shift != stco.getFirstOffSet() - newStco.getFirstOffSet())
                    {
                        throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED_INCORRECT_OFFSETS.getMsg(shift));
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (e instanceof CannotWriteException)
            {
                throw (CannotWriteException) e;
            }
            else
            {
                e.printStackTrace();
                throw new CannotWriteException(ErrorMessage.MP4_CHANGES_TO_FILE_FAILED.getMsg() + ":" + e.getMessage());
            }
        }
        finally
        {
            //Close references to new file
            fc.close();
        }
        logger.config("File has been written correctly");
    }

    /**
     * Delete the tag.
     * <p/>
     * <p>This is achieved by writing an empty {@code ilst} atom.
     *
     * @param file
     * @throws IOException
     */
    public void delete(Tag tag, Path file) throws CannotWriteException
    {
        tag = new Mp4Tag();
        write(tag, file);
    }

    /**
     * Use when we need to write metadata and there is no existing {@code udta} atom so we keepp the existing moov data
     * but have to ajdjust the moov header lengths and then create the complete udta/metadata structure and add to the
     * end.
     *
     * If we can fit the new metadata into top level free atom we just shrink that accordingly
     *
     * If we cant then we leave it alone and just shift all the data down aftet the moov (i.e top level free and mdat)
     *
     * @param fc
     * @param newIlstData
     * @param moovHeader
     * @param moovBuffer
     * @param mdatHeader
     * @param stcos
     * @param sizeOfExistingTopLevelFreeAtom
     * @param topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata
     * @throws IOException
     * @throws CannotWriteException
     */
    private void writeNoExistingUdtaAtom(
            SeekableByteChannel fc,
            ByteBuffer newIlstData,
            Mp4BoxHeader moovHeader,
            ByteBuffer moovBuffer,
            Mp4BoxHeader mdatHeader,
            List<Mp4StcoBox> stcos,
            int sizeOfExistingTopLevelFreeAtom,
            boolean topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
            Mp4BoxHeader neroTagsHeader)
            throws IOException

    {
        long endOfOriginalMoovAtom          = moovHeader.getFileEndPos();

        Mp4HdlrBox hdlrBox = Mp4HdlrBox.createiTunesStyleHdlrBox();
        Mp4MetaBox metaBox = Mp4MetaBox.createiTunesStyleMetaBox(hdlrBox.getHeader().getLength() + newIlstData.limit());
        Mp4BoxHeader udtaHeader = new Mp4BoxHeader(Mp4AtomIdentifier.UDTA.getFieldName());
        udtaHeader.setLength(Mp4BoxHeader.HEADER_LENGTH + metaBox.getHeader().getLength());

        //If we can fit in top level free atom we dont have to move mdat data
        boolean isMdatDataMoved = adjustStcosIfNoSuitableTopLevelAtom(sizeOfExistingTopLevelFreeAtom, topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata, udtaHeader.getLength(), stcos, moovHeader, mdatHeader);

        //Edit the Moov header to length and rewrite to account for new udta atom
        moovHeader.setLength(moovHeader.getLength() + udtaHeader.getLength());

        //Position to start of Moov Header in File
        fc.position(moovHeader.getFilePos());

        //Write the new Moov Header
        fc.write(moovHeader.getHeaderData());

        //Write the Existing Moov Data
        moovBuffer.rewind();
        fc.write(moovBuffer);

        //TODO what about nero tag ?
        if(!isMdatDataMoved)
        {
            logger.severe("Writing:Option 5.1;No udta atom");

            //Now Write new atoms required for holding metadata under udta/meta/hdlr
            fc.write(udtaHeader.getHeaderData());
            fc.write(metaBox.getHeader().getHeaderData());
            fc.write(metaBox.getData());
            fc.write(hdlrBox.getHeader().getHeaderData());
            fc.write(hdlrBox.getData());

            //Write new ilst data
            fc.write(newIlstData);

            //Shrink the free atom accordingly to accommodate the extra data
            adjustTopLevelFreeAtom(fc, sizeOfExistingTopLevelFreeAtom, udtaHeader.getLength());
        }
        //we need to shift the Mdat data to allow space to write the larger metadata
        else
        {
            logger.severe("Writing:Option 5.2;No udta atom, not enough free space");

            //Position after MoovBuffer in file
            fc.position(endOfOriginalMoovAtom);

            ShiftData.shiftDataByOffsetToMakeSpace(fc, udtaHeader.getLength());

            //Go back to position just after MoovBuffer in file
            fc.position(endOfOriginalMoovAtom);

            //Now Write new atoms required for holding metadata under udta/meta/hdlr
            fc.write(udtaHeader.getHeaderData());
            fc.write(metaBox.getHeader().getHeaderData());
            fc.write(metaBox.getData());
            fc.write(hdlrBox.getHeader().getHeaderData());
            fc.write(hdlrBox.getData());

            //Write new ilst data
            fc.write(newIlstData);
        }
    }

    /**
     * Use when we need to write metadata, we have a {@code udta} atom but there is no existing meta atom so we
     * have to create the complete metadata structure.
     *
     * @param fc
     * @param newIlstData
     * @param moovHeader
     * @param moovBuffer
     * @param mdatHeader
     * @param stcos
     * @param sizeOfExistingTopLevelFreeAtom
     * @param topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata
     * @throws IOException
     * @throws CannotWriteException
     */
    private void writeNoExistingMetaAtom(Mp4BoxHeader udtaHeader,
                                         SeekableByteChannel fc,
                                         ByteBuffer newIlstData,
                                         Mp4BoxHeader moovHeader,
                                         ByteBuffer moovBuffer,
                                         Mp4BoxHeader mdatHeader,
                                         List<Mp4StcoBox> stcos,
                                         int sizeOfExistingTopLevelFreeAtom,
                                         boolean topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                         Mp4BoxHeader neroTagsHeader,
                                         int positionOfStartOfIlstAtomInMoovBuffer,
                                         int existingSizeOfIlstData,
                                         int topLevelFreeSize,
                                         int additionalMetaSizeThatWontFitWithinMetaAtom) throws IOException

    {
        int newIlstDataSize = newIlstData.limit();
        int existingMoovHeaderDataLength = moovHeader.getDataLength();

        long endOfOriginalMoovAtom          = moovHeader.getFileEndPos();

        //Udta didnt have a meta atom but it may have some other data we want to preserve (I think)
        int existingUdtaLength     = udtaHeader.getLength();
        int existingUdtaDataLength = udtaHeader.getDataLength();

        Mp4HdlrBox hdlrBox = Mp4HdlrBox.createiTunesStyleHdlrBox();
        Mp4MetaBox metaBox = Mp4MetaBox.createiTunesStyleMetaBox(hdlrBox.getHeader().getLength() + newIlstDataSize);
        udtaHeader = new Mp4BoxHeader(Mp4AtomIdentifier.UDTA.getFieldName());
        udtaHeader.setLength(Mp4BoxHeader.HEADER_LENGTH + metaBox.getHeader().getLength() + existingUdtaDataLength);

        int increaseInSizeOfUdtaAtom = udtaHeader.getDataLength() - existingUdtaDataLength;

        boolean isMdatDataMoved = adjustStcosIfNoSuitableTopLevelAtom(sizeOfExistingTopLevelFreeAtom, topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata, increaseInSizeOfUdtaAtom, stcos, moovHeader, mdatHeader);

        //Edit and rewrite the Moov header upto start of Udta
        moovHeader.setLength(moovHeader.getLength() + increaseInSizeOfUdtaAtom);

        //Position to start of Moov Header in File
        fc.position(moovHeader.getFilePos());

        //Write the new Moov Header
        fc.write(moovHeader.getHeaderData());

        //Write Moov data upto start of existing udta
        moovBuffer.rewind();
        moovBuffer.limit(existingMoovHeaderDataLength - existingUdtaLength);
        fc.write(moovBuffer);

        //Write new atoms required for holding metadata in iTunes format
        fc.write(udtaHeader.getHeaderData());

        //Write any atoms if they previously existed within udta atom
        if(moovBuffer.position() + Mp4BoxHeader.HEADER_LENGTH < moovBuffer.capacity())
        {
            moovBuffer.limit(moovBuffer.capacity());
            moovBuffer.position(moovBuffer.position() + Mp4BoxHeader.HEADER_LENGTH);
            fc.write(moovBuffer);
        }

        if(!isMdatDataMoved)
        {
            logger.severe("Writing:Option 6.1;No meta atom");
            //Write our newly constructed meta/hdlr headers (required for ilst)
            fc.write(metaBox.getHeader().getHeaderData());
            fc.write(metaBox.getData());
            fc.write(hdlrBox.getHeader().getHeaderData());
            fc.write(hdlrBox.getData());

            //Write new ilst data
            fc.write(newIlstData);

            writeRestOfMoovHeaderAfterNewIlistAndAmendedTopLevelFreeAtom(
                    fc,
                    positionOfStartOfIlstAtomInMoovBuffer,
                    moovHeader,
                    moovBuffer,
                    additionalMetaSizeThatWontFitWithinMetaAtom,
                    topLevelFreeSize,
                    neroTagsHeader,
                    existingSizeOfIlstData
            );
        }
        //we need to shift the Mdat data to allow space to write the larger metadata
        else
        {
            logger.severe("Writing:Option 6.2;No meta atom, not enough free space");

            //Position after MoovBuffer in file
            fc.position(endOfOriginalMoovAtom);

            //Shift the existing data after Moov Atom by the size of the new meta atom (includes ilst under it)
            ShiftData.shiftDataByOffsetToMakeSpace(fc, metaBox.getHeader().getLength());

            //Now Write new ilst data, continuing from the end of the original Moov atom
            fc.position(endOfOriginalMoovAtom);

            //Write our newly constructed meta/hdlr headers (required for ilst)
            fc.write(metaBox.getHeader().getHeaderData());
            fc.write(metaBox.getData());
            fc.write(hdlrBox.getHeader().getHeaderData());
            fc.write(hdlrBox.getData());

            //Write te actual ilst data
            fc.write(newIlstData);
        }
    }

    /**
     * We have existing structure but we need more space then we have available.
     *
     * @param udtaHeader
     * @param fc
     * @param positionOfStartOfIlstAtomInMoovBuffer
     * @param moovHeader
     * @param moovBuffer
     * @param mdatHeader
     * @param stcos
     * @param topLevelFreeSize
     * @param topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata
     * @throws IOException
     * @throws CannotWriteException
     */
    private void  writeHaveExistingMetadata(Mp4BoxHeader udtaHeader,
                                            Mp4BoxHeader metaHeader,
                                            SeekableByteChannel fc,
                                            int positionOfStartOfIlstAtomInMoovBuffer,
                                            Mp4BoxHeader moovHeader,
                                            ByteBuffer moovBuffer,
                                            Mp4BoxHeader mdatHeader,
                                            List<Mp4StcoBox> stcos,
                                            int topLevelFreeSize,
                                            boolean topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                            ByteBuffer newIlstData,
                                            Mp4BoxHeader neroTagsHeader,
                                            int existingSizeOfIlstData)
            throws IOException
    {
        long endOfOriginalMoovAtom          = moovHeader.getFileEndPos();
        int sizeRequiredByNewIlstAtom       = newIlstData.limit();

        //Since we know we cant fit the data into the meta/free atom we dont try to use it, instead we leave it available for future smaller data additions
        //So we just decide if we can fit the extra data into any available toplevel free atom
        int additionalMetaSizeThatWontFitWithinMetaAtom  = sizeRequiredByNewIlstAtom - existingSizeOfIlstData;
        boolean isMdatDataMoved     = adjustStcosIfNoSuitableTopLevelAtom(topLevelFreeSize, topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata, additionalMetaSizeThatWontFitWithinMetaAtom, stcos, moovHeader, mdatHeader);

        //Edit and rewrite the Moov header inc udta and meta headers)
        adjustSizeOfMoovHeader(moovHeader, moovBuffer, additionalMetaSizeThatWontFitWithinMetaAtom, udtaHeader, metaHeader);

        //Position to start of Moov Header in File
        fc.position(moovHeader.getFilePos());

        //Write MoovHeader (with new larger size)
        fc.write(moovHeader.getHeaderData());

        //Now write from updated Moov buffer up until location of start of ilst atom
        //(Moov buffer contains all of Moov except Mov header)
        moovBuffer.rewind();
        moovBuffer.limit(positionOfStartOfIlstAtomInMoovBuffer);
        fc.write(moovBuffer);

        //If the top level free large enough to provide the extra space required then we didnt have to move the mdat
        //data we just write the new ilst data, rest of moov buffer and amended size top level free atom
        if(!isMdatDataMoved)
        {
            logger.severe("Writing:Option 7.1, Increased Data");

            //Write new ilst data
            fc.write(newIlstData);

            writeRestOfMoovHeaderAfterNewIlistAndAmendedTopLevelFreeAtom(
                    fc,
                    positionOfStartOfIlstAtomInMoovBuffer,
                    moovHeader,
                    moovBuffer,
                    additionalMetaSizeThatWontFitWithinMetaAtom,
                    topLevelFreeSize,
                    neroTagsHeader,
                    existingSizeOfIlstData
            );
        }
        //we need to shift the Mdat data to allow space to write the larger metadata
        else
        {
            logger.severe("Writing:Option 7.2 Increased Data, not enough free space");

            //Position after MoovBuffer in file
            fc.position(endOfOriginalMoovAtom);

            //Shift the existing data after Moov Atom by the increased size of ilst data
            ShiftData.shiftDataByOffsetToMakeSpace(fc, additionalMetaSizeThatWontFitWithinMetaAtom);

            //Now Write new ilst data, starting at the same location as the oldiLst atom
            fc.position(moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH + positionOfStartOfIlstAtomInMoovBuffer);
            fc.write(newIlstData);

            //Now Write any data that existed in MoovHeader after the old ilst atom (if any)
            moovBuffer.limit(moovBuffer.capacity());
            moovBuffer.position(positionOfStartOfIlstAtomInMoovBuffer + existingSizeOfIlstData);
            if(moovBuffer.position() < moovBuffer.capacity())
            {
                fc.write(moovBuffer);
            }
        }
    }

    private void writeRestOfMoovHeaderAfterNewIlistAndAmendedTopLevelFreeAtom(
            SeekableByteChannel fc,
            int positionOfStartOfIlstAtomInMoovBuffer,
            Mp4BoxHeader moovHeader,
            ByteBuffer moovBuffer,
            int additionalMetaSizeThatWontFitWithinMetaAtom,
            int topLevelFreeSize,
            Mp4BoxHeader neroTagsHeader,
            int existingSizeOfIlstData
    ) throws IOException
    {
        //Write the remainder of any data in the moov buffer thats comes after existing ilst/metadata level free atoms
        //but we replace any neroTags atoms with free atoms as these cause problems
        if (neroTagsHeader != null)
        {
            moovBuffer.limit(moovBuffer.capacity());
            moovBuffer.position(positionOfStartOfIlstAtomInMoovBuffer + existingSizeOfIlstData);
            writeFromEndOfIlstToNeroTagsAndMakeNeroFree(moovHeader, moovBuffer, fc, neroTagsHeader);

            //Shrink the top level free atom to accomodate the extra data
            adjustTopLevelFreeAtom(fc, topLevelFreeSize, additionalMetaSizeThatWontFitWithinMetaAtom);
        }
        else
        {
            //Write the remaining children under moov that come after ilst atom
            moovBuffer.limit(moovBuffer.capacity());
            moovBuffer.position(positionOfStartOfIlstAtomInMoovBuffer + existingSizeOfIlstData);
            if(moovBuffer.position() < moovBuffer.capacity())
            {
                fc.write(moovBuffer);
            }

            //Shrink the top level free atom to accommodate the extra data
            adjustTopLevelFreeAtom(fc, topLevelFreeSize, additionalMetaSizeThatWontFitWithinMetaAtom);
        }
    }

    /**
     * If any data between existing {@code ilst} atom and {@code tags} atom write it to new file, then convertMetadata
     * {@code tags} atom to a {@code free} atom.
     *
     * @param fc
     * @param neroTagsHeader
     * @throws IOException
     */
    private void writeFromEndOfIlstToNeroTagsAndMakeNeroFree(Mp4BoxHeader moovHeader, ByteBuffer moovBuffer, SeekableByteChannel fc, Mp4BoxHeader neroTagsHeader)
            throws IOException
    {
        //Write from after ilst (already in position) upto start of tags atom
        //And write from there to the start of the (nero) tags atom
        moovBuffer.limit((int)(neroTagsHeader.getFilePos() - (moovHeader.getFilePos() + Mp4BoxHeader.HEADER_LENGTH)));
        fc.write(moovBuffer);

        //Now write a free atom to replace the nero atom
        convertandWriteTagsAtomToFreeAtom(fc, neroTagsHeader);
    }

    /**
     * We adjust {@code free} top level atom, allowing us to not need to move {@code mdat} atom.
     *
     * @param fc
     * @param sizeOfExistingTopLevelAtom
     * @param additionalMetaSizeThatWontFitWithinMetaAtom
     * @throws IOException
     * @throws CannotWriteException
     */
    private void adjustTopLevelFreeAtom(SeekableByteChannel fc, int sizeOfExistingTopLevelAtom, int additionalMetaSizeThatWontFitWithinMetaAtom)
            throws IOException
    {
        //If the shift is less than the space available in this second free atom data size we just
        //shrink the free atom accordingly
        if (sizeOfExistingTopLevelAtom - Mp4BoxHeader.HEADER_LENGTH >= additionalMetaSizeThatWontFitWithinMetaAtom)
        {
            logger.config("Writing:Option 6;Larger Size can use top free atom");
            Mp4FreeBox freeBox = new Mp4FreeBox((sizeOfExistingTopLevelAtom - Mp4BoxHeader.HEADER_LENGTH) - additionalMetaSizeThatWontFitWithinMetaAtom);
            fc.write(freeBox.getHeader().getHeaderData());
            fc.write(freeBox.getData());
        }
        //If the space required is identical to total size of the free space (inc header)
        //we could just remove the header
        else if (sizeOfExistingTopLevelAtom == additionalMetaSizeThatWontFitWithinMetaAtom)
        {
            logger.config("Writing:Option 7;Larger Size uses top free atom including header");
        }
        else
        {
            //MDAT comes before MOOV, nothing to do because data has already been written
        }
    }

    /**
     * May need to rewrite the {@code stco} offsets, if the location of {@code mdat} (audio) header is going to move.
     *
     * @param topLevelFreeSize
     * @param topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata
     * @param additionalSizeRequired
     * @param stcos
     * @param moovHeader
     * @param mdatHeader
     *
     * @return {@code true}, if offsets were adjusted because unable to fit in new
     * metadata without shifting {@code mdat} header further down
     */
    private boolean adjustStcosIfNoSuitableTopLevelAtom(int topLevelFreeSize,
                                                        boolean topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata,
                                                        int additionalSizeRequired,
                                                        List<Mp4StcoBox> stcos,
                                                        Mp4BoxHeader moovHeader,
                                                        Mp4BoxHeader mdatHeader)
    {
        //We don't bother using the top level free atom because not big enough anyway, we need to adjust offsets
        //by the amount mdat is going to be shifted as long as mdat is after moov
        if (mdatHeader.getFilePos() > moovHeader.getFilePos())
        {
            //Edit stco atoms within moov header, if the free atom comes after mdat OR
            //(there is not enough space in the top level free atom
            //or special case (of not matching exactly the free atom plus header so could remove free atom completely)
            if (
                    (!topLevelFreeAtomComesBeforeMdatAtomAndAfterMetadata)
                    ||
                    (
                            (topLevelFreeSize - Mp4BoxHeader.HEADER_LENGTH < additionalSizeRequired)
                            &&
                            (topLevelFreeSize != additionalSizeRequired)
                    )
            )
            {
                for (final Mp4StcoBox stoc : stcos)
                {
                    stoc.adjustOffsets(additionalSizeRequired);
                }
                return true;
            }
        }
        return false;
    }
}