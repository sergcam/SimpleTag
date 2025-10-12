/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can getFields a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jaudiotagger.tag.id3;

import org.jaudiotagger.audio.exceptions.UnableToCreateFileException;
import org.jaudiotagger.audio.exceptions.UnableToModifyFileException;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.logging.FileSystemMessage;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.datatype.Pair;
import org.jaudiotagger.tag.id3.framebody.*;
import org.jaudiotagger.tag.id3.valuepair.ID3NumberTotalFields;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.reference.Languages;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jaudiotagger.utils.ShiftData;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;

/**
 * This is the abstract base class for all ID3v2 tags.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public abstract class AbstractID3v2Tag extends AbstractID3Tag implements Tag
{
    //Start location of this chunk
    //TODO currently only used by ID3 embedded into Wav/Aiff but should be extended to mp3s
    private Long startLocationInFile = null;

    //End location of this chunk
    private Long endLocationInFile = null;

    protected static final String TYPE_HEADER = "header";
    protected static final String TYPE_BODY = "body";

    //Tag ID as held in file
    public static final byte[] TAG_ID = {'I', 'D', '3'};
    public static final String TAGID = "ID3";

    //The tag header is the same for ID3v2 versions
    public static final int TAG_HEADER_LENGTH = 10;
    public static final int FIELD_TAGID_LENGTH = 3;
    public static final int FIELD_TAG_MAJOR_VERSION_LENGTH = 1;
    public static final int FIELD_TAG_MINOR_VERSION_LENGTH = 1;
    public static final int FIELD_TAG_FLAG_LENGTH = 1;
    public static final int FIELD_TAG_SIZE_LENGTH = 4;

    public static final int FIELD_TAGID_POS = 0;
    public static final int FIELD_TAG_MAJOR_VERSION_POS = 3;
    public static final int FIELD_TAG_MINOR_VERSION_POS = 4;
    public static final int FIELD_TAG_FLAG_POS = 5;
    public static final int FIELD_TAG_SIZE_POS = 6;

    protected static final int TAG_SIZE_INCREMENT = 100;

    /**
     * Map of all frames for this tag
     */
    protected Map<String, List<TagField>> frameMap = null;

    /**
     * Map of all encrypted frames, these cannot be unencrypted by jaudiotagger
     */
    protected Map<String, List<TagField>> encryptedFrameMap = null;

    /**
     * Holds the ids of invalid duplicate frames
     */
    protected static final String TYPE_DUPLICATEFRAMEID = "duplicateFrameId";
    protected String duplicateFrameId = "";

    /**
     * Holds count the number of bytes used up by invalid duplicate frames
     */
    protected static final String TYPE_DUPLICATEBYTES = "duplicateBytes";
    protected int duplicateBytes = 0;

    /**
     * Holds count the number bytes used up by empty frames
     */
    protected static final String TYPE_EMPTYFRAMEBYTES = "emptyFrameBytes";
    protected int emptyFrameBytes = 0;

    /**
     * Holds the size of the tag as reported by the tag header
     */
    protected static final String TYPE_FILEREADSIZE = "fileReadSize";
    protected int fileReadSize = 0;

    /**
     * Holds count of invalid frames, (frames that could not be read)
     */
    protected static final String TYPE_INVALIDFRAMES = "invalidFrames";
    protected int invalidFrames = 0;

    /**
     * True if files has a ID3v2 header
     *
     * @param raf
     * @return
     * @throws IOException
     */
    private static boolean isID3V2Header(RandomAccessFile raf) throws IOException
    {
        long start = raf.getFilePointer();
        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        raf.read(tagIdentifier);
        raf.seek(start);
        if (!(Arrays.equals(tagIdentifier, TAG_ID)))
        {
            return false;
        }
        return true;
    }

    private static boolean isID3V2Header(FileChannel fc) throws IOException
    {
        long start = fc.position();
        ByteBuffer headerBuffer = Utils.readFileDataIntoBufferBE(fc, FIELD_TAGID_LENGTH);
        fc.position(start);
        String s = Utils.readThreeBytesAsChars(headerBuffer);
        return s.equals(TAGID);
    }


    /**
     * Determines if file contain an id3 tag and if so positions the file pointer just after the end
     * of the tag.
     * <p/>
     * This method is used by non mp3s (such as .ogg and .flac) to determine if they contain an id3 tag
     *
     * @param raf
     * @return
     * @throws IOException
     */
    public static boolean isId3Tag(RandomAccessFile raf) throws IOException
    {
        if (!isID3V2Header(raf))
        {
            return false;
        }
        //So we have a tag
        byte[] tagHeader = new byte[FIELD_TAG_SIZE_LENGTH];
        raf.seek(raf.getFilePointer() + FIELD_TAGID_LENGTH + FIELD_TAG_MAJOR_VERSION_LENGTH + FIELD_TAG_MINOR_VERSION_LENGTH + FIELD_TAG_FLAG_LENGTH);
        raf.read(tagHeader);
        ByteBuffer bb = ByteBuffer.wrap(tagHeader);

        int size = ID3SyncSafeInteger.bufferToValue(bb);
        raf.seek(size + TAG_HEADER_LENGTH);
        return true;
    }

    /**
     * Is ID3 tag
     *
     * @param fc
     * @return
     * @throws IOException
     */
    public static boolean isId3Tag(FileChannel fc) throws IOException
    {
        if (!isID3V2Header(fc))
        {
            return false;
        }
        //So we have a tag
        ByteBuffer bb = ByteBuffer.allocateDirect(FIELD_TAG_SIZE_LENGTH);
        fc.position(fc.position() + FIELD_TAGID_LENGTH + FIELD_TAG_MAJOR_VERSION_LENGTH + FIELD_TAG_MINOR_VERSION_LENGTH + FIELD_TAG_FLAG_LENGTH);
        fc.read(bb);
        bb.flip();
        int size = ID3SyncSafeInteger.bufferToValue(bb);
        fc.position(size + TAG_HEADER_LENGTH);
        return true;
    }

    /**
     * Empty Constructor
     */
    public AbstractID3v2Tag()
    {
    }

    /**
     * This constructor is used when a tag is created as a duplicate of another
     * tag of the same type and version.
     *
     * @param copyObject
     */
    protected AbstractID3v2Tag(AbstractID3v2Tag copyObject)
    {
    }

    /**
     * Copy primitives apply to all tags
     *
     * @param copyObject
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObject)
    {
        logger.config("Copying Primitives");
        //Primitives type variables common to all IDv2 Tags
        this.duplicateFrameId = copyObject.duplicateFrameId;
        this.duplicateBytes = copyObject.duplicateBytes;
        this.emptyFrameBytes = copyObject.emptyFrameBytes;
        this.fileReadSize = copyObject.fileReadSize;
        this.invalidFrames = copyObject.invalidFrames;
    }

    /**
     * Copy frames from another tag,
     *
     * @param copyObject
     */
    //TODO Copy Encrypted frames needs implementing
    protected void copyFrames(AbstractID3v2Tag copyObject)
    {
        frameMap = new LinkedHashMap<>();
        encryptedFrameMap = new LinkedHashMap<>();

        //Copy Frames that are a valid 2.4 type
        for (String id : copyObject.frameMap.keySet())
        {
            List<TagField> fields = copyObject.frameMap.get(id);
            for(TagField o : fields) {
	            if (o instanceof AbstractID3v2Frame)
	            {
	                addFrame((AbstractID3v2Frame) o);
	            }
	            else if (o instanceof TyerTdatAggregatedFrame)
	            {
	                for (AbstractID3v2Frame next : ((TyerTdatAggregatedFrame) o).getFrames())
	                {
	                    addFrame(next);
	                }
	            }
            }
        }
    }

    /**
     * Add the frame converted to the correct version
     * @param frame
     */
    protected abstract void addFrame(AbstractID3v2Frame frame);

    /**
     * Convert the frame to the correct frame(s)
     *
     * @param frame
     * @return
     * @throws InvalidFrameException
     */
    protected abstract List<AbstractID3v2Frame> convertFrame(AbstractID3v2Frame frame) throws InvalidFrameException;

    /**
     * Returns the number of bytes which come from duplicate frames
     *
     * @return the number of bytes which come from duplicate frames
     */
    public int getDuplicateBytes()
    {
        return duplicateBytes;
    }

    /**
     * Return the string which holds the ids of all
     * duplicate frames.
     *
     * @return the string which holds the ids of all duplicate frames.
     */
    public String getDuplicateFrameId()
    {
        return duplicateFrameId;
    }

    /**
     * Returns the number of bytes which come from empty frames
     *
     * @return the number of bytes which come from empty frames
     */
    public int getEmptyFrameBytes()
    {
        return emptyFrameBytes;
    }

    /**
     * Return  byte count of invalid frames
     *
     * @return byte count of invalid frames
     */
    public int getInvalidFrames()
    {
        return invalidFrames;
    }

    /**
     * Returns the tag size as reported by the tag header
     *
     * @return the tag size as reported by the tag header
     */
    public int getFileReadBytes()
    {
        return fileReadSize;
    }

    /**
     * Return whether tag has frame with this identifier
     * <p/>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will return true
     *
     * @param identifier frameId to lookup
     * @return true if tag has frame with this identifier
     */
    public boolean hasFrame(String identifier)
    {
        return frameMap.containsKey(identifier);
    }


    /**
     * Return whether tag has frame with this identifier and a related body. This is required to protect
     * against circumstances whereby a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag which it has been converted to
     * <p/>
     * e.g TDRC is an invalid frame in a v23 tag but if somehow a v23tag has been created by another application
     * with a TDRC frame we construct an UnsupportedFrameBody to hold it, then this library constructs a
     * v24 tag, it will contain a frame with id TDRC but it will not have the expected frame body it is not really a
     * TDRC frame.
     *
     * @param identifier frameId to lookup
     * @return true if tag has frame with this identifier
     */
    public boolean hasFrameAndBody(String identifier)
    {
        if (hasFrame(identifier))
        {
            List<TagField> fields = getFrame(identifier);
            if(fields.size()>0)
            {
                TagField frame = fields.get(0);
                if (frame instanceof AbstractID3v2Frame)
                {
                    return !(((AbstractID3v2Frame)frame).getBody() instanceof FrameBodyUnsupported);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Return whether tag has frame starting with this identifier
     * <p/>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will return true
     *
     * @param identifier start of frameId to lookup
     * @return tag has frame starting with this identifier
     */
    public boolean hasFrameOfType(String identifier)
    {
        Iterator<String> iterator = frameMap.keySet().iterator();
        String key;
        boolean found = false;
        while (iterator.hasNext() && !found)
        {
            key = iterator.next();
            if (key.startsWith(identifier))
            {
                found = true;
            }
        }
        return found;
    }


    /**
     * Return a list containing all the frames with this identifier
     * <p/>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will be returned.
     *
     * @param identifier is an ID3Frame identifier
     * @return list of matching frames
     */
    public List<TagField> getFrame(String identifier)
    {
        return frameMap.get(identifier);
    }

    /**
     * Return any encrypted frames with this identifier
     * <p/>
     * <p>For single frames return the frame in this tag with given identifier if it exists, if multiple frames
     * exist with the same identifier it will return a list containing all the frames with this identifier
     *
     * @param identifier
     * @return
     */
    public List<TagField> getEncryptedFrame(String identifier)
    {
        return encryptedFrameMap.get(identifier);
    }

    /**
     * Retrieve the first value that exists for this identifier
     * <p/>
     * If the value is a String it returns that, otherwise returns a summary of the fields information
     *
     * @param identifier
     * @return
     */
    public String getFirst(String identifier)
    {
        AbstractID3v2Frame frame = getFirstField(identifier);
        if (frame == null)
        {
            return "";
        }
        return getTextValueForFrame(frame);
    }

    /**
     * @param frame
     * @return
     */
    private String getTextValueForFrame(AbstractID3v2Frame frame)
    {
        return frame.getBody().getUserFriendlyValue();
    }

    public TagField getFirstField(FieldKey genericKey) throws KeyNotFoundException
    {
        List<TagField> fields = getFields(genericKey);
        if (fields.size() > 0)
        {
            return fields.get(0);
        }
        return null;
    }


    /**
     * Retrieve the first tag field that exists for this identifier
     *
     * @param identifier
     * @return tag field or null if doesn't exist
     */
    public AbstractID3v2Frame getFirstField(String identifier)
    {
        List<TagField> fields = getFrame(identifier);
        if (fields == null || fields.isEmpty() || containsAggregatedFrame(fields))
        {
            return null;
        }
        return (AbstractID3v2Frame) fields.get(0);
    }

    /**
     * Add a frame to this tag
     *
     * @param frame the frame to add
     *              <p/>
     *              <p/>
     *              Warning if frame(s) already exists for this identifier that they are overwritten
     */
    //TODO needs to ensure do not addField an invalid frame for this tag
    //TODO what happens if already contains a list with this ID
    public void setFrame(AbstractID3v2Frame frame)
    {
    	List<TagField> frames = new ArrayList<>();
    	frames.add(frame);
        frameMap.put(frame.getIdentifier(), frames);
    }
    
    protected void setTagField(String id, TagField frame)
    {
    	List<TagField> frames = new ArrayList<>();
    	frames.add(frame);
        frameMap.put(id, frames);
    }

    protected abstract ID3Frames getID3Frames();


    public void setField(FieldKey genericKey, String... values) throws KeyNotFoundException, FieldDataInvalidException
    {
        TagField tagfield = createField(genericKey, values);
        setField(tagfield);
    }

    public void addField(FieldKey genericKey, String... value) throws KeyNotFoundException, FieldDataInvalidException
    {
        TagField tagfield = createField(genericKey, value);
        addField(tagfield);
    }


    /**
     * All Number/Count frames  are treated the same (TCK, TPOS, MVNM)
     *
     * @param newFrame
     * @param nextFrame
     */
    public void mergeNumberTotalFrames(AbstractID3v2Frame newFrame, AbstractID3v2Frame nextFrame)
    {
        AbstractFrameBodyNumberTotal newBody = (AbstractFrameBodyNumberTotal) newFrame.getBody();
        AbstractFrameBodyNumberTotal oldBody = (AbstractFrameBodyNumberTotal) nextFrame.getBody();

        if (newBody.getNumber() != null && newBody.getNumber() > 0)
        {
            oldBody.setNumber(newBody.getNumberAsText());
        }

        if (newBody.getTotal() != null && newBody.getTotal() > 0)
        {
            oldBody.setTotal(newBody.getTotalAsText());
        }
        return;
    }

    /**
     * Add frame taking into account existing frames of the same type
     *
     * @param newFrame
     */
    public void mergeDuplicateFrames(AbstractID3v2Frame newFrame)
    {
    	List<TagField> frames = frameMap.get(newFrame.getId());
    	if(frames == null) {
    		frames = new ArrayList<>();
    	}
        for (ListIterator<TagField> li = frames.listIterator(); li.hasNext(); )
        {
        	TagField tagField = li.next();
        	if(!(tagField instanceof AbstractID3v2Frame)) {
        		// Skip aggregated frames
        		continue;
        	}
            AbstractID3v2Frame nextFrame = (AbstractID3v2Frame)tagField;

            if (newFrame.getBody() instanceof FrameBodyTXXX)
            {
                //Value with matching key exists so replace
                if (((FrameBodyTXXX) newFrame.getBody()).getDescription().equals(((FrameBodyTXXX) nextFrame.getBody()).getDescription()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            else if (newFrame.getBody() instanceof FrameBodyWXXX)
            {
                //Value with matching key exists so replace
                if (((FrameBodyWXXX) newFrame.getBody()).getDescription().equals(((FrameBodyWXXX) nextFrame.getBody()).getDescription()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            else if (newFrame.getBody() instanceof FrameBodyCOMM)
            {
                if (((FrameBodyCOMM) newFrame.getBody()).getDescription().equals(((FrameBodyCOMM) nextFrame.getBody()).getDescription()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            else if (newFrame.getBody() instanceof FrameBodyUFID)
            {
                if (((FrameBodyUFID) newFrame.getBody()).getOwner().equals(((FrameBodyUFID) nextFrame.getBody()).getOwner()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            else if (newFrame.getBody() instanceof FrameBodyUSLT)
            {
                if (((FrameBodyUSLT) newFrame.getBody()).getDescription().equals(((FrameBodyUSLT) nextFrame.getBody()).getDescription()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            else if (newFrame.getBody() instanceof FrameBodyPOPM)
            {
                if (((FrameBodyPOPM) newFrame.getBody()).getEmailToUser().equals(((FrameBodyPOPM) nextFrame.getBody()).getEmailToUser()))
                {
                    li.set(newFrame);
                    frameMap.put(newFrame.getId(), frames);
                    return;
                }
            }
            //e.g TRCK, TPOS, MVNM
            else if (newFrame.getBody() instanceof AbstractFrameBodyNumberTotal)
            {
                mergeNumberTotalFrames(newFrame,nextFrame);
                return;
            }
            //e.g TIPL IPLS, TMCL
            else if (newFrame.getBody() instanceof AbstractFrameBodyPairs)
            {
                AbstractFrameBodyPairs frameBody = (AbstractFrameBodyPairs) newFrame.getBody();
                AbstractFrameBodyPairs existingFrameBody = (AbstractFrameBodyPairs) nextFrame.getBody();
                existingFrameBody.addPair(frameBody.getText());
                return;
            }
        }

        if (!getID3Frames().isMultipleAllowed(newFrame.getId()))
        {
        	setFrame(newFrame);
        }
        else
        {
            //No match found so addField new one
            frames.add(newFrame);
            frameMap.put(newFrame.getId(), frames);
        }
    }

    /**
     * Add another frame to the map
     *
     * @param list
     * @param frameMap
     * @param existingFrame
     * @param frame
     */
    private void addNewFrameToMap(List<TagField> list, Map<String, List<TagField>> frameMap, AbstractID3v2Frame existingFrame, AbstractID3v2Frame frame)
    {
        if (list.size() == 0)
        {
            list.add(existingFrame);
            list.add(frame);
            frameMap.put(frame.getId(), list);
        }
        else
        {
            list.add(frame);
        }
    }

    /**
     * Handles adding of a new field that's shares a frame with other fields, so modifies the existing frame rather
     * than creating a new frame for these special cases
     *
     * @param list
     * @param frameMap
     * @param existingFrame
     * @param frame
     */
    private void addNewFrameOrAddField(List<TagField> list, Map<String, List<TagField>> frameMap, AbstractID3v2Frame existingFrame, AbstractID3v2Frame frame)
    {
        List<TagField> mergedList = new ArrayList<>();
        if (existingFrame != null)
        {
            mergedList.add(existingFrame);
        }
        else
        {
            mergedList.addAll(list);
        }

        /**
         * If the frame is a TXXX frame then we add an extra string to the existing frame
         * if same description otherwise we create a new frame
         */
        if (frame.getBody() instanceof FrameBodyTXXX)
        {
            FrameBodyTXXX frameBody = (FrameBodyTXXX) frame.getBody();
            boolean match = false;
            Iterator<TagField> i = mergedList.listIterator();
            while (i.hasNext())
            {
                FrameBodyTXXX existingFrameBody = (FrameBodyTXXX) ((AbstractID3v2Frame) i.next()).getBody();
                if (frameBody.getDescription().equals(existingFrameBody.getDescription()))
                {
                    existingFrameBody.addTextValue(frameBody.getText());
                    match = true;
                    break;
                }
            }
            if (!match)
            {
                addNewFrameToMap(list, frameMap, existingFrame, frame);
            }
        }
        else if (frame.getBody() instanceof FrameBodyWXXX)
        {
            FrameBodyWXXX frameBody = (FrameBodyWXXX) frame.getBody();
            boolean match = false;
            Iterator<TagField> i = mergedList.listIterator();
            while (i.hasNext())
            {
                FrameBodyWXXX existingFrameBody = (FrameBodyWXXX) ((AbstractID3v2Frame) i.next()).getBody();
                if (frameBody.getDescription().equals(existingFrameBody.getDescription()))
                {
                    existingFrameBody.addUrlLink(frameBody.getUrlLink());
                    match = true;
                    break;
                }
            }
            if (!match)
            {
                addNewFrameToMap(list, frameMap, existingFrame, frame);
            }
        }
        else if (frame.getBody() instanceof AbstractFrameBodyTextInfo)
        {
            AbstractFrameBodyTextInfo frameBody = (AbstractFrameBodyTextInfo) frame.getBody();
            AbstractFrameBodyTextInfo existingFrameBody = (AbstractFrameBodyTextInfo) existingFrame.getBody();
            existingFrameBody.addTextValue(frameBody.getText());
        }
        else if (frame.getBody() instanceof AbstractFrameBodyPairs)
        {
            AbstractFrameBodyPairs frameBody = (AbstractFrameBodyPairs) frame.getBody();
            AbstractFrameBodyPairs existingFrameBody = (AbstractFrameBodyPairs) existingFrame.getBody();
            existingFrameBody.addPair(frameBody.getText());
        }
        else if (frame.getBody() instanceof AbstractFrameBodyNumberTotal)
        {
            AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
            AbstractFrameBodyNumberTotal existingFrameBody = (AbstractFrameBodyNumberTotal) existingFrame.getBody();

            if (frameBody.getNumber() != null && frameBody.getNumber() > 0)
            {
                existingFrameBody.setNumber(frameBody.getNumberAsText());
            }

            if (frameBody.getTotal() != null && frameBody.getTotal() > 0)
            {
                existingFrameBody.setTotal(frameBody.getTotalAsText());
            }
        }
        else
        {
            addNewFrameToMap(list, frameMap, existingFrame, frame);
        }
    }

    /**
     * Set Field
     *
     * @param field
     * @throws FieldDataInvalidException
     */
    public void setField(TagField field) throws FieldDataInvalidException
    {
        if ((!(field instanceof AbstractID3v2Frame)) && (!(field instanceof AggregatedFrame)))
        {
            throw new FieldDataInvalidException("Field " + field + " is not of type AbstractID3v2Frame nor AggregatedFrame");
        }

        if (field instanceof AbstractID3v2Frame)
        {
            mergeDuplicateFrames((AbstractID3v2Frame) field);
        }
        else
        //TODO not handling multiple aggregated frames of same type
        {
        	setTagField(field.getId(), field);
        }
    }


    /**
     * Add new field
     * <p/>
     * There is a special handling if adding another text field of the same type, in this case the value will
     * be appended to the existing field, separated by the null character.
     *
     * @param field
     * @throws FieldDataInvalidException
     */
    public void addField(TagField field) throws FieldDataInvalidException
    {
        if (field == null)
        {
            return;
        }

        if ((!(field instanceof AbstractID3v2Frame)) && (!(field instanceof AggregatedFrame)))
        {
            throw new FieldDataInvalidException("Field " + field + " is not of type AbstractID3v2Frame or AggregatedFrame");
        }

        if (field instanceof AbstractID3v2Frame)
        {
            AbstractID3v2Frame frame = (AbstractID3v2Frame) field;

            List<TagField> fields = frameMap.get(field.getId());

            //No frame of this type
            if (fields == null)
            {
            	fields = new ArrayList<>();
            	fields.add(field);
                frameMap.put(field.getId(), fields);
            }
            else 
			{
				if (fields.size() == 1 && fields.get(0) instanceof AbstractID3v2Frame) 
				{
					addNewFrameOrAddField(fields, frameMap, (AbstractID3v2Frame) fields.get(0), frame);
				} 
				else 
				{
					addNewFrameOrAddField(fields, frameMap, null, frame);
				}
            }
        }
        else
        {
        	setTagField(field.getId(), field);
        }
    }


    /**
     * Used for setting multiple frames for a single frame Identifier
     * <p/>
     * Warning if frame(s) already exists for this identifier they are overwritten
     * TODO needs to ensure do not add an invalid frame for this tag
     *
     * @param identifier
     * @param multiFrame
     */
    public void setFrame(String identifier, List<TagField> multiFrame)
    {
        logger.finest("Adding " + multiFrame.size() + " frames for " + identifier);
        frameMap.put(identifier, multiFrame);
    }

    /**
     * Return the number of frames in this tag of a particular type, multiple frames
     * of the same time will only be counted once
     *
     * @return a count of different frames
     */
    /*
    public int getFrameCount()
    {
        if (frameMap == null)
        {
            return 0;
        }
        else
        {
            return frameMap.size();
        }
    }
    */

    /**
     * Return all frames which start with the identifier, this
     * can be more than one which is useful if trying to retrieve
     * similar frames e.g TIT1,TIT2,TIT3 ... and don't know exactly
     * which ones there are.
     * <p/>
     * Warning the match is only done against the identifier so if a tag contains a frame with an unsupported body
     * but happens to have an identifier that is valid for another version of the tag it will be returned.
     *
     * @param identifier
     * @return an iterator of all the frames starting with a particular identifier
     */
    public Iterator<Object> getFrameOfType(String identifier)
    {
        Iterator<String> iterator = frameMap.keySet().iterator();
        Set<Object> result = new HashSet<>();
        String key;
        while (iterator.hasNext())
        {
            key = iterator.next();
            if (key.startsWith(identifier))
            {
                Object o = frameMap.get(key);
                if (o instanceof List)
                {
                    for (Object next : (List<?>) o)
                    {
                        result.add(next);
                    }
                }
                else
                {
                    result.add(o);
                }
            }
        }
        return result.iterator();
    }


    /**
     * Delete Tag
     *
     * @param file to delete the tag from
     * @throws IOException if problem accessing the file
     */
    //TODO should clear all data and preferably recover lost space and go upto end of mp3s 
    public void delete(RandomAccessFile file) throws IOException
    {
        // this works by just erasing the "ID3" tag at the beginning
        // of the file
        byte[] buffer = new byte[FIELD_TAGID_LENGTH];
        //Read into Byte Buffer
        final FileChannel fc = file.getChannel();
        fc.position();
        ByteBuffer byteBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);
        fc.read(byteBuffer, 0);
        byteBuffer.flip();
        if (seek(byteBuffer))
        {
            file.seek(0L);
            file.write(buffer);
        }
    }

    /**
     * Is this tag equivalent to another
     *
     * @param obj to test for equivalence
     * @return true if they are equivalent
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AbstractID3v2Tag))
        {
            return false;
        }
        AbstractID3v2Tag object = (AbstractID3v2Tag) obj;
        return this.frameMap.equals(object.frameMap) && super.equals(obj);
    }


    /**
     * Return the frames in the order they were added
     *
     * @return and iterator of the frmaes/list of multi value frames
     */
    public Iterator<List<TagField>> iterator()
    {
        return frameMap.values().iterator();
    }

    /**
     * Remove frame(s) with this identifier from tag
     *
     * @param identifier frameId to look for
     */
    public void removeFrame(String identifier)
    {
        logger.config("Removing frame with identifier:" + identifier);
        frameMap.remove(identifier);
    }

    /**
     * Remove all frame(s) which have an unsupported body, in other words
     * remove all frames that are not part of the standard frameSet for
     * this tag
     */
	public void removeUnsupportedFrames() 
	{
		for (Iterator<List<TagField>> fieldsIterator = iterator(); fieldsIterator.hasNext();) 
		{
			List<TagField> fields = fieldsIterator.next();
			Iterator<TagField> i = fields.iterator();
			while (i.hasNext()) 
			{
				TagField o = i.next();
				if (o instanceof AbstractID3v2Frame) 
				{
					if (((AbstractID3v2Frame) o).getBody() instanceof FrameBodyUnsupported) 
					{
						logger.finest("Removing frame" + ((AbstractID3v2Frame) o).getIdentifier());
						i.remove();
					}
				}
			}
			if(fields.isEmpty()) {
				fieldsIterator.remove();
			}
		}
	}

    /**
     * Remove any frames starting with this identifier from tag
     *
     * @param identifier start of frameId to look for
     */
    public void removeFrameOfType(String identifier)
    {
        //First fine matching keys
        Set<String> result = new HashSet<>();
        for (String key : frameMap.keySet())
        {
            if (key.startsWith(identifier))
            {
                result.add(key);
            }
        }
        //Then deleteField outside of loop to prevent concurrent modificatioon eception if there are two keys
        //with the same id
        for (String match : result)
        {
            logger.finest("Removing frame with identifier:" + match + "because starts with:" + identifier);
            frameMap.remove(match);
        }
    }


    /**
     * Write tag to file.
     *
     * @param file
     * @param audioStartByte
     * @return new audioStartByte - different only if the audio content had to be moved
     * @throws IOException
     */
    public abstract long write(File file, long audioStartByte) throws IOException;

    /**
     * Get file lock for writing too file
     * <p/>
     * TODO:this appears to have little effect on Windows Vista
     *
     * @param fileChannel
     * @param filePath
     * @return lock or null if locking is not supported
     * @throws IOException                                    if unable to get lock because already locked by another program
     * @throws java.nio.channels.OverlappingFileLockException if already locked by another thread in the same VM, we dont catch this
     *                                                        because indicates a programming error
     */
    protected FileLock getFileLockForWriting(FileChannel fileChannel, String filePath) throws IOException
    {
        logger.finest("locking fileChannel for " + filePath);
        FileLock fileLock;
        try
        {
            fileLock = fileChannel.tryLock();
        }
        //Assumes locking is not supported on this platform so just returns null
        catch (IOException exception)
        {
            return null;
        }
        //#129 Workaround for https://bugs.openjdk.java.net/browse/JDK-8025619
        catch (Error error)
        {
            return null;
        }

        //Couldnt getFields lock because file is already locked by another application
        if (fileLock == null)
        {
            throw new IOException(ErrorMessage.GENERAL_WRITE_FAILED_FILE_LOCKED.getMsg(filePath));
        }
        return fileLock;
    }

    /**
     * Write tag to file.
     *
     * @param file
     * @throws IOException TODO should be abstract
     */
    public void write(RandomAccessFile file) throws IOException
    {
    }

    /**
     * Write tag to channel.
     *
     * @param channel
     * @throws IOException TODO should be abstract
     */
    public void write(WritableByteChannel channel, int currentTagSize) throws IOException
    {
    }

    /**
     * Write tag to output stream
     *
     * @param outputStream
     * @throws IOException
     */
    public void write(OutputStream outputStream) throws IOException
    {
        write(Channels.newChannel(outputStream), 0);
    }

    /**
     * Write tag to output stream
     *
     * @param outputStream
     * @throws IOException
     */
    public void write(OutputStream outputStream, int currentTagSize) throws IOException
    {
        write(Channels.newChannel(outputStream), currentTagSize);
    }


    /**
     * Write paddings byte to the channel
     *
     * @param channel
     * @param padding
     * @throws IOException
     */
    protected void writePadding(WritableByteChannel channel, int padding) throws IOException
    {
        if (padding > 0)
        {
            channel.write(ByteBuffer.wrap(new byte[padding]));
        }
    }

    /**
     * Checks to see if the file contains an ID3tag and if so return its size as reported in
     * the tag header  and return the size of the tag (including header), if no such tag exists return
     * zero.
     *
     * @param file
     * @return the end of the tag in the file or zero if no tag exists.
     * @throws java.io.IOException
     */
    public static long getV2TagSizeIfExists(File file) throws IOException
    {
        FileInputStream fis = null;
        FileChannel fc = null;
        ByteBuffer bb = null;
        try
        {
            //Files
            fis = new FileInputStream(file);
            fc = fis.getChannel();

            //Read possible Tag header  Byte Buffer
            bb = ByteBuffer.allocate(TAG_HEADER_LENGTH);
            fc.read(bb);
            bb.flip();
            if (bb.limit() < (TAG_HEADER_LENGTH))
            {
                return 0;
            }
        }
        finally
        {
            if (fc != null)
            {
                fc.close();
            }

            if (fis != null)
            {
                fis.close();
            }
        }

        //ID3 identifier
        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        bb.get(tagIdentifier, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(tagIdentifier, TAG_ID)))
        {
            return 0;
        }

        //Is it valid Major Version
        byte majorVersion = bb.get();
        if ((majorVersion != ID3v22Tag.MAJOR_VERSION) && (majorVersion != ID3v23Tag.MAJOR_VERSION) && (majorVersion != ID3v24Tag.MAJOR_VERSION))
        {
            return 0;
        }

        //Skip Minor Version
        bb.get();

        //Skip Flags
        bb.get();

        //Get size as recorded in frame header
        int frameSize = ID3SyncSafeInteger.bufferToValue(bb);

        //addField header size to frame size
        frameSize += TAG_HEADER_LENGTH;
        return frameSize;
    }

    /**
     * Does a tag of the correct version exist in this file.
     *
     * @param byteBuffer to search through
     * @return true if tag exists.
     */
    public boolean seek(ByteBuffer byteBuffer)
    {
        byteBuffer.rewind();
        logger.config("ByteBuffer pos:" + byteBuffer.position() + ":limit" + byteBuffer.limit() + ":cap" + byteBuffer.capacity());


        byte[] tagIdentifier = new byte[FIELD_TAGID_LENGTH];
        byteBuffer.get(tagIdentifier, 0, FIELD_TAGID_LENGTH);
        if (!(Arrays.equals(tagIdentifier, TAG_ID)))
        {
            return false;
        }
        //Major Version
        if (byteBuffer.get() != getMajorVersion())
        {
            return false;
        }
        //Minor Version
        return byteBuffer.get() == getRevision();
    }

    /**
     * This method determines the total tag size taking into account
     * the preferredSize and the min size required for new tag. For mp3
     * preferred size is the location of the audio, for other formats
     * preferred size is the size of the existing tag
     *
     * @param tagSize
     * @param preferredSize
     * @return
     */
    protected int calculateTagSize(int tagSize, int preferredSize)
    {
        if(TagOptionSingleton.getInstance().isId3v2PaddingWillShorten())
        {
            //We just use required size
            return tagSize;
        }
        else
        {
            //We can fit in the tag so no adjustments required
            if (tagSize <= preferredSize)
            {
                return preferredSize;
            }
            //There is not enough room as we need to move the audio file we might
            //as well increase it more than necessary for future changes
            return tagSize + TAG_SIZE_INCREMENT;
        }
    }

    /**
     * Write the data from the buffer to the file
     *
     * @param file
     * @param headerBuffer
     * @param bodyByteBuffer
     * @param padding
     * @param sizeIncPadding
     * @param audioStartLocation
     * @throws IOException
     */
    protected void writeBufferToFile(File file, ByteBuffer headerBuffer, byte[] bodyByteBuffer, int padding, int sizeIncPadding, long audioStartLocation) throws IOException
    {
        try(SeekableByteChannel fc = Files.newByteChannel(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE))
        {
            //We need to adjust location of audio file if true
            if (sizeIncPadding > audioStartLocation)
            {
                fc.position(audioStartLocation);
                ShiftData.shiftDataByOffsetToMakeSpace(fc, (int)(sizeIncPadding - audioStartLocation));
            }
            else if(TagOptionSingleton.getInstance().isId3v2PaddingWillShorten() && sizeIncPadding < audioStartLocation)
            {
                fc.position(audioStartLocation);
                ShiftData.shiftDataByOffsetToShrinkSpace(fc, (int)(audioStartLocation - sizeIncPadding));
            }
            fc.position(0);
            fc.write(headerBuffer);
            fc.write(ByteBuffer.wrap(bodyByteBuffer));
            fc.write(ByteBuffer.wrap(new byte[padding]));
        }
        catch(IOException ioe)
        {
            logger.log(Level.SEVERE, getLoggingFilename() + ioe.getMessage(), ioe);
            if (ioe.getMessage().equals(FileSystemMessage.ACCESS_IS_DENIED.getMsg()))
            {
                logger.severe(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING.getMsg(file.getParentFile().getPath()));
                throw new UnableToModifyFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING.getMsg(file.getParentFile().getPath()));
            }
            else
            {
                logger.severe(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING.getMsg(file.getParentFile().getPath()));
                throw new UnableToCreateFileException(ErrorMessage.GENERAL_WRITE_FAILED_TO_OPEN_FILE_FOR_EDITING.getMsg(file.getParentFile().getPath()));
            }
        }
    }

	private boolean containsAggregatedFrame(Collection<TagField> fields)
	{
		boolean result = false;
	
		if (fields != null)
		{
			for (TagField field : fields)
			{
				if (field instanceof AggregatedFrame)
				{
					result = true;
					break;
				}
			}
		}
	
		return result;
	}

    /**
     * Copy frame into map, whilst accounting for multiple frame of same type which can occur even if there were
     * not frames of the same type in the original tag
     *
     * @param id
     * @param newFrame
     */
    protected final void copyFrameIntoMap(String id, AbstractID3v2Frame newFrame)
    {
    	List<TagField> tagFields = frameMap.get(newFrame.getIdentifier());
    	if(tagFields == null) {
    		tagFields = new ArrayList<>();
    		tagFields.add(newFrame);
    		frameMap.put(newFrame.getIdentifier(), tagFields);
    	} else if(containsAggregatedFrame(tagFields)) 
    	{
    		logger.severe("Duplicated Aggregate Frame, ignoring:" + id);
    	} else 
    	{
    		combineFrames(newFrame, tagFields);
    	}
    }
    
    protected void combineFrames(AbstractID3v2Frame newFrame, List<TagField> existing) 
    {
    	existing.add(newFrame);
    }

    /**
     * Add frame to the frame map
     *
     * @param frameId
     * @param next
     */
    protected void loadFrameIntoMap(String frameId, AbstractID3v2Frame next)
    {
        if (next.getBody() instanceof FrameBodyEncrypted)
        {
            loadFrameIntoSpecifiedMap(encryptedFrameMap, frameId, next);
        }
        else
        {
            loadFrameIntoSpecifiedMap(frameMap, frameId, next);
        }
    }


    /**
     * Decides what to with the frame that has just been read from file.
     * If the frame is an allowable duplicate frame and is a duplicate we add all
     * frames into an ArrayList and add the ArrayList to the HashMap. if not allowed
     * to be duplicate we store the number of bytes in the duplicateBytes variable and discard
     * the frame itself.
     *
     * @param frameId
     * @param next
     */
    protected void loadFrameIntoSpecifiedMap(Map<String, List<TagField>> map, String frameId, AbstractID3v2Frame next)
    {
        if ((ID3v24Frames.getInstanceOf().isMultipleAllowed(frameId)) ||
                (ID3v23Frames.getInstanceOf().isMultipleAllowed(frameId)) ||
                (ID3v22Frames.getInstanceOf().isMultipleAllowed(frameId)))
        {
            //If a frame already exists of this type
            if (map.containsKey(frameId))
            {
            	List<TagField> multiValues = map.get(frameId);
                multiValues.add(next);
            }
            else
            {
                logger.finer("Adding Multi FrameList(3)" + frameId);
            	List<TagField> fields = new ArrayList<>();
            	fields.add(next);
                map.put(frameId, fields);
            }
        }
        //If duplicate frame just stores the name of the frame and the number of bytes the frame contains
        else if (map.containsKey(frameId) && !map.get(frameId).isEmpty())
        {
            logger.warning("Ignoring Duplicate Frame:" + frameId);
            //If we have multiple duplicate frames in a tag separate them with semicolons
            if (this.duplicateFrameId.length() > 0)
            {
                this.duplicateFrameId += ";";
            }
            this.duplicateFrameId += frameId;
			for (TagField tagField : frameMap.get(frameId)) 
			{
				if (tagField instanceof AbstractID3v2Frame) 
				{
					this.duplicateBytes += ((AbstractID3v2Frame) tagField).getSize();
				}
			}          
        }
        else
        {
            logger.finer("Adding Frame" + frameId);
        	List<TagField> fields = new ArrayList<>();
        	fields.add(next);
            map.put(frameId, fields);
        }
    }

    /**
     * Return tag size based upon the sizes of the tags rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.Should be extended
     * by subclasses to include header.
     *
     * @return size of the tag
     */
    public int getSize()
    {
        int size = 0;
        Iterator<List<TagField>> iterator = frameMap.values().iterator();
        AbstractID3v2Frame frame;
        while (iterator.hasNext())
        {
			List<TagField> fields = iterator.next();
			if (fields != null) 
			{
				for (TagField field : fields) 
				{
					if (field instanceof AggregatedFrame) 
					{
						AggregatedFrame af = (AggregatedFrame) field;
						for (AbstractID3v2Frame next : af.frames) 
						{
							size += next.getSize();
						}
					} 
					else if (field instanceof AbstractID3v2Frame) 
					{
						frame = (AbstractID3v2Frame) field;
						size += frame.getSize();
					}
				}
			}
        }
        return size;
    }

    /**
     * Write all the frames to the byteArrayOutputStream
     * <p/>
     * <p>Currently Write all frames, defaults to the order in which they were loaded, newly
     * created frames will be at end of tag.
     *
     * @return ByteBuffer Contains all the frames written within the tag ready for writing to file
     * @throws IOException
     */
    protected ByteArrayOutputStream writeFramesToBuffer() throws IOException
    {
        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        writeFramesToBufferStream(frameMap, bodyBuffer);
        writeFramesToBufferStream(encryptedFrameMap, bodyBuffer);
        return bodyBuffer;
    }

    /**
     * Write frames in map to bodyBuffer
     *
     * @param map
     * @param bodyBuffer
     * @throws IOException
     */
    private void writeFramesToBufferStream(Map<String, List<TagField>> map, ByteArrayOutputStream bodyBuffer) throws IOException
    {
        //Sort keys into Preferred Order
        TreeSet<String> sortedWriteOrder = new TreeSet<String>(getPreferredFrameOrderComparator());
        sortedWriteOrder.addAll(map.keySet());

        for (String id : sortedWriteOrder)
        {
            List<TagField> fields = map.get(id);
			for (TagField field : fields) 
			{
				if (field instanceof AbstractID3v2Frame) 
				{
					AbstractID3v2Frame frame = (AbstractID3v2Frame) field;
					frame.setLoggingFilename(getLoggingFilename());
					frame.write(bodyBuffer);
				} 
				else if (field instanceof AggregatedFrame) 
				{
					AggregatedFrame aggreagatedFrame = (AggregatedFrame) field;
					for (AbstractID3v2Frame next : aggreagatedFrame.getFrames()) 
					{
						next.setLoggingFilename(getLoggingFilename());
						next.write(bodyBuffer);
					}
				}
			}
        }
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file
     * so that most important frames are written first.
     */
    public abstract Comparator<String> getPreferredFrameOrderComparator();

    public void createStructure()
    {
        createStructureHeader();
        createStructureBody();
    }

    public void createStructureHeader()
    {
        MP3File.getStructureFormatter().addElement(TYPE_DUPLICATEBYTES, this.duplicateBytes);
        MP3File.getStructureFormatter().addElement(TYPE_DUPLICATEFRAMEID, this.duplicateFrameId);
        MP3File.getStructureFormatter().addElement(TYPE_EMPTYFRAMEBYTES, this.emptyFrameBytes);
        MP3File.getStructureFormatter().addElement(TYPE_FILEREADSIZE, this.fileReadSize);
        MP3File.getStructureFormatter().addElement(TYPE_INVALIDFRAMES, this.invalidFrames);
    }

    public void createStructureBody()
    {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_BODY, "");

        AbstractID3v2Frame frame;
        for (List<TagField> fields : frameMap.values())
        {
        	for(TagField o : fields) {
	            if (o instanceof AbstractID3v2Frame)
	            {
	                frame = (AbstractID3v2Frame) o;
	                frame.createStructure();
	            }
        	}
        }
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_BODY);
    }

    /**
     * Maps the generic key to the id3 key and return the list of values for this field as strings
     *
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public List<String> getAll(FieldKey genericKey) throws KeyNotFoundException
    {
        //Special case here because the generic key to frameid/subid mapping is identical for trackno versus tracktotal
        //and discno versus disctotal so we have to handle here, also want to ignore index parameter.
        List<String> values = new ArrayList<String>();
        List<TagField> fields = getFields(genericKey);

        if (ID3NumberTotalFields.isNumber(genericKey))
        {
            if (fields != null && fields.size() > 0)
            {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(((AbstractFrameBodyNumberTotal) frame.getBody()).getNumberAsText());
            }
            return values;
        }
        else if (ID3NumberTotalFields.isTotal(genericKey))
        {
            if (fields != null && fields.size() > 0)
            {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(((AbstractFrameBodyNumberTotal) frame.getBody()).getTotalAsText());
            }
            return values;
        }
        else if(genericKey == FieldKey.RATING)
        {
            if (fields != null && fields.size() > 0)
            {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                values.add(String.valueOf(((FrameBodyPOPM) frame.getBody()).getRating()));
            }
            return values;
        }
        else
        {
            return this.doGetValues(getFrameAndSubIdFromGenericKey(genericKey));
        }
    }

    /**
     * Retrieve the values that exists for this id3 frame id
     */
    public List<TagField> getFields(String id) throws KeyNotFoundException
    {
    	List<TagField> o = getFrame(id);
        if (o == null)
        {
            return new ArrayList<TagField>();
        }
        else if (o instanceof List)
        {
            //TODO should return copy
            return o;
        }
        else
        {
            throw new RuntimeException("Found entry in frameMap that was not a frame or a list:" + o);
        }
    }


    /**
     * Create Frame of correct ID3 version with the specified id
     *
     * @param id
     * @return
     */
    public abstract AbstractID3v2Frame createFrame(String id);

    //TODO

    public boolean hasCommonFields()
    {
        return true;
    }

    /**
     * Does this tag contain a field with the specified key
     *
     * @param key The field id to look for.
     * @return true if has field , false if does not or if no mapping for key exists
     */
    public boolean hasField(FieldKey key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }

        try
        {
            return getFirstField(key) != null;
        }
        catch (KeyNotFoundException knfe)
        {
            logger.log(Level.SEVERE, knfe.getMessage(), knfe);
            return false;
        }
    }

    /**
     * Does this tag contain a field with the specified id
     *
     * @see org.jaudiotagger.tag.Tag#hasField(java.lang.String)
     */
    public boolean hasField(String id)
    {
        return hasFrame(id);
    }

    /**
     * Is this tag empty
     *
     * @see org.jaudiotagger.tag.Tag#isEmpty()
     */
    public boolean isEmpty()
    {
        return frameMap.size() == 0;
    }

    /**
     * @return iterator of all fields, multiple values for the same Id (e.g multiple TXXX frames) count as separate
     * fields
     */
    public Iterator<TagField> getFields()
    {
        //Iterator of each different frameId in this tag
    	List<TagField> allTagFields = new ArrayList<>();
		for (List<TagField> value : this.frameMap.values()) 
		{
			allTagFields.addAll(value);
		}

        return allTagFields.iterator();
    }

    /**
     * Count number of frames/fields in this tag
     *
     * @return
     */
    public int getFieldCount()
    {
        Iterator<TagField> it = getFields();
        int count = 0;

        //Done this way because it.hasNext() incorrectly counts empty list
        //whereas it.next() works correctly
        try
        {
            while (true)
            {
                it.next();
                count++;
            }
        }
        catch (NoSuchElementException nse)
        {
            //this is thrown when no more elements
        }
        return count;
    }

    /**
     * Return count of fields, this considers a text frame with two null separated values as two fields, if you want
     * a count of frames @see getFrameCount
     *
     * @return count of fields
     */
    public int getFieldCountIncludingSubValues()
    {
        Iterator<TagField> it = getFields();
        int count = 0;

        //Done this way because it.hasNext() incorrectly counts empty list
        //whereas it.next() works correctly
        try
        {
            while (true)
            {
                TagField next = it.next();
                if (next instanceof AbstractID3v2Frame)
                {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame) next;
                    if ((frame.getBody() instanceof AbstractFrameBodyTextInfo) && !(frame.getBody() instanceof FrameBodyTXXX))
                    {
                        AbstractFrameBodyTextInfo frameBody = (AbstractFrameBodyTextInfo) frame.getBody();
                        count += frameBody.getNumberOfValues();
                        continue;
                    }
                }
                count++;
            }
        }
        catch (NoSuchElementException nse)
        {
            //this is thrown when no more elements
        }
        return count;
    }

    //TODO is this a special field?

    @Override
    public boolean setEncoding(final Charset enc) throws FieldDataInvalidException
    {
        throw new UnsupportedOperationException("Not Implemented Yet");
    }

    /**
     * Retrieve the first value that exists for this generic key
     *
     * @param genericKey
     * @return
     */
    public String getFirst(FieldKey genericKey) throws KeyNotFoundException
    {
        return getValue(genericKey, 0);
    }

    /**
     * Retrieve the value that exists for this generic key and this index
     * <p/>
     * Have to do some special mapping for certain generic keys because they share frame
     * with another generic key.
     *
     * @param genericKey
     * @return
     */
    public String getValue(FieldKey genericKey, int index) throws KeyNotFoundException
    {
        if (genericKey == null)
        {
            throw new KeyNotFoundException();
        }

        //Special case here because the generic key to frameid/subid mapping is identical for trackno versus tracktotal
        //and discno versus disctotal so we have to handle here, also want to ignore index parameter.
        if (ID3NumberTotalFields.isNumber(genericKey)||ID3NumberTotalFields.isTotal(genericKey))
        {
            List<TagField> fields = getFields(genericKey);
            if (fields != null && fields.size() > 0)
            {
                //Should only be one frame so ignore index value, and we ignore multiple values within the frame
                //it would make no sense if it existed.
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                if (ID3NumberTotalFields.isNumber(genericKey))
                {
                    return ((AbstractFrameBodyNumberTotal) frame.getBody()).getNumberAsText();
                }
                else if (ID3NumberTotalFields.isTotal(genericKey))
                {
                    return ((AbstractFrameBodyNumberTotal) frame.getBody()).getTotalAsText();
                }
            }
            else
            {
                return "";
            }
        }
        //Special Case, TODO may be possible to put into doGetValueAtIndex but getUserFriendlyValue in POPMGFrameBody
        //is implemented different to what we would need.
        else if (genericKey == FieldKey.RATING)
        {
            List<TagField> fields = getFields(genericKey);
            if (fields != null && fields.size() > index)
            {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(index);
                return String.valueOf(((FrameBodyPOPM) frame.getBody()).getRating());
            }
            else
            {
                return "";
            }
        }

        FrameAndSubId frameAndSubId = getFrameAndSubIdFromGenericKey(genericKey);
        return doGetValueAtIndex(frameAndSubId, index);
    }

    /**
     * Create a new field
     *
     * Only MUSICIAN field make use of Varargs values field
     *
     * @param genericKey is the generic key
     * @param values
     * @return
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public TagField createField(FieldKey genericKey, String... values) throws KeyNotFoundException, FieldDataInvalidException
    {
        if (genericKey == null)
        {
            throw new KeyNotFoundException();
        }

        if (values == null || values[0] == null)
        {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }

        String value = values[0];
        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);

        //FrameAndSubId does not contain enough info for these fields to be able to work out what to update
        //that is why we need the extra processing here instead of doCreateTagField()
        if (ID3NumberTotalFields.isNumber(genericKey))
        {
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            AbstractFrameBodyNumberTotal framebody = (AbstractFrameBodyNumberTotal) frame.getBody();
            framebody.setNumber(value);
            return frame;
        }
        else if (ID3NumberTotalFields.isTotal(genericKey))
        {
            AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
            AbstractFrameBodyNumberTotal framebody = (AbstractFrameBodyNumberTotal) frame.getBody();
            framebody.setTotal(value);
            return frame;
        }
        else
        {
            return doCreateTagField(formatKey, values);
        }
    }

    /**
     * Create Frame for Id3 Key
     * <p/>
     * Only textual data supported at the moment, should only be used with frames that
     * support a simple string argument.
     *
     * @param formatKey
     * @param values
     * @return
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    protected TagField doCreateTagField(FrameAndSubId formatKey, String... values) throws KeyNotFoundException, FieldDataInvalidException
    {
        String value = values[0];

        AbstractID3v2Frame frame = createFrame(formatKey.getFrameId());
        if (frame.getBody() instanceof FrameBodyUFID)
        {
            ((FrameBodyUFID) frame.getBody()).setOwner(formatKey.getSubId());
            try
            {
                ((FrameBodyUFID) frame.getBody()).setUniqueIdentifier(value.getBytes("ISO-8859-1"));
            }
            catch (UnsupportedEncodingException uee)
            {
                //This will never happen because we are using a charset supported on all platforms
                //but just in case
                throw new RuntimeException("When encoding UFID charset ISO-8859-1 was deemed unsupported");
            }
        }
        else if (frame.getBody() instanceof FrameBodyTXXX)
        {
            ((FrameBodyTXXX) frame.getBody()).setDescription(formatKey.getSubId());
            ((FrameBodyTXXX) frame.getBody()).setText(value);
        }
        else if (frame.getBody() instanceof FrameBodyWXXX)
        {
            ((FrameBodyWXXX) frame.getBody()).setDescription(formatKey.getSubId());
            ((FrameBodyWXXX) frame.getBody()).setUrlLink(value);
        }
        else if (frame.getBody() instanceof FrameBodyCOMM)
        {
            //Set description if set
            if (formatKey.getSubId() != null)
            {
                ((FrameBodyCOMM) frame.getBody()).setDescription(formatKey.getSubId());
                //Special Handling for Media Monkey Compatability
                if (((FrameBodyCOMM) frame.getBody()).isMediaMonkeyFrame())
                {
                    ((FrameBodyCOMM) frame.getBody()).setLanguage(Languages.MEDIA_MONKEY_ID);
                }
            }
            ((FrameBodyCOMM) frame.getBody()).setText(value);
        }
        else if (frame.getBody() instanceof FrameBodyUSLT)
        {
            ((FrameBodyUSLT) frame.getBody()).setDescription("");
            ((FrameBodyUSLT) frame.getBody()).setLyric(value);
        }
        else if (frame.getBody() instanceof FrameBodyWOAR)
        {
            ((FrameBodyWOAR) frame.getBody()).setUrlLink(value);
        }
        else if (frame.getBody() instanceof AbstractFrameBodyTextInfo)
        {
            ((AbstractFrameBodyTextInfo) frame.getBody()).setText(value);
        }
        else if (frame.getBody() instanceof FrameBodyPOPM)
        {
            ((FrameBodyPOPM) frame.getBody()).parseString(value);
        }
        else if (frame.getBody() instanceof FrameBodyIPLS)
        {
            if (formatKey.getSubId() != null)
            {
                ((FrameBodyIPLS) (frame.getBody())).addPair(formatKey.getSubId(), value);
            }
            else
            {
                if(values.length>=2)
                {
                    ((FrameBodyIPLS) (frame.getBody())).addPair(values[0], values[1]);
                }
                else
                {
                    ((FrameBodyIPLS) (frame.getBody())).addPair(values[0]);
                }
            }
        }
        else if (frame.getBody() instanceof FrameBodyTIPL)
        {
            if(formatKey.getSubId()!=null)
            {
                ((FrameBodyTIPL) (frame.getBody())).addPair(formatKey.getSubId(), value);
            }
            else if(values.length>=2)
            {
                ((FrameBodyTIPL) (frame.getBody())).addPair(values[0], values[1]);
            }
            else
            {
                ((FrameBodyTIPL) (frame.getBody())).addPair(values[0]);
            }
        }
        else if (frame.getBody() instanceof FrameBodyTMCL)
        {
            if(values.length>=2)
            {
                ((FrameBodyTMCL) (frame.getBody())).addPair(values[0], values[1]);
            }
            else
            {
                ((FrameBodyTMCL) (frame.getBody())).addPair(values[0]);
            }
        }
        else if ((frame.getBody() instanceof FrameBodyAPIC) || (frame.getBody() instanceof FrameBodyPIC))
        {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        else
        {
            throw new FieldDataInvalidException("Field with key of:" + formatKey.getFrameId() + ":does not accept cannot parse data:" + value);
        }
        return frame;
    }

    /**
     * Create a list of values for this (sub)frame
     * <p/>
     * This method  does all the complex stuff of splitting multiple values in one frame into separate values.
     *
     * @param formatKey
     * @return
     * @throws KeyNotFoundException
     */
    protected List<String> doGetValues(FrameAndSubId formatKey) throws KeyNotFoundException
    {
        List<String> values = new ArrayList<String>();

        if (formatKey.getSubId() != null)
        {
            //Get list of frames that this uses
            List<TagField> list = getFields(formatKey.getFrameId());
            ListIterator<TagField> li = list.listIterator();
            while (li.hasNext())
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) li.next()).getBody();

                if (next instanceof FrameBodyTXXX)
                {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        values.addAll((((FrameBodyTXXX) next).getValues()));
                    }
                }
                else if (next instanceof FrameBodyWXXX)
                {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        values.addAll((((FrameBodyWXXX) next).getUrlLinks()));
                    }
                }
                else if (next instanceof FrameBodyCOMM)
                {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId()))
                    {
                        values.addAll((((FrameBodyCOMM) next).getValues()));
                    }
                }
                else if (next instanceof FrameBodyUFID)
                {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId()))
                    {
                        if (((FrameBodyUFID) next).getUniqueIdentifier() != null)
                        {
                            values.add(new String(((FrameBodyUFID) next).getUniqueIdentifier()));
                        }
                    }
                }
                else if (next instanceof AbstractFrameBodyPairs)
                {
                    for (Pair entry : ((AbstractFrameBodyPairs) next).getPairing().getMapping())
                    {
                        if (entry.getKey().equals(formatKey.getSubId()))
                        {
                            if (entry.getValue() != null)
                            {
                                values.add(entry.getValue());
                            }
                        }
                    }
                }
                else
                {
                    logger.severe(getLoggingFilename() + ":Need to implement getFields(FieldKey genericKey) for:" + formatKey + next.getClass());
                }
            }
        }
        //Special handling for paired fields with no defined key
        else if ((formatKey.getGenericKey()!=null)&&
                 (formatKey.getGenericKey() == FieldKey.INVOLVEDPEOPLE)
                )
        {
            List<TagField> list = getFields(formatKey.getFrameId());
            ListIterator<TagField> li = list.listIterator();
            while (li.hasNext())
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) li.next()).getBody();
                if (next instanceof AbstractFrameBodyPairs)
                {
                    for (Pair entry : ((AbstractFrameBodyPairs) next).getPairing().getMapping())
                    {
                        //if(!StandardIPLSKey.isKey(entry.getKey()))
                        if(true)
                        {
                            if (!entry.getValue().isEmpty())
                            {
                                if (!entry.getKey().isEmpty())
                                {
                                    values.add(entry.getPairValue());
                                }
                                else
                                {
                                    values.add(entry.getValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        //Simple 1 to 1 mapping
        else
        {
            List<TagField> list = getFields(formatKey.getFrameId());
            for (TagField next : list)
            {
                AbstractID3v2Frame frame = (AbstractID3v2Frame) next;
                if (frame != null)
                {
                    if (frame.getBody() instanceof AbstractFrameBodyTextInfo)
                    {
                        AbstractFrameBodyTextInfo fb = (AbstractFrameBodyTextInfo) frame.getBody();
                        values.addAll(fb.getValues());
                    }
                    else
                    {
                        values.add(getTextValueForFrame(frame));
                    }
                }
            }
        }
        return values;
    }

    /**
     * Get the value at the index, we massage the values so that the index as used in the generic interface rather
     * than simply taking the frame index. For example if two composers have been added then then they can be retrieved
     * individually using index=0, index=1 despite the fact that both internally will be stored in a single TCOM frame.
     *
     * @param formatKey
     * @param index     the index specified by the user
     * @return
     * @throws KeyNotFoundException
     */
    protected String doGetValueAtIndex(FrameAndSubId formatKey, int index) throws KeyNotFoundException
    {
        List<String> values = doGetValues(formatKey);
        if (values.size() > index)
        {
            return values.get(index);
        }
        return "";
    }

    /**
     * Create a link to artwork, this is not recommended because the link may be broken if the mp3 or image
     * file is moved
     *
     * @param url specifies the link, it could be a local file or could be a full url
     * @return
     */
    public TagField createLinkedArtworkField(String url)
    {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
        if (frame.getBody() instanceof FrameBodyAPIC)
        {
            FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
            body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, url.getBytes(StandardCharsets.ISO_8859_1));
            body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
            body.setObjectValue(DataTypes.OBJ_MIME_TYPE, FrameBodyAPIC.IMAGE_IS_URL);
            body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
        }
        else if (frame.getBody() instanceof FrameBodyPIC)
        {
            FrameBodyPIC body = (FrameBodyPIC) frame.getBody();
            body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, url.getBytes(StandardCharsets.ISO_8859_1));
            body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
            body.setObjectValue(DataTypes.OBJ_IMAGE_FORMAT, FrameBodyAPIC.IMAGE_IS_URL);
            body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
        }
        return frame;
    }


    /**
     * Some frames are used to store a number/total value, we have to consider both values when requested to delete a
     * key relating to one of them
     *
     * @param formatKey
     * @param numberFieldKey
     * @param totalFieldKey
     * @param deleteNumberFieldKey
     */
    private void deleteNumberTotalFrame(FrameAndSubId formatKey, FieldKey numberFieldKey, FieldKey totalFieldKey, boolean deleteNumberFieldKey)
    {
        if (deleteNumberFieldKey)
        {
            String total = this.getFirst(totalFieldKey);
            if (total.length() == 0)
            {
                doDeleteTagField(formatKey);
                return;
            }
            else
            {
                List<TagField> fields = getFrame(formatKey.getFrameId());
                if (fields.size() > 0)
                {
                    AbstractID3v2Frame frame = (AbstractID3v2Frame) fields.get(0);
                    AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
                    if (frameBody.getTotal() == 0)
                    {
                        doDeleteTagField(formatKey);
                        return;
                    }
                    frameBody.setNumber(0);
                    return;
                }
            }
        }
        else
        {
            String number = this.getFirst(numberFieldKey);
            if (number.length() == 0)
            {
                doDeleteTagField(formatKey);
                return;
            }
            else
            {
				for (TagField field : this.getFrame(formatKey.getFrameId())) 
				{
					if (field instanceof AbstractID3v2Frame) 
					{
						AbstractID3v2Frame frame = (AbstractID3v2Frame) field;
						AbstractFrameBodyNumberTotal frameBody = (AbstractFrameBodyNumberTotal) frame.getBody();
						if (frameBody.getNumber() == 0) 
						{
							doDeleteTagField(formatKey);
						}
						frameBody.setTotal(0);
					}
				}
            }
        }
    }
    /**
     * Delete fields with this generic key
     *
     * If generic key maps to multiple frames then do special processing here rather doDeleteField()
     *
     * @param fieldKey
     */
    public void deleteField(FieldKey fieldKey) throws KeyNotFoundException
    {
        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(fieldKey);
        if (fieldKey == null)
        {
            throw new KeyNotFoundException();
        }

        switch(fieldKey)
        {
            case TRACK:
                deleteNumberTotalFrame(formatKey, FieldKey.TRACK, FieldKey.TRACK_TOTAL, true);
                break;
            case TRACK_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.TRACK, FieldKey.TRACK_TOTAL, false);
                break;
            case DISC_NO:
                deleteNumberTotalFrame(formatKey, FieldKey.DISC_NO, FieldKey.DISC_TOTAL, true);
                break;
            case DISC_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.DISC_NO, FieldKey.DISC_TOTAL, false);
                break;
            case MOVEMENT_NO:
                deleteNumberTotalFrame(formatKey, FieldKey.MOVEMENT_NO, FieldKey.MOVEMENT_TOTAL, true);
                break;
            case MOVEMENT_TOTAL:
                deleteNumberTotalFrame(formatKey, FieldKey.MOVEMENT_NO, FieldKey.MOVEMENT_TOTAL, false);
                break;
            default:
                doDeleteTagField(formatKey);
        }

    }

    /**
     * Internal delete method, for deleting/modifying an individual ID3 frame
     *
     * @param formatKey
     * @throws KeyNotFoundException
     */
    protected void doDeleteTagField(FrameAndSubId formatKey) throws KeyNotFoundException
    {
        if (formatKey.getSubId() != null)
        {
            //Get list of frames that this uses
            List<TagField> list = getFields(formatKey.getFrameId());
            ListIterator<TagField> li = list.listIterator();
            while (li.hasNext())
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) li.next()).getBody();
                if (next instanceof FrameBodyTXXX)
                {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        if (list.size() == 1)
                        {
                            removeFrame(formatKey.getFrameId());
                        }
                        else
                        {
                            li.remove();
                        }
                    }
                }
                else if (next instanceof FrameBodyCOMM)
                {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId()))
                    {
                        if (list.size() == 1)
                        {
                            removeFrame(formatKey.getFrameId());
                        }
                        else
                        {
                            li.remove();
                        }
                    }
                }
                else if (next instanceof FrameBodyWXXX)
                {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        if (list.size() == 1)
                        {
                            removeFrame(formatKey.getFrameId());
                        }
                        else
                        {
                            li.remove();
                        }
                    }
                }
                else if (next instanceof FrameBodyUFID)
                {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId()))
                    {
                        if (list.size() == 1)
                        {
                            removeFrame(formatKey.getFrameId());
                        }
                        else
                        {
                            li.remove();
                        }
                    }
                }
                else
                {
                    throw new RuntimeException("Need to implement getFields(FieldKey genericKey) for:" + next.getClass());
                }
            }
        }
        //Simple 1 to 1 mapping
        else if (formatKey.getSubId() == null)
        {
            removeFrame(formatKey.getFrameId());
        }
    }

    protected abstract FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey);

    /**
     * Get field(s) for this generic key
     * <p/>
     * This will return the number of underlying frames of this type, for example if you have added two TCOM field
     * values these will be stored within a single frame so only one field will be returned not two. This can be
     * confusing because getValues() would return two values.
     *
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public List<TagField> getFields(FieldKey genericKey) throws KeyNotFoundException
    {
        if (genericKey == null)
        {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        FrameAndSubId formatKey = getFrameAndSubIdFromGenericKey(genericKey);

        if (formatKey == null)
        {
            throw new KeyNotFoundException();
        }

        //Get list of frames that this uses, as we are going to remove entries we don't want take a copy
        List<TagField> list = getFields(formatKey.getFrameId());
        List<TagField> filteredList = new ArrayList<TagField>();
        String subFieldId = formatKey.getSubId();

        if (subFieldId != null)
        {
            for (TagField tagfield : list)
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof FrameBodyTXXX)
                {
                    if (((FrameBodyTXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        filteredList.add(tagfield);
                    }
                }
                else if (next instanceof FrameBodyWXXX)
                {
                    if (((FrameBodyWXXX) next).getDescription().equals(formatKey.getSubId()))
                    {
                        filteredList.add(tagfield);
                    }
                }
                else if (next instanceof FrameBodyCOMM)
                {
                    if (((FrameBodyCOMM) next).getDescription().equals(formatKey.getSubId()))
                    {
                        filteredList.add(tagfield);
                    }
                }
                else if (next instanceof FrameBodyUFID)
                {
                    if (((FrameBodyUFID) next).getOwner().equals(formatKey.getSubId()))
                    {
                        filteredList.add(tagfield);
                    }
                }
                else if (next instanceof FrameBodyIPLS)
                {
                    for (Pair entry : ((FrameBodyIPLS) next).getPairing().getMapping())
                    {
                        if (entry.getKey().equals(formatKey.getSubId()))
                        {
                            filteredList.add(tagfield);
                        }
                    }
                }
                else if (next instanceof FrameBodyTIPL)
                {
                    for (Pair entry : ((FrameBodyTIPL) next).getPairing().getMapping())
                    {
                        if (entry.getKey().equals(formatKey.getSubId()))
                        {
                            filteredList.add(tagfield);
                        }
                    }
                }
                else
                {
                    logger.severe("Need to implement getFields(FieldKey genericKey) for:" + formatKey + next.getClass());
                }
            }
            return filteredList;
        }
        else if(ID3NumberTotalFields.isNumber(genericKey))
        {
            for (TagField tagfield : list)
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof AbstractFrameBodyNumberTotal)
                {
                    if (((AbstractFrameBodyNumberTotal) next).getNumber() != null)
                    {
                        filteredList.add(tagfield);
                    }
                }
            }
            return filteredList;
        }
        else if(ID3NumberTotalFields.isTotal(genericKey))
        {
            for (TagField tagfield : list)
            {
                AbstractTagFrameBody next = ((AbstractID3v2Frame) tagfield).getBody();
                if (next instanceof AbstractFrameBodyNumberTotal)
                {
                    if (((AbstractFrameBodyNumberTotal) next).getTotal() != null)
                    {
                        filteredList.add(tagfield);
                    }
                }
            }
            return filteredList;
        }
        else
        {
            return list;
        }
    }

    /**
     * This class had to be created to minimize the duplicate code in concrete subclasses
     * of this class. It is required in some cases when using the fieldKey enums because enums
     * cannot be sub classed. We want to use enums instead of regular classes because they are
     * much easier for end users to  to use.
     */
    class FrameAndSubId
    {
        private FieldKey genericKey;
        private String frameId;
        private String subId;

        public FrameAndSubId(FieldKey genericKey, String frameId, String subId)
        {
            this.genericKey = genericKey;
            this.frameId = frameId;
            this.subId = subId;
        }

        public FieldKey getGenericKey()
        {
            return genericKey;
        }

        public String getFrameId()
        {
            return frameId;
        }

        public String getSubId()
        {
            return subId;
        }

        public String toString()
        {
            return String.format("%s:%s:%s",genericKey.name(), frameId, subId);
        }
    }


    public Artwork getFirstArtwork()
    {
        List<Artwork> artwork = getArtworkList();
        if (artwork.size() > 0)
        {
            return artwork.get(0);
        }
        return null;
    }

    /**
     * Create field and then set within tag itself
     *
     * @param artwork
     * @throws FieldDataInvalidException
     */
    public void setField(Artwork artwork) throws FieldDataInvalidException
    {
        this.setField(createField(artwork));
    }

    /**
     * Create field and then set within tag itself
     *
     * @param artwork
     * @throws FieldDataInvalidException
     */
    public void addField(Artwork artwork) throws FieldDataInvalidException
    {
        this.addField(createField(artwork));
    }

    /**
     * Delete all instance of artwork Field
     *
     * @throws KeyNotFoundException
     */
    public void deleteArtworkField() throws KeyNotFoundException
    {
        this.deleteField(FieldKey.COVER_ART);
    }

    @Override
    public String toString()
    {
        final StringBuilder out = new StringBuilder();
        out.append("Tag content:\n");
        final Iterator<TagField> it = getFields();
        while (it.hasNext())
        {
            final TagField field = it.next();
            out.append("\t");
            out.append(field.getId());
            out.append(":");
            out.append(field.toString());
            out.append("\n");
        }

        return out.toString();
    }

    public TagField createCompilationField(boolean value) throws KeyNotFoundException, FieldDataInvalidException
    {
        if (value)
        {
            return createField(FieldKey.IS_COMPILATION, "1");
        }
        else
        {
            return createField(FieldKey.IS_COMPILATION, "0");
        }
    }

    public Long getStartLocationInFile()
    {
        return startLocationInFile;
    }

    public void setStartLocationInFile(long startLocationInFile)
    {
        this.startLocationInFile = startLocationInFile;
    }

    public Long getEndLocationInFile()
    {
        return endLocationInFile;
    }

    public void setEndLocationInFile(long endLocationInFile)
    {
        this.endLocationInFile = endLocationInFile;
    }
}