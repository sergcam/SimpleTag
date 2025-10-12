package org.jaudiotagger.audio.dff;

import java.util.HashMap;
import java.util.Map;

/**
 * Chunk types mark each {@link org.jaudiotagger.audio.iff.ChunkHeader}. They are <em>always</em> 4 ASCII chars long.
 *
 * @see org.jaudiotagger.audio.iff.Chunk
 */
public enum DffChunkType
{
    FRM8("FRM8"),
    DSD("DSD "),
    PROP("PROP"),
    SND("SND "),
    FS("FS  "),
    CHNL("CHNL"),
    CMPR("CMPR"),
    DITI("DITI"),
    END("DSD "),
    DST("DST "),
    FRTE("FRTE"),
    ID3("ID3 "),
    DATA("data"),;

    private static final Map<String, DffChunkType> CODE_TYPE_MAP = new HashMap<String, DffChunkType>();
    private String code;

    /**
     * @param code 4 char string
     */
    DffChunkType(final String code)
    {
        this.code = code;
    }

    /**
     * Get {@link org.jaudiotagger.audio.dsf.DsfChunkType} for code (e.g. "SSND").
     *
     * @param code chunk id
     * @return chunk type or {@code null} if not registered
     */
    public synchronized static DffChunkType get(final String code)
    {
        if (CODE_TYPE_MAP.isEmpty())
        {
            for (final DffChunkType type : values())
            {
                CODE_TYPE_MAP.put(type.getCode(), type);
            }
        }
        return CODE_TYPE_MAP.get(code);
    }

    /**
     * 4 char type code.
     *
     * @return 4 char type code, e.g. "SSND" for the sound chunk.
     */
    public String getCode()
    {
        return code;
    }
}
