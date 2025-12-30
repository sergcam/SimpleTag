package org.jaudiotagger.audio.wav.chunk;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.iff.ChunkSummary;
import org.jaudiotagger.audio.wav.WavChunkType;
import org.jaudiotagger.tag.wav.WavTag;

import java.util.List;
import java.util.logging.Logger;

/**
 * AIFF Specific methods for ChunkSummarys
 */
public class WavChunkSummary
{
    // Logger Object
    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.wav.chunk");

    /**
     * Get start location in file of first metadata chunk (could be LIST or ID3)
     *
     * @param tag
     * @return
     */
    public static long getStartLocationOfFirstMetadataChunk(WavTag tag)
    {
        //Work out the location of the first metadata tag (could be id3 or LIST tag)
        if(tag.getMetadataChunkSummaryList().size()>0)
        {
            return tag.getMetadataChunkSummaryList().get(0).getFileStartLocation();
        }
        return -1;
    }

    /**
     * Checks that there are only metadata tags after the currently selected metadata because this means its safe to truncate
     * the remainder of the file.
     *
     * @param tag
     * @return
     */
    public static boolean isOnlyMetadataTagsAfterStartingMetadataTag(WavTag tag)
    {
        long startLocationOfMetadatTag = getStartLocationOfFirstMetadataChunk(tag);
        if(startLocationOfMetadatTag==-1)
        {
            logger.severe("Unable to find any metadata tags !");
            return false;
        }

        boolean firstMetadataTag = false;

        for(ChunkSummary cs:tag.getChunkSummaryList())
        {
            //Once we have found first metadata chunk we check all other chunks afterwards and if they are
            //only metadata chunks we can truncate file at start of this metadata chunk
            if(firstMetadataTag)
            {
                if(
                        !cs.getChunkId().equals(WavChunkType.ID3.getCode()) &&
                        !cs.getChunkId().equals(WavChunkType.ID3_UPPERCASE.getCode()) &&
                        !cs.getChunkId().equals(WavChunkType.LIST.getCode()) &&
                        !cs.getChunkId().equals(WavChunkType.INFO.getCode())
                  )
                {
                    return false;
                }
            }
            else
            {
                //Found the first metadata chunk
                if (cs.getFileStartLocation() == startLocationOfMetadatTag)
                {
                    //Found starting point
                    firstMetadataTag = true;
                }
            }
        }

        //Should always be true but this is to protect against something gone wrong
        if(firstMetadataTag==true)
        {
            return true;
        }
        return false;

    }


    /**
     * Get chunk before the first metadata tag
     *
     * @param tag
     * @return
     */
    public static ChunkSummary getChunkBeforeFirstMetadataTag(WavTag tag)
    {
        long startLocationOfMetadatTag = getStartLocationOfFirstMetadataChunk(tag);

        for(int i=0;i < tag.getChunkSummaryList().size(); i++)
        {
            ChunkSummary cs = tag.getChunkSummaryList().get(i);
            if (cs.getFileStartLocation() == startLocationOfMetadatTag)
            {
                return tag.getChunkSummaryList().get(i - 1);
            }
        }
        return null;
    }
}
