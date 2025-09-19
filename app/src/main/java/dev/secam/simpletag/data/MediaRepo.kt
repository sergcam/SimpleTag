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

package dev.secam.simpletag.data

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.Options
import dev.secam.simpletag.data.coil.MusicDataKeyer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import javax.inject.Inject

class MediaRepo @Inject constructor(private val context: Context) {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope = CoroutineScope(Dispatchers.Default).plus(coroutineExceptionHandler)
    val musicMapState = MutableStateFlow(mapOf<Long, MusicData>())

    suspend fun loadFiles() {
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.let { resolver ->
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            val music = mutableMapOf<Long, MusicData>()
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                // Read Music from MediaStore
                val musicLoaders = mutableListOf<Deferred<MusicData?>>()
                while (it.moveToNext()) {
                    // get id and path from MediaStore metadata
                    val id = it.getLong(idColumn)
                    val path = it.getString(pathColumn)
                    val ext = path.substringAfterLast(".").lowercase()
                    // Use MediaStore metadata for mp3 and flac files
                    if (ext == "mp3" || ext == "flac") {
                        val title = it.getString(titleColumn)
                        val album = it.getString(albumColumn)
                        val artist = it.getString(artistColumn)
                        music.put(
                            key = id,
                            MusicData(
                                id = id,
                                path = path,
                                title = title,
                                artist = artist,
                                album = album,
                                hasArtwork = null,
                                tagged = artist != "<unknown>"
                            )
                        )
                    }
                    // Use jaudiotagger for non-mp3/flac files (better compatibility / more accurate but slower)
                    else {
                        val loader = backgroundScope.async {
                            val file: AudioFile = AudioFileIO.read(File(path))
                            val tag: Tag? = file.getTag()
                            var tagged = 0
                            val title: String
                            val artist: String
                            val album: String
                            if (tag?.getFirst(FieldKey.TITLE) == "" || tag?.getFirst(FieldKey.TITLE) == null) {
                                title = file.file.name
                                tagged++
                            } else {
                                title = tag.getFirst(FieldKey.TITLE) ?: file.file.name
                            }
                            if (tag?.getFirst(FieldKey.ALBUM) == "" || tag?.getFirst(FieldKey.ALBUM) == null) {
                                album = "<unknown>"
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
                            music.put(
                                key = id,
                                MusicData(
                                    id = id,
                                    path = path,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    hasArtwork = hasArt,
                                    tagged = tagged == 0 // TODO: Fix this
                                )
                            )
                        }
                        musicLoaders.add(loader)
                    }
                }
                musicLoaders.awaitAll()
                musicMapState.update { music }
            }
        }
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
                val file = AudioFileIO.read(File(song.path))
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
                    tagged = tagged == 0
                )
            }
            musicMapState.update { updatedMap }
        }.await()
    }
    suspend fun rescanMediaStore(scanListener: MediaScannerConnection.OnScanCompletedListener? = null) {
        backgroundScope.async{
            val mimeTypes = arrayOf("audio/mp3", "audio/flac")
            val paths = musicMapState.value.map { mapEntry ->
                mapEntry.value.path
            }.toTypedArray()
        MediaScannerConnection.scanFile(context, paths, mimeTypes, scanListener)
        }.await()
//        loadFiles()
    }
    suspend fun updateHasArt(id: Long){
        backgroundScope.async {
            val data = musicMapState.value[id]!!
            val file: AudioFile = AudioFileIO.read(File(data.path))
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
            )
            musicMapState.update {
                musicMapState.value + Pair(id,newData)
            }
        }.await()
    }
}