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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.selector.components.ListScreenTopBar
import dev.secam.simpletag.ui.selector.components.SimpleMusicItem
import dev.secam.simpletag.ui.selector.dialogs.FilterDialog
import dev.secam.simpletag.ui.selector.dialogs.SortDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    onNavigateToEditor: (List<MusicData>) -> Unit,
    viewModel: SelectorViewModel,
    lazyListState: LazyListState
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
    val searchEnabled = uiState.searchEnabled
    val searchQuery = uiState.searchQuery

    val searchFieldState = rememberTextFieldState(searchQuery)
    if (searchEnabled && searchFieldState.text != searchQuery){
        viewModel.setSearchQuery(searchFieldState.text as String)
    }

    if (!filesLoaded) {
        viewModel.loadList()
    }

    Scaffold(
        topBar = {
            ListScreenTopBar(
                searchEnabled = searchEnabled,
                textFieldState = searchFieldState,
                scrollBehavior = scrollBehavior,
                setSearchEnabled = viewModel::setSearchEnabled,
                onFilter = { viewModel.setShowFilterDialog(true) },
                onSort = { viewModel.setShowSortDialog(true) }
            )
        },
        contentWindowInsets = WindowInsets(0),
        modifier = modifier
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            //  Loading bar while loading music
            if (!filesLoaded) {
                LoadingScreen()
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
                            SimpleMusicItem(musicList[index]) {
                                onNavigateToEditor(listOf(musicList[index]))
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
    }
}





