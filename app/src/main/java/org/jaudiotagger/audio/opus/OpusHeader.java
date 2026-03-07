package org.jaudiotagger.audio.opus;

/**
 * Defines variables common to all vorbis headers
 */
public interface OpusHeader
{
    //Capture pattern at start of header
    String CAPTURE_PATTERN = "OpusTags";
    String ID_CAPTURE_PATTERN = "OpusHead";

    byte[] CAPTURE_PATTERN_AS_BYTES = {'O', 'p', 'u', 's', 'T', 'a', 'g', 's'};

    int FIELD_CAPTURE_PATTERN_POS = 0;
    int FIELD_CAPTURE_PATTERN_LENGTH = 8;
}