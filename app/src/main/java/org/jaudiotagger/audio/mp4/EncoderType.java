package org.jaudiotagger.audio.mp4;

/**
 * Encoder Type actually identifies the format of the audio within the mp4. This is because
 * mp4 container can be used to hold different types of files.
 */
public enum EncoderType
{
    AAC("Aac"),
    DRM_AAC("Aac (Drm)"),
    APPLE_LOSSLESS("Alac"),;

    private String description;

    EncoderType(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
