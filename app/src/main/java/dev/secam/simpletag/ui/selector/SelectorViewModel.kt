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
import dev.secam.simpletag.data.SortOrder
import dev.secam.simpletag.data.SortDirection
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.Deferred
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import java.io.File
import javax.inject.Inject
import kotlin.collections.sortBy

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

                        // Use MediaStore metadata for mp3 files
                        if (path.substring(path.length - 3, path.length).lowercase() == "mp3") {
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
                        // Use jaudiotagger for non-mp3 files (better compatibility / more accurate but slower)
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
                                        tagged = tagged == 0
                                    )
                                )
                            }
                            musicLoaders.add(loader)
                        }
                    }
                    musicLoaders.awaitAll()
                    _uiState.value = _uiState.value.copy(
                        musicMap = music,
                        filteredMusic = music.map { entry ->
                            entry.value
                        }.sortedBy { selector ->
                            selector.title?.lowercase()
                        },
                        filesLoaded = true
                    )
                }
            }
        }
    }

    fun updateMusicList(query: String) {
        backgroundScope.launch {
            val musicList = uiState.value.musicMap.map{ entry ->
                entry.value
            }
            val newList = mutableListOf<MusicData>()

            //  Check query and filter
            val scanners = mutableListOf<Deferred<Boolean>>()
            for (song in musicList) {
                val scanner = backgroundScope.async {
                    if (matchesQuery(song, query)) {
                        if (uiState.value.taggedFilter) {
                            if (!song.tagged) {
                                newList.add(song)
                            } else false
                        } else {
                            newList.add(song)
                        }
                    } else false
                }
                scanners.add(scanner)
            }
            scanners.awaitAll()

            //  Sort files
            newList.sortBy { selector ->
                when(uiState.value.sortOrder){
                    SortOrder.Album ->
                        selector.album?.lowercase()
                    SortOrder.Title ->
                        selector.title?.lowercase()
                    SortOrder.Artist ->
                        selector.artist?.lowercase()
                    SortOrder.DateAdded ->
                        selector.artist?.lowercase()
                }
            }
            if(uiState.value.sortDirection == SortDirection.Descending){
                newList.reverse()
            }
            _uiState.update { currentState ->
                currentState.copy(
                    filteredMusic = newList
                )
            }
        }
    }

    fun matchesQuery(song: MusicData, query: String): Boolean {
        return when {
            query.isEmpty() ->
                true
            song.title?.contains(query, true) ?: false ->
                true

            song.artist?.contains(query, true) ?: false ->
                true

            song.album?.contains(query, true) ?: false ->
                true
            else ->
                false
        }
    }

    fun updateHasArt(index: Int){
        val path = uiState.value.filteredMusic[index].path
        backgroundScope.launch {
            val file: AudioFile = AudioFileIO.read(File(path))
            val tag: Tag? = file.getTag()
            val hasArt = tag?.firstArtwork != null
            _uiState.update { currentState ->
                val newList = uiState.value.filteredMusic.toMutableList()
                newList[index] = MusicData(
                    id = newList[index].id,
                    path = path,
                    title = newList[index].title,
                    artist = newList[index].artist,
                    album = newList[index].album,
                    hasArtwork = hasArt,
                    tagged = newList[index].tagged
                )
                currentState.copy(
                    musicMap = uiState.value.musicMap + Pair(newList[index].id,newList[index]),
                    filteredMusic = newList
                )
            }
        }
    }

    //  Setters
    fun setTaggedFilter(taggedFilter: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                taggedFilter = taggedFilter
            )
        }
    }
    fun setShowSortDialog(showSortDialog: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                showSortDialog = showSortDialog
            )
        }
    }
    fun setShowFilterDialog(showFilterDialog: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                showFilterDialog = showFilterDialog
            )
        }
    }
    fun setSort(sortOrder: SortOrder, sortDirection: SortDirection){
        _uiState.update { currentState ->
            currentState.copy(
                sortOrder = sortOrder,
                sortDirection = sortDirection
            )
        }
    }
}

data class SelectorUiState(
    val musicMap: Map<Long,MusicData> = mapOf(),
    val filteredMusic: List<MusicData> = listOf(),
    val showSortDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val taggedFilter: Boolean = false, //true means only show non-tagged
    val sortOrder: SortOrder = SortOrder.Title,
    val sortDirection: SortDirection = SortDirection.Ascending,
    val filesLoaded: Boolean = false
)