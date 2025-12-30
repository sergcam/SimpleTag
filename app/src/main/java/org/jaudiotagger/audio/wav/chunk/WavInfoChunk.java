package org.jaudiotagger.audio.wav.chunk;

import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.iff.IffHeaderChunk;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.wav.WavInfoTag;
import org.jaudiotagger.tag.wav.WavTag;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores basic only metadata but only exists as part of a LIST chunk, doesn't have its own size field
 * instead contains a number of name,size, value tuples. So for this reason we do not subclass the Chunk class
 */
public class WavInfoChunk
{
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.wav.WavInfoChunk");

    private WavInfoTag wavInfoTag;
    private String    loggingName;

    public WavInfoChunk(WavTag tag, String loggingName)
    {
        this.loggingName = loggingName;
        wavInfoTag = new WavInfoTag();
        tag.setInfoTag(wavInfoTag);
    }

    /**
     * Read Info chunk
     * @param chunkData
     */
    public  boolean readChunks(ByteBuffer chunkData)
    {
        EnumSet<FieldKey> overrideFieldKeys = TagOptionSingleton.getInstance().getOverrideCharsetFields();

        while(chunkData.remaining()>= IffHeaderChunk.TYPE_LENGTH)
        {
            String id       = Utils.readFourBytesAsChars(chunkData);

            //Padding
            if(id.trim().isEmpty())
            {
                return true;
            }
            int    size     = chunkData.getInt();

            //Check Identifier is valid, #655 we allow numeric characters although not strictly correctly lets us process
            //slightly wrong files but dont allow any other more unusual characters
            if(
                    (!Character.isAlphabetic(id.charAt(0)) && !Character.isDigit(id.charAt(0)) )||
                    (!Character.isAlphabetic(id.charAt(1))  && !Character.isDigit(id.charAt(1)) )||
                    (!Character.isAlphabetic(id.charAt(2))  && !Character.isDigit(id.charAt(2)))||
                    (!Character.isAlphabetic(id.charAt(3))  && !Character.isDigit(id.charAt(3)))
               )
            {
                logger.severe(loggingName + "LISTINFO appears corrupt, non-alphabetical characters ignoring:"+id+":"+size);
                return false;
            }

            //Find FieldKey relating to this field and if one of the override fields keys
            Charset charset = StandardCharsets.UTF_8;

            //Sometimes applications default to users default charset than sticking by the spec which specifies UTF-8
            //if we have options to override set and the field key is one of the fields we want to override for use override chatset
            if (TagOptionSingleton.getInstance().isOverrideCharsetForInfo() && TagOptionSingleton.getInstance().getOverrideCharset() != null)
            {
                WavInfoIdentifier wii = WavInfoIdentifier.getByCode(id);
                if (overrideFieldKeys.contains(wii.getFieldKey()))
                {
                    charset = TagOptionSingleton.getInstance().getOverrideCharset();
                    logger.severe(loggingName + "Charset used is:" + charset.displayName());
                }
            }

            //Read data for identifier
            String value =null;
            try
            {
                value = Utils.getString(chunkData, 0, size, charset);
            }
            catch(BufferUnderflowException bue)
            {
                logger.log(Level.SEVERE, loggingName + "LISTINFO appears corrupt, buffer underflow, ignoring:"+bue.getMessage(), bue);
                return false;
            }

            //Is it known identifer
            logger.config(loggingName + "Result:" + id + ":" + size + ":" + value + ":");
            WavInfoIdentifier wii = WavInfoIdentifier.getByCode(id);
            if(wii!=null && wii.getFieldKey()!=null)
            {
                try
                {
                    wavInfoTag.setField(wii.getFieldKey(), value);
                }
                catch(FieldDataInvalidException fdie)
                {
                    logger.log(Level.SEVERE, loggingName + fdie.getMessage(), fdie);
                }
            }
            //Add unless just padding
            else if(id!=null && !id.trim().isEmpty())
            {
                wavInfoTag.addUnRecognizedField(id, value);
            }

            //Each tuple aligned on even byte boundary
            if (Utils.isOddLength(size) && chunkData.hasRemaining())
            {
                chunkData.get();
            }
        }
        return true;
    }
}
