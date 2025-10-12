package org.jaudiotagger.audio.wav.chunk;

/**
 * Chunk types incorrectly aligned, we can work round these, the 4th char either leading or ending is not known
 *
 * @see org.jaudiotagger.audio.iff.Chunk
 */
public enum WavCorruptChunkType
{
    CORRUPT_ID3_EARLY("id3"),
    CORRUPT_ID3_LATE("d3 "),
    CORRUPT_LIST_EARLY("LIS"),
    CORRUPT_LIST_LATE("IST"),
    ;

    private String code;


    WavCorruptChunkType(final String code)
    {
        this.code=code;
    }


    public String getCode()
    {
        return code;
    }
}
