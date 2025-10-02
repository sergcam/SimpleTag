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

package dev.secam.simpletag.data.media

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.Options
import dev.secam.simpletag.data.coil.MusicDataKeyer
import dev.secam.simpletag.util.tag.simpleFileReader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import javax.inject.Inject

val COMPATIBLE_TYPES = listOf("mp3", "wav", "wave", "dsf", "aiff", "aif", "aifc", "wma", "ogg", "mp4", "m4a", "m4p", "flac", "aac")
const val JAUDIO_TIMEOUT = 10L
const val LOAD_FILES_TIMEOUT = 1000L * 60

const val TAG = "MediaRepo"
class MediaRepo @Inject constructor(private val context: Context) {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope = CoroutineScope(Dispatchers.Default).plus(coroutineExceptionHandler)
    val musicMapState = MutableStateFlow(mapOf<Long, MusicData>())

    suspend fun loadFiles(): String? {
        var log = "Loading files\n"
        try {
            withTimeout(LOAD_FILES_TIMEOUT) {
                val contentResolver = context.contentResolver
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.BITRATE,
                    MediaStore.Audio.Media.DURATION
                )
                val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
                val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
                log += "Entering content resolver\n"
                contentResolver.let { resolver ->
                    log += "Building cursor\n"
                    val cursor = resolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        sortOrder
                    )
                    log += "Cursor built\n"
                    val music = mutableMapOf<Long, MusicData>()
                    log += "Using cursor\n"
                    cursor?.use {
                        val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        log += "Found idColumn at: $idColumn\n"
                        val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        log += "Found titleColumn at: $titleColumn\n"
                        val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        log += "Found albumColumn at: $albumColumn\n"
                        val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        log += "Found artistColumn at: $artistColumn\n"
                        val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        log += "Found pathColumn at: $pathColumn\n"
                        val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                        log += "Found trackColumn at: $trackColumn\n"
                        val bitrateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
                        log += "Found bitrateColumn at: $bitrateColumn\n"
                        val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        log += "Found durationColumn at: $durationColumn\n"
                        // Read Music from MediaStore
                        val musicLoaders = mutableListOf<Deferred<Any>>()
                        log += "Iterating through mediastore\n"
                        while (it.moveToNext()) {
                            // get id and path from MediaStore metadata
                            val id = it.getLong(idColumn)
                            val path = it.getString(pathColumn)
                            val ext = path.substringAfterLast(".").lowercase()
                            val bitrate = it.getInt(bitrateColumn)
                            val duration = it.getInt(durationColumn)
                            val title = it.getString(titleColumn)
                            val album = it.getString(albumColumn)
                            val artist = it.getString(artistColumn)
                            val track = it.getInt(trackColumn) % 1000
                            // Use MediaStore metadata for mp3 and flac files
                            if (ext == "mp3" || ext == "flac") {
                                log += "$id: Using mediastore metadata for $path\n"
                                music.put(
                                    key = id,
                                    MusicData(
                                        id = id,
                                        path = path,
                                        title = title,
                                        artist = artist,
                                        album = album,
                                        hasArtwork = null,
                                        tagged = artist != "<unknown>",
                                        track = track,
                                        bitrate = bitrate,
                                        duration = duration
                                    )
                                )
                                log += "$id: imported $path\n"
                            }
                            // Use jaudiotagger for non-mp3/flac files (better compatibility / more accurate but slower)
                            else if (COMPATIBLE_TYPES.contains(ext)) {
                                log += "$id: Using jaudiotagger metadata for $path\n"
                                val loader = backgroundScope.async {
                                    try {
                                        withTimeout(JAUDIO_TIMEOUT) {
                                            log += "$id: Opening file\n"
                                            val file = simpleFileReader(path)
                                            log += "$id: File opened\n"
                                            log += "$id: Getting tag\n"
                                            val tag: Tag? = file.getTag()
                                            log += "$id: Got tag\n"
                                            var tagged = 0
                                            val jTitle: String
                                            val jArtist: String
                                            val jAlbum: String
                                            if (tag?.getFirst(FieldKey.TITLE) == "" || tag?.getFirst(
                                                    FieldKey.TITLE
                                                ) == null
                                            ) {
                                                jTitle = file.file.name
                                                tagged++
                                            } else {
                                                jTitle =
                                                    tag.getFirst(FieldKey.TITLE) ?: file.file.name
                                            }
                                            log += "$id: Got title\n"
                                            if (tag?.getFirst(FieldKey.ALBUM) == "" || tag?.getFirst(
                                                    FieldKey.ALBUM
                                                ) == null
                                            ) {
                                                jAlbum = "<unknown>"
                                                tagged++
                                            } else {
                                                jAlbum = tag.getFirst(FieldKey.ALBUM) ?: "<unknown>"
                                            }
                                            log += "$id: Got album\n"
                                            if (tag?.getFirst(FieldKey.ARTIST) == "" || tag?.getFirst(
                                                    FieldKey.ARTIST
                                                ) == null
                                            ) {
                                                jArtist = "<unknown>"
                                                tagged++
                                            } else {
                                                jArtist =
                                                    tag.getFirst(FieldKey.ARTIST) ?: "<unknown>"
                                            }
                                            log += "$id: Got artist\n"
                                            val hasArt = tag?.firstArtwork != null
                                            val track =
                                                if(tag?.getFirst(FieldKey.TRACK)?.isEmpty() == false) tag.getFirst(FieldKey.TRACK)?.toInt()
                                                else 0

                                            music.put(
                                                key = id,
                                                MusicData(
                                                    id = id,
                                                    path = path,
                                                    title = jTitle,
                                                    artist = jArtist,
                                                    album = jAlbum,
                                                    hasArtwork = hasArt,
                                                    track = track,
                                                    bitrate = bitrate,
                                                    duration = duration,
                                                    tagged = tagged == 0 // TODO: Fix this
                                                )
                                            )
                                            log += "$id: imported $path using jaudiotagger\n"
                                        }
                                    } catch (e: TimeoutCancellationException) {
                                        Log.d("MediaRepo", e.toString() + path.toString())
                                        log += "$id: Timed out. falling back to mediastore\n"
                                        music.put(
                                            key = id,
                                            MusicData(
                                                id = id,
                                                path = path,
                                                title = title,
                                                artist = artist,
                                                album = album,
                                                hasArtwork = null,
                                                track = track,
                                                tagged = artist != "<unknown>",
                                                bitrate = bitrate,
                                                duration = duration
                                            )
                                        )
                                        log += "$id: imported $path using mediastore\n"
                                    }
                                }
                                musicLoaders.add(loader)
                            }
                        }
                        log += "Waiting for all tasks to finish\n"
                        musicLoaders.awaitAll()
                        log += "Processing complete. Updating repo\n"
                        musicMapState.update { music }
                        log += "Repo Updated. Loading complete\n"
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            return log + e.toString()
        }
        Log.d(TAG, log)
        return null
    }
    suspend fun refreshMediaStore(musicList: List<MusicData>) {
        backgroundScope.async {
            val imageLoader = context.imageLoader
            val mimeTypes = arrayOf("audio/mp3", "audio/flac")
            val paths = musicList.map { data ->
                data.path
            }.toTypedArray()
            MediaScannerConnection.scanFile(context, paths, mimeTypes, null)
            val updatedMap = musicMapState.value.toMutableMap()
            for (song in musicList) {
                // remove old art from cache
                MusicDataKeyer.key(song, Options(context)).let { imageLoader.memoryCache?.remove(MemoryCache.Key(it)) }
                val file = simpleFileReader(song.path)
                val tag = file.tag
                var tagged = 0
                val title: String
                val artist: String
                val album: String
                if (tag?.getFirst(FieldKey.TITLE) == "" || tag?.getFirst(FieldKey.TITLE) == null) {
                    title = file.file.name.substringBeforeLast(".")
                    tagged++
                } else {
                    title = tag.getFirst(FieldKey.TITLE)
                }
                if (tag?.getFirst(FieldKey.ALBUM) == "" || tag?.getFirst(FieldKey.ALBUM) == null) {
                    album = if (file.ext == "mp3" || file.ext == "flac") {
                        song.path.substringBeforeLast("/").substringAfterLast("/")
                    } else {
                        "<unknown>"
                    }

                    tagged++
                } else {
                    album = tag.getFirst(FieldKey.ALBUM) ?: "<unknown>"
                }
                if (tag?.getFirst(FieldKey.ARTIST) == "" || tag?.getFirst(FieldKey.ARTIST) == null) {
                    artist = "<unknown>"
                    tagged++
                } else {
                    artist = tag.getFirst(FieldKey.ARTIST) ?: "<unknown>"
                }
                val hasArt = tag?.firstArtwork != null
                updatedMap[song.id] = MusicData(
                    id = song.id,
                    path = song.path,
                    title = title,
                    artist = artist,
                    album = album,
                    hasArtwork = hasArt,
                    track = song.track,
                    tagged = tagged == 0,
                    bitrate = song.bitrate,
                    duration = song.duration
                )
            }
            musicMapState.update { updatedMap }
        }.await()
    }
    suspend fun rescanMediaStore(scanListener: MediaScannerConnection.OnScanCompletedListener? = null) {
        backgroundScope.async{
            val paths = musicMapState.value.map { mapEntry ->
                mapEntry.value.path
            }.toTypedArray()
        MediaScannerConnection.scanFile(context, paths, null, scanListener)
        }.await()
    }
    suspend fun updateHasArt(id: Long){
        backgroundScope.async {
            val data = musicMapState.value[id]!!
            val file: AudioFile = simpleFileReader(data.path)
            val tag: Tag? = file.getTag()
            val hasArt = tag?.firstArtwork != null
            val newData = MusicData(
                id = id,
                path = data.path,
                title = data.title,
                artist = data.artist,
                album = data.album,
                tagged = data.tagged,
                hasArtwork = hasArt,
                track = data.track,
                bitrate = data.bitrate,
                duration = data.duration
            )
            musicMapState.update {
                musicMapState.value + Pair(id,newData)
            }
        }.await()
    }
}