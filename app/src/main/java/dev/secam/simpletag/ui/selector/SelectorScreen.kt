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

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.secam.simpletag.R
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.components.SimpleTopBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SelectorScreen(
    modifier: Modifier = Modifier,
    viewModel: SelectorViewModel = hiltViewModel(),
    onNavigateToEditor: (List<MusicData>) -> Unit,
    onNavigateToSettings: () -> Unit
) {

    // ui state
    val uiState = viewModel.uiState.collectAsState().value
    val musicList = uiState.filteredMusic
    val showSortDialog = uiState.showSortDialog
    val showFilterDialog = uiState.showFilterDialog
    val taggedFilter = uiState.taggedFilter
    val sortOrder = uiState.sortOrder
    val sortDirection = uiState.sortDirection

    val mediaPermissionState = rememberPermissionState(
            permission =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "SimpleTag",
                actionIcon = painterResource(R.drawable.ic_settings_24px),
                contentDescription = "save tag",
                action = { onNavigateToSettings() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->

        if (mediaPermissionState.status.isGranted) {
            val contentResolver = LocalContext.current.contentResolver
            // TODO: fix this. key1 should not be true (reloads library everytime you navigate)
            LaunchedEffect(key1 = true) {
                viewModel.loadFiles(contentResolver)
            }
            ListScreen(
                onNavigateToEditor = onNavigateToEditor,
                updateHasArt = viewModel::updateHasArt,
                musicList = musicList,
                updateQuery = viewModel::updateQuery,
                modifier = modifier
                    .padding(contentPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                setSortOrder = viewModel::setSortOrder,
                taggedFilter = taggedFilter,
                setTaggedFilter = viewModel::setTaggedFilter,
                showSortDialog = showSortDialog,
                showFilterDialog = showFilterDialog,
                setShowSortDialog = viewModel::setShowSortDialog,
                setShowFilterDialog = viewModel::setShowFilterDialog,
                sortOrder = sortOrder,
                sortDirection = sortDirection
            )
        } else {
            PermissionScreen(
                modifier = modifier
                    .padding(contentPadding)
            ) { mediaPermissionState.launchPermissionRequest() }
        }

    }
}