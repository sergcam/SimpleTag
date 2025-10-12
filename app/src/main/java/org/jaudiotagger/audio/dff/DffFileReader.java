/*
 * Created on 03.05.2015
 * Author: Veselin Markov.
 */
package org.jaudiotagger.audio.dff;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidChunkException;
import org.jaudiotagger.audio.generic.AudioFileReader2;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.logging.Level;

public class DffFileReader extends AudioFileReader2
{
    @Override
    protected GenericAudioHeader getEncodingInfo(Path file) throws CannotReadException, IOException
    {
        try (FileChannel fc = FileChannel.open(file))
        {
            Frm8Chunk frm8 = Frm8Chunk.readChunk(Utils.readFileDataIntoBufferLE(fc, Frm8Chunk.FRM8_HEADER_LENGTH));
            if (frm8 != null)
            {

                DsdChunk dsd = DsdChunk.readChunk(Utils.readFileDataIntoBufferLE(fc, DsdChunk.DSD_HEADER_LENGTH));

                if (dsd == null)
                {
                    throw new CannotReadException(file + " Not a valid dff file. Missing 'DSD '  after 'FRM8' ");
                }
                PropChunk prop;
                for (; ; )
                {
                    prop = PropChunk.readChunk(Utils.readFileDataIntoBufferLE(fc, PropChunk.PROP_HEADER_LENGTH));
                    if (prop != null)
                    {
                        break;
                    }
                }

                if (prop == null)
                {

                    throw new CannotReadException(file + " Not a valid dff file. Content does not have 'PROP'");
                }

                SndChunk snd = SndChunk.readChunk(Utils.readFileDataIntoBufferLE(fc, SndChunk.SND_HEADER_LENGTH));
                if (snd == null)
                {
                    throw new CannotReadException(file + " Not a valid dff file. Missing 'SND '  after 'PROP' ");
                }

                BaseChunk chunk = null;
                FsChunk fs = null;
                ChnlChunk chnl = null;
                CmprChunk cmpr = null;
                DitiChunk diti = null;
                EndChunk end = null;
                DstChunk dst = null;
                FrteChunk frte = null;
                Id3Chunk id3 = null;

                for (; ; )
                {
                    try
                    {
                        chunk = BaseChunk.readIdChunk(Utils.readFileDataIntoBufferLE(fc, BaseChunk.ID_LENGHT));

                    }
                    catch (InvalidChunkException ex)
                    {

                        continue;
                    }

                    if (chunk instanceof FsChunk)
                    {
                        fs = (FsChunk) chunk;
                        fs.readDataChunch(fc);

                    }
                    else if (chunk instanceof ChnlChunk)
                    {
                        chnl = (ChnlChunk) chunk;
                        chnl.readDataChunch(fc);

                    }
                    else if (chunk instanceof CmprChunk)
                    {
                        cmpr = (CmprChunk) chunk;
                        cmpr.readDataChunch(fc);

                    }
                    else if (chunk instanceof DitiChunk)
                    {
                        diti = (DitiChunk) chunk;
                        diti.readDataChunch(fc);

                    }
                    else if (chunk instanceof EndChunk)
                    {
                        end = (EndChunk) chunk;
                        end.readDataChunch(fc);

                        break; //no more data after the end.

                    }
                    else if (chunk instanceof DstChunk)
                    {
                        dst = (DstChunk) chunk;
                        dst.readDataChunch(fc);

                        try
                        {

                            frte = (FrteChunk) BaseChunk.readIdChunk(Utils.readFileDataIntoBufferLE(fc, BaseChunk.ID_LENGHT));

                        }
                        catch (InvalidChunkException ex)
                        {

                            throw new CannotReadException(file + "Not a valid dft file. Missing 'FRTE' chunk");
                        }

                        if (frte != null)
                        {

                            frte.readDataChunch(fc);

                        }

                    }
                    else if (chunk instanceof Id3Chunk)
                    {
                        id3 = (Id3Chunk) chunk;
                        id3.readDataChunch(fc);


                    }

                } //end for

                if (chnl == null)
                {
                    throw new CannotReadException(file + " Not a valid dff file. Missing 'CHNL' chunk");
                }
                if (fs == null)
                {
                    throw new CannotReadException(file + " Not a valid dff file. Missing 'FS' chunk");
                }
                if (dst != null && frte == null)
                {
                    throw new CannotReadException(file + " Not a valid dst file. Missing 'FRTE' chunk");
                }
                if (end == null && dst == null)
                {
                    throw new CannotReadException(file + " Not a valid dff file. Missing 'DSD' end chunk");
                }

                int bitsPerSample = 1;
                int channelNumber = chnl.getNumChannels();
                int samplingFreqency = fs.getSampleRate();
                long sampleCount;

                if (dst != null)
                {

                    sampleCount = frte.getNumFrames() / frte.getRate()
                            * samplingFreqency;

                }
                else
                {

                    sampleCount = (end.getDataEnd() - end.getDataStart())
                            * (8 / channelNumber);

                }

                return buildAudioHeader(channelNumber, samplingFreqency, sampleCount, bitsPerSample, (dst != null));

            }
            else
            {
                throw new CannotReadException(file + " Not a valid dff file. Content does not start with 'FRM8'");

            } //end if frm8

        } // end try.

    }

    private GenericAudioHeader buildAudioHeader(int channelNumber, int samplingFreqency, long sampleCount, int bitsPerSample, boolean isDST)
    {
        GenericAudioHeader audioHeader = new GenericAudioHeader();

        audioHeader.setEncodingType("DFF");
        audioHeader.setBitRate(bitsPerSample * samplingFreqency * channelNumber);
        audioHeader.setBitsPerSample(bitsPerSample);
        audioHeader.setChannelNumber(channelNumber);
        audioHeader.setSamplingRate(samplingFreqency);
        audioHeader.setNoOfSamples(sampleCount);
        audioHeader.setPreciseLength((float) sampleCount / samplingFreqency);
        audioHeader.setVariableBitRate(isDST);

        logger.log(Level.FINE, "Created audio header: " + audioHeader);
        return audioHeader;
    }

    @Override
    protected Tag getTag(Path path) throws CannotReadException, IOException
    {
        return null;
    }

}
