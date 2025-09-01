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

package dev.secam.simpletag.ui.selector

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.secam.simpletag.data.MusicData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SelectorViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(SelectorUiState())
    val uiState = _uiState.asStateFlow()
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope = viewModelScope.plus(Dispatchers.Default + coroutineExceptionHandler)

    fun loadFiles(contentResolver: ContentResolver) {
        backgroundScope.launch {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            contentResolver.let { cosRes ->
                val cursor = cosRes.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )
                val music = mutableListOf<MusicData>()

                cursor?.use {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val displayNameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (it.moveToNext()) {
                        // get this info from MediaStore metadata
                        val id = it.getLong(idColumn)
                        val path = it.getString(pathColumn)
                        // Use MediaStore metadata for mp3 files
                        if (path.substring(path.length - 3, path.length).lowercase() == "mp3") {
                            val title = if(it.getString(titleColumn) == "") it.getString(displayNameColumn) else it.getString(titleColumn)
                            val album = if(it.getString(albumColumn) == "") "<unknown>" else it.getString(albumColumn)
                            val artist = if(it.getString(artistColumn) == "") "<unknown>" else it.getString(artistColumn)
                            music.add(
                                MusicData(
                                    id = id,
                                    path = path,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                )
                            )
                        }
                        // Use jaudiotagger for non-mp3 files (better compatibility but slower)
                        else {
                            val file: AudioFile = AudioFileIO.read(File(path))
                            val tag: Tag? = file.getTag()
                            val title = if (tag?.getFirst(FieldKey.TITLE) != "") {
                                tag?.getFirst(FieldKey.TITLE) ?: file.file.name
                            } else {
                                file.file.name
                            }
                            val album = if (tag?.getFirst(FieldKey.ALBUM) != "") {
                                tag?.getFirst(FieldKey.ALBUM) ?: "<unknown>"
                            } else {
                                "<unknown>"
                            }
                            val artist = if (tag?.getFirst(FieldKey.ARTIST) != "") {
                                tag?.getFirst(FieldKey.ARTIST) ?: "<unknown>"
                            } else {
                                "<unknown>"
                            }
                            val hasArt = tag?.firstArtwork != null
                            music.add(
                                MusicData(
                                    id = id,
                                    path = path,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    hasArtwork = hasArt
                                )
                            )
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        musicList = music
                    )
                }
            }
        }
    }

    fun updateHasArt(index: Int){
        val path = uiState.value.musicList[index].path
        backgroundScope.launch {
            val file: AudioFile = AudioFileIO.read(File(path))
            val tag: Tag? = file.getTag()
            val hasArt = tag?.firstArtwork != null
            _uiState.update { currentState ->
                val newList = uiState.value.musicList.toMutableList()
                newList[index] = MusicData(
                    id = newList[index].id,
                    path = path,
                    title = newList[index].title,
                    artist = newList[index].artist,
                    album = newList[index].album,
                    hasArtwork = hasArt
                )
                currentState.copy(
                    musicList = newList
                )
            }
        }
    }
}

data class SelectorUiState(
    val musicList : List<MusicData> = listOf()
)