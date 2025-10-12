package org.jaudiotagger.audio.iff;

public class BadChunkSummary extends ChunkSummary
{
    public BadChunkSummary(long fileStartLocation, long chunkSize)
    {
        super("BAD-DATA", fileStartLocation, chunkSize);
    }
}
