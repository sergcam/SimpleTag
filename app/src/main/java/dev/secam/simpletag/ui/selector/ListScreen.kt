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

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.enums.SortOrder
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.editor.dialogs.LogDialog
import dev.secam.simpletag.ui.selector.components.SimpleAlbumItem
import dev.secam.simpletag.ui.selector.components.SimpleMusicItem
import dev.secam.simpletag.ui.selector.dialogs.FilterDialog
import dev.secam.simpletag.ui.selector.dialogs.SortDialog
import dev.secam.simpletag.util.animateExpandTopBar
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    onNavigateToEditor: (List<MusicData>) -> Unit,
    viewModel: SelectorViewModel,
    lazyListState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // ui state
    val uiState = viewModel.uiState.collectAsState().value
    val musicList = uiState.musicList
    val sortOrder = uiState.sortOrder
    val sortDirection = uiState.sortDirection
    val taggedFilter = uiState.taggedFilter
    val showSortDialog = uiState.showSortDialog
    val showFilterDialog = uiState.showFilterDialog
    val filesLoaded = uiState.filesLoaded
    val isRefreshing = uiState.isRefreshing
    val showLogDialog = uiState.showLogDialog
    val albumList = uiState.albumList
    val albumMap = uiState.albumMap
    val log = uiState.log
    val multiSelectEnabled = uiState.multiSelectEnabled
    val selectedItems = uiState.selectedItems



    if (!filesLoaded) {
        viewModel.loadList(
            snackbarHostState = snackbarHostState,
            message = stringResource(R.string.list_screen_error),
            actionLabel = stringResource(R.string.log)
        )
    }
    val onSongClick = { item: MusicData ->
        if (multiSelectEnabled) {
            if (selectedItems.contains(item)) {
                viewModel.removeSelection(item)
            } else {
                viewModel.addSelection(item)
            }
        } else {
            onNavigateToEditor(listOf(item))
        }
    }
    val onLongClick = { item: MusicData ->
        if (multiSelectEnabled) {
            onSongClick(item)
        } else {
            scope.launch { scrollBehavior.animateExpandTopBar(tween(250)) }
            viewModel.setMultiSelectedEnabled(true)
            viewModel.addSelection(item)
        }
    }
    val onAlbumLongClick = { musicList: List<MusicData> ->
        if (!multiSelectEnabled) {
            scope.launch { scrollBehavior.animateExpandTopBar(tween(250)) }
            viewModel.setMultiSelectedEnabled(true)
        }
        if (selectedItems.containsAll(musicList)) {
            for (song in musicList){
                viewModel.removeSelection(song)
            }
        } else {
            for (song in musicList){
                viewModel.addSelection(song)
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        //  Loading bar while loading music
        if (!filesLoaded) {
            LoadingScreen(
                text = stringResource(R.string.loading_music),
                subtext = stringResource(R.string.loading_music_long)
            )
        }
        //  if loaded and list is empty
        else if (musicList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_results),
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }
        // File List
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    viewModel.setIsRefreshing(true)
                    viewModel.refreshMediaStore()
                },
            ) {
                if (sortOrder == SortOrder.Album) {
                    if (albumList.isEmpty() && !musicList.isEmpty()) {
                        viewModel.setAlbumList()
                    }
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(
                            count = albumList.size,
                            key = null,//{ musicList[it].id },
                            contentType = { String }
                        ) { index ->
                            if (viewModel.hasArtwork(albumList[index].first) == null) {
                                viewModel.updateHasArt(index, true)
                            }
                            if (albumMap[albumList[index].first] != null) {
                                SimpleAlbumItem(
                                    musicList = albumMap[albumList[index].first]!!,
                                    onSongClick = onSongClick,
                                    expanded = albumList[index].second,
                                    selectedSet = selectedItems,
                                    onClick = { viewModel.expandAlbum(index) },
                                    onSongLongClick = onLongClick,
                                    onLongClick = onAlbumLongClick,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(
                            count = musicList.size,
                            key = null,//{ musicList[it].id },
                            contentType = { MusicData }
                        ) { index ->

                            if (musicList[index].hasArtwork == null) {
                                viewModel.updateHasArt(index)
                            }
                            SimpleMusicItem(
                                musicData = musicList[index],
                                onLongClick = { onLongClick(musicList[index]) },
                                selected = selectedItems.contains(musicList[index]),
                                onClick = { onSongClick(musicList[index]) },
                            )
                        }
                    }
                }
            }
        }
    }

    //  Show Dialogs
    if (showFilterDialog) {
        FilterDialog(
            hasTag = taggedFilter,
            onConfirm = viewModel::setTaggedFilter
        ) { viewModel.setShowFilterDialog(false) }
    }
    if (showSortDialog) {
        SortDialog(
            sortOrder = sortOrder,
            sortDirection = sortDirection,
            onConfirm = viewModel::setSort
        ) { viewModel.setShowSortDialog(false) }
    }
    if (showLogDialog) {
        LogDialog(log) { viewModel.setShowLogDialog(false) }
    }
}