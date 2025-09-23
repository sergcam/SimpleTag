package dev.secam.simpletag.util

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp4.Mp4FileReader
import org.jaudiotagger.audio.mp4.Mp4FileWriter
import java.io.File

fun simpleFileReader(path: String): AudioFile {
//    if(path.endsWith("aac", true))
    return when {
        path.endsWith("aac", true) ->
            Mp4FileReader().read(File(path))

        else ->
            AudioFileIO.read(File(path))
    }
}

fun simpleFileWriter(file: AudioFile) {
    when {
        file.file.path.endsWith("aac", true) ->
            Mp4FileWriter().write(file)

//        file.file.path.endsWith("ogg", true) ->
//            OggFileWriter().write(file as Og)

        else ->
            AudioFileIO.write(file)
    }
}