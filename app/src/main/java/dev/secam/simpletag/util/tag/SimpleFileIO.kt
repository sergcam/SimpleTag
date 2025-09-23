/*
 * Copyright (C) 2025  Sergio Camacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.secam.simpletag.util.tag

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
    return when {
        path.endsWith("aac", true) ->
            Mp4FileReader().read(File(path))

        else ->
            AudioFileIO.read(File(path))
    }
}

/**
 * for ogg/vorbis use [oggFileWriter] instead
 */
fun simpleFileWriter(file: AudioFile) {
    when {
        file.file.path.endsWith("aac", true) ->
            Mp4FileWriter().write(file)
        else ->
            AudioFileIO.write(file)
    }
}

/**
 * need to use this to write ogg as jaudiotagger tries to create temp files which it doesn't have permissions for
 */
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