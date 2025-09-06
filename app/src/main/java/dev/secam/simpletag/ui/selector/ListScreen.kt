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
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.data.SimpleSortOrder
import dev.secam.simpletag.data.SortDirection
import dev.secam.simpletag.ui.components.ListScreenTopBar
import dev.secam.simpletag.ui.components.SimpleMusicItem
import dev.secam.simpletag.ui.selector.dialogs.FilterDialog
import dev.secam.simpletag.ui.selector.dialogs.SortDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    musicList: List<MusicData>,
    modifier: Modifier = Modifier,
    onNavigateToEditor: (List<MusicData>) -> Unit,
    updateHasArt: (Int) -> Unit,
    updateQuery: (String) -> Unit,
    setSortOrder: (SimpleSortOrder, SortDirection) -> Unit,
    sortOrder: SimpleSortOrder,
    sortDirection: SortDirection,
    taggedFilter: Boolean,
    setTaggedFilter: (Boolean) -> Unit,
    showSortDialog: Boolean,
    showFilterDialog: Boolean,
    setShowSortDialog: (Boolean) -> Unit,
    setShowFilterDialog: (Boolean) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val textFieldState = rememberTextFieldState()
    var searchEnabled by remember { mutableStateOf(false) }
    if (!searchEnabled) {
        textFieldState.clearText()
    }
    LaunchedEffect(textFieldState.text, sortOrder, taggedFilter, sortDirection) {
        updateQuery(textFieldState.text as String)
    }
    Scaffold(
        topBar = {
            ListScreenTopBar(
                searchEnabled = searchEnabled,
                textFieldState = textFieldState,
                scrollBehavior = scrollBehavior,
                setSearchEnabled = { enabled ->
                    searchEnabled = enabled
                },
                onFilter = { setShowFilterDialog(true)},
                onSort = { setShowSortDialog(true) }
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
            if (musicList.isEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {

                    items(
                        count = musicList.size,
                        key = { musicList[it].id },
                        contentType = { MusicData }
                    ) { index ->
                        if (musicList[index].hasArtwork == null) {
                            updateHasArt(index)
                        }
                        SimpleMusicItem(musicList[index]) {
                            onNavigateToEditor(listOf(musicList[index]))
                        }
                    }
                }
            }
        }
        if(showFilterDialog){
            FilterDialog(
                hasTag = taggedFilter,
                onConfirm = setTaggedFilter
            ) { setShowFilterDialog(false) }
        }
        if(showSortDialog){
            SortDialog(
                sortOrder = sortOrder,
                sortDirection = sortDirection,
                onConfirm = setSortOrder
            ) { setShowSortDialog(false) }
        }
    }
}





