package dev.secam.simpletag.util

import android.content.Context
import android.util.Log
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp4.Mp4FileReader
import org.jaudiotagger.audio.mp4.Mp4FileWriter
import org.jaudiotagger.audio.ogg.OggFileReader
import org.jaudiotagger.audio.ogg.OggFileWriter
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
        else ->
            AudioFileIO.write(file)
    }
}

fun oggFileWriter(file: AudioFile, context: Context){
    val tempFile = File(context.filesDir, "ogg_temp")
    tempFile.writeBytes(file.file.readBytes())
    val tempAF = OggFileReader().read(tempFile)
    tempAF.tag = file.tag
    Log.d("oggWriter", tempAF.toString())
    OggFileWriter().write(tempAF)
    file.file.writeBytes(tempFile.readBytes())
    tempFile.delete()
}