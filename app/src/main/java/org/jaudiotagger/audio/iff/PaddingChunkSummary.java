package org.jaudiotagger.audio.iff;

public class PaddingChunkSummary extends ChunkSummary
{
    public PaddingChunkSummary(long fileStartLocation, long chunkSize)
    {
        super("    ", fileStartLocation, chunkSize);
    }
}
