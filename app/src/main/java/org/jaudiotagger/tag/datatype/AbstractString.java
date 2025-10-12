/**
 *  @author : Paul Taylor
 *  @author : Eric Farng
 *
 *  Version @version:$Id$
 *
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
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * Description:
 *
 */
package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyCOMM;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.id3.framebody.FrameBodyWXXX;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

/**
 * A partial implementation for String based ID3 fields
 */
public abstract class AbstractString extends AbstractDataType
{
    //When String is used to describe the type of frame such as type of TXXX frame the logic is that this field will not
    //be incorrectly encoded even if the value is so we dont allow override of charset in these cases by checking the value
    //of this boolean
    protected boolean isAllowReadMetadataWithOverrideCharset = false;

    /**
     * Creates a new  datatype
     *
     * @param identifier
     * @param frameBody
     */
    protected AbstractString(String identifier, AbstractTagFrameBody frameBody)
    {
        super(identifier, frameBody);
    }

    /**
     * Creates a new  datatype, with value
     *
     * @param identifier
     * @param frameBody
     * @param value
     */
    public AbstractString(String identifier, AbstractTagFrameBody frameBody, String value)
    {
        super(identifier, frameBody, value);
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    protected AbstractString(AbstractString object)
    {
        super(object);
    }

    /**
     * Return the size in bytes of this datatype as it was/is held in file this
     * will be effected by the encoding type.
     *
     * @return the size
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Sets the size in bytes of this data type.
     * This is set after writing the data to allow us to recalculate the size for
     * frame header.
     * @param size
     */
    protected void setSize(int size)
    {
        this.size = size;
    }

    /**
     * Return String representation of data type
     *
     * @return a string representation of the value
     */
    public String toString()
    {
        return (String) value;
    }

    /**
     * Check the value can be encoded with the specified encoding
     * @return
     */
    public boolean canBeEncoded()
    {
        //Try and write to buffer using the CharSet defined by the textEncoding field (note if using UTF16 we dont
        //need to worry about LE,BE at this point it makes no difference)
        final byte textEncoding = this.getBody().getTextEncoding();
        final TextEncoding encoding = TextEncoding.getInstanceOf();
        final Charset charset = encoding.getCharsetForId(textEncoding);
        CharsetEncoder encoder = charset.newEncoder();

        if (encoder.canEncode((String) value))
        {
            return true;
        }
        else
        {
            logger.finest("Failed Trying to decode" + value + "with" + encoder.toString());
            return false;
        }
    }

    /**
     * If charset encoding byte to zero this should be read as ISO-8859-1 unless overridecharset is set to allow to read as
     * different charset in case user has used non-standard charset instead, this is quite common if based in countries
     * where the default language is not English
     *
     * If they have specified UTF-16 then decoder works out by looking at BOM
     * but if missing we have to make an educated guess otherwise just use
     * specified decoder
     *
     * @param inBuffer
     * @return
     */
    protected CharsetDecoder getCorrectDecoder(ByteBuffer inBuffer)
    {
        EnumSet<FieldKey> overrideFieldKeys = TagOptionSingleton.getInstance().getOverrideCharsetFields();
        Charset charset = getTextEncodingCharSet();
        if(charset==StandardCharsets.ISO_8859_1
                && isAllowReadMetadataWithOverrideCharset
                && TagOptionSingleton.getInstance().isOverrideCharsetForId3()
                && TagOptionSingleton.getInstance().getOverrideCharset()!=null)
        {
            //Get generic key based on id
            ID3v23FieldKey id3v23FieldKey=null;
            if(frameBody instanceof FrameBodyTXXX)
            {
                id3v23FieldKey = ID3v23FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyTXXX)frameBody).getDescription());
            }
            else if(frameBody instanceof FrameBodyWXXX)
            {
                id3v23FieldKey = ID3v23FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyWXXX)frameBody).getDescription());
            }
            else if(frameBody instanceof FrameBodyCOMM)
            {
                id3v23FieldKey = ID3v23FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyCOMM)frameBody).getDescription());
            }
            else
            {
                id3v23FieldKey = ID3v23FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier());
            }

            if(id3v23FieldKey!=null)
            {
                FieldKey fieldKey = ID3v23Frames.getInstanceOf().getGenericKeyFromId3(id3v23FieldKey);
                if (fieldKey != null)
                {
                    if (overrideFieldKeys.contains(fieldKey))
                    {
                        charset = TagOptionSingleton.getInstance().getOverrideCharset();
                    }
                }
            }
            else
            {
                //Get generic key based on id
                ID3v24FieldKey id3v24FieldKey=null;
                if(frameBody instanceof FrameBodyTXXX)
                {
                    id3v24FieldKey = ID3v24FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyTXXX)frameBody).getDescription());
                }
                else if(frameBody instanceof FrameBodyWXXX)
                {
                    id3v24FieldKey = ID3v24FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyWXXX)frameBody).getDescription());
                }
                else if(frameBody instanceof FrameBodyCOMM)
                {
                    id3v24FieldKey = ID3v24FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier() + ((FrameBodyCOMM)frameBody).getDescription());
                }
                else
                {
                    id3v24FieldKey = ID3v24FieldKey.getFieldKeyFromFrameId(frameBody.getIdentifier());
                }

                if(id3v24FieldKey!=null)
                {
                    FieldKey fieldKey = ID3v24Frames.getInstanceOf().getGenericKeyFromId3(id3v24FieldKey);
                    if (fieldKey != null)
                    {
                        if (overrideFieldKeys.contains(fieldKey))
                        {
                            charset = TagOptionSingleton.getInstance().getOverrideCharset();
                        }
                    }
                }
            }
        }

        CharsetDecoder decoder=null;
        if(inBuffer.remaining()<=2)
        {
            decoder = charset.newDecoder();
            decoder.reset();
            return decoder;
        }

        if(charset == StandardCharsets.UTF_16)
        {
            if(inBuffer.getChar(0)==0xfffe || inBuffer.getChar(0)==0xfeff)
            {
                //Get the Specified Decoder
                decoder = charset.newDecoder();
                decoder.reset();
            }
            else
            {
                if(inBuffer.get(0)==0)
                {
                    decoder = StandardCharsets.UTF_16BE.newDecoder();
                    decoder.reset();
                }
                else
                {
                    decoder = StandardCharsets.UTF_16LE.newDecoder();
                    decoder.reset();
                }
            }
        }
        else
        {
            decoder = charset.newDecoder();
            decoder.reset();
        }
        return decoder;
    }

    /**
     * Get the text encoding being used.
     *
     * The text encoding is defined by the frame body that the text field belongs to.
     *
     * @return the text encoding charset
     */
    protected Charset getTextEncodingCharSet()
    {
        final byte textEncoding = this.getBody().getTextEncoding();
        final Charset charSetName = TextEncoding.getInstanceOf().getCharsetForId(textEncoding);
        return charSetName;
    }
}
