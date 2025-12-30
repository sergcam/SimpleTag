package org.jaudiotagger.tag.images;

//import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Image Handler
 */
public interface ImageHandler
{
    public void reduceQuality(Artwork artwork, int maxSize) throws IOException;
    public void makeSmaller(Artwork artwork,int size) throws IOException;
    public boolean isMimeTypeWritable(String mimeType);
//    public byte[] writeImage(BufferedImage bi,String mimeType) throws IOException;
//    public byte[] writeImageAsPng(BufferedImage bi) throws IOException;
    public void showReadFormats();
    public void showWriteFormats();
}
