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
 import androidx.compose.animation.AnimatedVisibility
 import androidx.compose.animation.core.tween
 import androidx.compose.animation.fadeIn
 import androidx.compose.animation.fadeOut
 import androidx.compose.animation.scaleIn
 import androidx.compose.animation.scaleOut
 import androidx.compose.foundation.layout.Column
 import androidx.compose.foundation.layout.padding
 import androidx.compose.foundation.lazy.rememberLazyListState
 import androidx.compose.foundation.text.input.rememberTextFieldState
 import androidx.compose.material3.ExperimentalMaterial3Api
 import androidx.compose.material3.FloatingActionButton
 import androidx.compose.material3.Icon
 import androidx.compose.material3.Scaffold
 import androidx.compose.material3.TopAppBarDefaults
 import androidx.compose.runtime.Composable
 import androidx.compose.runtime.collectAsState
 import androidx.compose.runtime.derivedStateOf
 import androidx.compose.runtime.getValue
 import androidx.compose.runtime.mutableStateOf
 import androidx.compose.runtime.remember
 import androidx.compose.runtime.rememberCoroutineScope
 import androidx.compose.runtime.setValue
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.input.nestedscroll.nestedScroll
 import androidx.compose.ui.res.painterResource
 import androidx.compose.ui.res.stringResource
 import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
 import com.google.accompanist.permissions.ExperimentalPermissionsApi
 import com.google.accompanist.permissions.isGranted
 import com.google.accompanist.permissions.rememberPermissionState
 import dev.secam.simpletag.R
 import dev.secam.simpletag.data.media.MusicData
 import dev.secam.simpletag.ui.components.SimpleTopBar
 import dev.secam.simpletag.ui.selector.components.ListScreenTopBar
 import dev.secam.simpletag.ui.selector.components.MultiSelectTopBar
 import dev.secam.simpletag.ui.selector.permission.OptionalPermissionScreen
 import dev.secam.simpletag.ui.selector.permission.PermissionScreen
 import dev.secam.simpletag.util.animateExpandTopBar
 import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SelectorScreen(
    modifier: Modifier = Modifier,
    viewModel: SelectorViewModel = hiltViewModel(),
    onNavigateToEditor: (List<MusicData>) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val optionalPermissionsSkipped = viewModel.prefState.collectAsState().value.optionalPermissionsSkipped
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollBehaviorPinned = TopAppBarDefaults.pinnedScrollBehavior()
    val mediaPermissionState = rememberPermissionState(
        permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                Manifest.permission.READ_EXTERNAL_STORAGE
            } else {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
    )
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val firstVisible = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    val uiState = viewModel.uiState.collectAsState().value
    val searchEnabled = uiState.searchEnabled
    val searchQuery = uiState.searchQuery
    val multiSelectEnabled = uiState.multiSelectEnabled
    val selectedItems = uiState.selectedItems
    val searchFieldState = rememberTextFieldState(searchQuery)
    if (searchEnabled && searchFieldState.text != searchQuery){
        viewModel.setSearchQuery(searchFieldState.text as String)
    }

    Scaffold(
        topBar = {
            Column {
                if (!multiSelectEnabled) {
                    SimpleTopBar(
                        title = stringResource(R.string.app_name),
                        actionIcon = painterResource(R.drawable.ic_settings_24px),
                        contentDescription = stringResource(R.string.cd_settings_icon),
                        action = { onNavigateToSettings() },
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    MultiSelectTopBar(
                        numSelected = selectedItems.size,
                        scrollBehavior = scrollBehavior,
                        onEdit = { onNavigateToEditor(selectedItems.toList()) },
                        onBack = { viewModel.setMultiSelectedEnabled(false) }
                    )
                }
                ListScreenTopBar(
                    searchEnabled = searchEnabled,
                    textFieldState = searchFieldState,
                    scrollBehavior = scrollBehaviorPinned,
                    setSearchEnabled = viewModel::setSearchEnabled,
                    onFilter = { viewModel.setShowFilterDialog(true) },
                    onSort = { viewModel.setShowSortDialog(true) }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = firstVisible.value != 0,
                enter = scaleIn(tween(150)) + fadeIn(),
                exit = scaleOut(tween(150)) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            lazyListState.animateScrollToItem(0)
                            scrollBehavior.animateExpandTopBar()
                        }

                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_upward_24px),
                        contentDescription = stringResource(R.string.cd_scroll_top)
                    )
                }
            }
        }
    ) { contentPadding ->
        var readAudio by remember { mutableStateOf(mediaPermissionState.status.isGranted) }
        readAudio = mediaPermissionState.status.isGranted

        when {
            readAudio && optionalPermissionsSkipped ->
                ListScreen(
                    onNavigateToEditor = onNavigateToEditor,
                    viewModel = viewModel,
                    lazyListState = lazyListState,
                    modifier = modifier
                        .padding(contentPadding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .nestedScroll(scrollBehaviorPinned.nestedScrollConnection),
                    scrollBehavior = scrollBehavior
                )
            !readAudio ->
                PermissionScreen(
                    mediaPermissionState = mediaPermissionState,
                    modifier = modifier
                        .padding(contentPadding)
                )
            else ->
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    OptionalPermissionScreen(
                        modifier = modifier
                            .padding(contentPadding),
                        setSkipped = viewModel::setOptionalPermissionsSkipped
                    )
                } else viewModel.setOptionalPermissionsSkipped(true)
        }
    }
}

