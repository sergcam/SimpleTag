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

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.secam.simpletag.data.enums.SortDirection
import dev.secam.simpletag.data.enums.SortOrder
import dev.secam.simpletag.data.media.MediaRepo
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.data.preferences.PreferencesRepo
import dev.secam.simpletag.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class SelectorViewModel @Inject constructor(
    private val preferencesRepo: PreferencesRepo,
    private val mediaRepo: MediaRepo
): ViewModel() {
    private val _uiState = MutableStateFlow(SelectorUiState())
    val uiState = _uiState.asStateFlow()
    val prefState = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )
    val musicMapState = mediaRepo.musicMapState
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope = viewModelScope.plus(Dispatchers.Default + coroutineExceptionHandler)

    fun loadList(snackbarHostState: SnackbarHostState, message: String, actionLabel: String) {
        backgroundScope.launch {
            val result = mediaRepo.loadFiles()
            if(result == null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        musicList = sortList(
                            musicMapState.value.map { entry ->
                                entry.value
                            },
                        ),
                        filesLoaded = true
                    )
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        musicList = sortList(
                            musicMapState.value.map { entry ->
                                entry.value
                            },
                        ),
                        filesLoaded = true,
                        log = result
                    )
                }
                if (snackbarHostState.showSnackbar(message, actionLabel, duration = SnackbarDuration.Long) == SnackbarResult.ActionPerformed)
                    setShowLogDialog(true)
            }
        }
    }
    suspend fun suspendLoadList() {
        mediaRepo.loadFiles()
        _uiState.update { currentState ->
            currentState.copy(
                musicList = sortList(
                    musicMapState.value.map { entry ->
                        entry.value
                    }
                ),
                filesLoaded = true
            )
        }
    }
    fun hasArtwork(album: String): Boolean?{
        return uiState.value.albumMap[album]?.get(0)?.hasArtwork
    }
    fun updateMusicList() {
        backgroundScope.launch {
            if(uiState.value.sortOrder == SortOrder.Album){
                val newList: MutableList<Pair<String, Boolean>> = mutableListOf()

                //  Check query and filter
                val scanners = mutableListOf<Deferred<Boolean>>()
                for (album in uiState.value.albumMap) {
                    val scanner = backgroundScope.async {
                        if (matchesQuery(album.key)) {
                            newList.add(Pair(album.key,false))
                        } else false
                    }
                    scanners.add(scanner)
                }
                scanners.awaitAll()

                //  Sort files
                newList.sortBy { it.first }
                if (uiState.value.sortDirection == SortDirection.Descending) {
                    newList.reverse()
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        albumList = newList
                    )
                }
            } else {
                val musicList = musicMapState.value.map { entry ->
                    entry.value
                }
                val newList = mutableListOf<MusicData>()

                //  Check query and filter
                val scanners = mutableListOf<Deferred<Boolean>>()
                for (song in musicList) {
                    val scanner = backgroundScope.async {
                        if (matchesQuery(song)) {
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
                    when (uiState.value.sortOrder) {
                        SortOrder.Album ->
                            selector.album.lowercase()

                        SortOrder.Title ->
                            selector.title.lowercase()

                        SortOrder.Artist ->
                            selector.artist.lowercase()
//                    SortOrder.DateAdded ->
//                        selector.artist?.lowercase()
                    }
                }
                if (uiState.value.sortDirection == SortDirection.Descending) {
                    newList.reverse()
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        musicList = newList
                    )
                }
            }
        }
    }

    fun sortList(musicList: List<MusicData>): List<MusicData> {
        val newList = musicList.toMutableList()
        newList.sortBy { selector ->
            when(uiState.value.sortOrder){
                SortOrder.Album ->
                    selector.album.lowercase()
                SortOrder.Title ->
                    selector.title.lowercase()
                SortOrder.Artist ->
                    selector.artist.lowercase()
//                    SortOrder.DateAdded ->
//                        selector.artist?.lowercase()
            }
        }
        if(uiState.value.sortDirection == SortDirection.Descending){
            newList.reverse()
        }
        return newList as List<MusicData>
    }
    fun matchesQuery(song: MusicData): Boolean {
        val query = uiState.value.searchQuery
        return when {
            query.isEmpty() ->
                true
            song.title.contains(query, true) ->
                true

            song.artist.contains(query, true) ->
                true

            song.album.contains(query, true) ->
                true
            else ->
                false
        }
    }

    fun matchesQuery(text: String): Boolean {
        val query = uiState.value.searchQuery
        return when {
            query.isEmpty() ->
                true
            text.contains(query, true) ->
                true
            else ->
                false
        }
    }
    fun expandAlbum(index: Int) {
        val newList = uiState.value.albumList.toMutableList()
        newList[index] = Pair(newList[index].first, !newList[index].second)
        _uiState.update { it.copy( albumList = newList) }
    }
    fun updateHasArt(index: Int, isAlbum: Boolean = false) {
        backgroundScope.launch {
            if (isAlbum) {
                val id = uiState.value.albumMap[uiState.value.albumList[index].first]!![0].id
                mediaRepo.updateHasArt(id)
                val newList = uiState.value.albumMap[uiState.value.albumList[index].first]!!.toMutableList()
                newList[0] = musicMapState.value[id]!!
                _uiState.update { currentState ->
                    currentState.copy(
                        albumMap = uiState.value.albumMap + Pair(uiState.value.albumList[index].first, newList)
                    )
                }
            } else {
                val id = uiState.value.musicList[index].id
                mediaRepo.updateHasArt(id)
                _uiState.update { currentState ->
                    val newList = uiState.value.musicList.toMutableList()
                    newList[index] = musicMapState.value[id]!!
                    currentState.copy(
                        musicList = newList
                    )
                }
            }
        }
    }

    fun refreshMediaStore() {
        backgroundScope.launch{
//            var count = 0
//            mediaRepo.rescanMediaStore {path, uri ->
//                count++
//                if(count == musicMapState.value.size){
//                    backgroundScope.launch {
                        suspendLoadList()
                        updateMusicList()
                        _uiState.update { it.copy(isRefreshing = false) }
//                    }
//                }
//            }
        }
    }

    fun setAlbumList() {
        backgroundScope.launch {
            val albumMap = mutableMapOf<String, List<MusicData>>()
            for (song in uiState.value.musicList) {
                if (albumMap[song.album] == null) {
                    albumMap[song.album] = listOf(song)
                } else {
                    albumMap[song.album] = albumMap[song.album]!! + song
                }
            }
            _uiState.update { currentState ->
                currentState.copy(
                    albumMap = albumMap,
                    albumList = albumMap.map { Pair(it.key,false) }
                )
            }
        }
    }

    /*------- Setters -------*/
    fun setTaggedFilter(taggedFilter: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                taggedFilter = taggedFilter
            )
        }
        updateMusicList()
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
        updateMusicList()
    }
    fun setIsRefreshing(isRefreshing: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                isRefreshing = isRefreshing
            )
        }
    }
    fun setSearchQuery(searchQuery: String){
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = searchQuery
            )
        }
        updateMusicList()
    }
    fun setSearchEnabled(searchEnabled: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                searchEnabled = searchEnabled
            )
        }
        setSearchQuery("")
    }
    fun setOptionalPermissionsSkipped(optionalPermissionsSkipped: Boolean){
        viewModelScope.launch {
            preferencesRepo.saveOptionalPermissionsSkipped(optionalPermissionsSkipped)
        }
    }
    fun setShowLogDialog( showLogDialog: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showLogDialog = showLogDialog
            )
        }
    }

}

data class SelectorUiState(
    val musicList: List<MusicData> = listOf(),
    val showSortDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val taggedFilter: Boolean = false, //true means only show non-tagged
    val sortOrder: SortOrder = SortOrder.Title,
    val sortDirection: SortDirection = SortDirection.Ascending,
    val filesLoaded: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val searchEnabled: Boolean = false,
    val log: String = "",
    val showLogDialog: Boolean = false,
    val albumMap: Map<String, List<MusicData>> = mapOf(),
    val albumList: List<Pair<String, Boolean>> = listOf()
)