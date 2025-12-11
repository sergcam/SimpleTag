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

package dev.secam.simpletag.ui.editor

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.secam.simpletag.R
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.components.AnimatedFloatingActionButton
import dev.secam.simpletag.ui.components.SimpleTopBar
import dev.secam.simpletag.ui.editor.dialogs.LyricsEditorSheet
import dev.secam.simpletag.ui.editor.dialogs.AddFieldDialog
import dev.secam.simpletag.ui.editor.dialogs.BackWarningDialog
import dev.secam.simpletag.ui.editor.dialogs.HelpDialog
import dev.secam.simpletag.ui.editor.dialogs.LogDialog
import dev.secam.simpletag.ui.editor.dialogs.SaveTagDialog
import dev.secam.simpletag.ui.selector.LoadingScreen
import dev.secam.simpletag.util.rememberActivityResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditorScreen(
    musicList: List<MusicData>,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // prefs
    val prefs = viewModel.prefState.collectAsState().value
    val roundCovers = prefs?.roundCovers
    val advancedEditor = prefs?.advancedEditor

    // ui state
    val uiState = viewModel.uiState.collectAsState().value
    val editorMusicList = uiState.editorMusicList
    val initialized = uiState.initialized
    val artwork = uiState.artwork
    val lyrics = uiState.lyrics
    val fieldStates = uiState.fieldStates
    val showSaveDialog = uiState.showSaveDialog
    val showBackDialog = uiState.showBackDialog
    val showLogDialog = uiState.showLogDialog
    val showAddFieldDialog = uiState.showAddFieldDialog
    val showHelpDialog = uiState.showHelpDialog
    val showLyricsSheet = uiState.showLyricsSheet
    val searchResults = uiState.searchResults
    val log = uiState.log
    val deletedFields = uiState.deletedFields
    val artworkEnabled = uiState.artworkEnabled

    // permission request (API30+)
    val onCancelText = stringResource(R.string.permission_denied)
    val onOkText = stringResource(R.string.tag_written)
    val onErrorText = stringResource(R.string.tag_written_error)
    val actionText = stringResource(R.string.log)
    val getPermissionResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        rememberActivityResult(
            onResultCanceled = {
                scope.launch {
                    snackbarHostState.showSnackbar(onCancelText )
                }
            }
        ) {
            scope.launch {
                if(!viewModel.writeTags(context)){
                    if (snackbarHostState.showSnackbar(onErrorText, actionText) == SnackbarResult.ActionPerformed)
                        viewModel.setShowLogDialog(true)
                } else snackbarHostState.showSnackbar(onOkText)
            }
        }
    } else null

    // image picker
    val pickArtwork = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setArtwork(viewModel.getArtworkFromUri(context.contentResolver, uri))
        }
    }
    // initialize editor fields
    if (!initialized || musicList != editorMusicList) {
        viewModel.initEditor(
            musicList,
            tagNames = buildMap {
                SimpleTagField.entries.forEach { field ->
                    put(field, stringResource(field.displayNameRes))
                }
            }
        )
    }

    BackHandler {
        if (viewModel.changesMade()) {
            viewModel.setShowBackDialog(true)
        } else onNavigateBack()
    }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = if (musicList.size == 1) {
                    stringResource(R.string.edit_tag)
                } else {
                    stringResource(R.string.edit_n_tags).replaceFirst("#", musicList.size.toString())
                },
                onBack = {
                    if (viewModel.changesMade()) {
                        viewModel.setShowBackDialog(true)
                    } else onNavigateBack()
                },
                actions = {
                    if(musicList.size == 1) {
                        IconButton(
                            onClick = { viewModel.openExternal(context, musicList[0]) }
                        ) {
                            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                                viewModel.clearCache(context)
                            }
                            Icon(
                                painter = painterResource(R.drawable.ic_open_in_new_24px),
                                contentDescription = stringResource(R.string.cd_save_tag_icon)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.setShowHelpDialog(true) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_help_24px),
                                contentDescription = stringResource(R.string.cd_help_icon)
                            )
                        }
                    }

                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    AnimatedFloatingActionButton(
                        visible = true,
                        icon = painterResource(R.drawable.ic_save_24px),
                        iconDescription = stringResource(R.string.cd_save_tag_icon)
                    ) { viewModel.setShowSaveDialog(true) }
                },
//                scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
//                    FloatingToolbarExitDirection.Bottom)
            ) {


                IconButton(
                    onClick = { viewModel.setShowLyricsSheet(true) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lyrics_24px),
                        contentDescription = stringResource(R.string.cd_edit_lyrics)
                    )
                }
                IconButton(
                    onClick = {  }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_done_all_24px),
                        contentDescription = stringResource(R.string.cd_edit_lyrics)
                    )
                }
                IconButton(
                    onClick = { viewModel.setShowAddFieldDialog(true) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24px),
                        contentDescription = stringResource(R.string.cd_add_field)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { contentPadding ->
        if (initialized) {
            if (musicList.size == 1) {
                SingleEditor(
                    musicData = musicList[0],
                    artwork = artwork,
                    setArtwork = viewModel::setArtwork,
                    roundCovers = roundCovers,
                    fieldStates = fieldStates,
                    pickArtwork = pickArtwork,
                    advancedEditor = advancedEditor!!,
                    removeField = viewModel::removeField,
                    modifier = modifier
                        .padding(contentPadding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .verticalScroll(rememberScrollState()),
                    deletedFields = deletedFields
                )
            } else {
                BatchEditor(
                    artwork = artwork,
                    setArtwork = viewModel::setArtwork,
                    roundCovers = roundCovers,
                    fieldStates = fieldStates,
                    pickArtwork = pickArtwork,
                    advancedEditor = advancedEditor!!,
                    removeField = viewModel::removeField,
                    deletedFields = deletedFields,
                    modifier = modifier
                        .padding(contentPadding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .verticalScroll(rememberScrollState()),
                    artworkEnabled = artworkEnabled,
                    setArtworkEnabled = viewModel::setArtworkEnabled
                )
            }
        } else {
            LoadingScreen(text = stringResource(R.string.reading_tags))
        }

        //  Show dialogs
        if(showSaveDialog){
            SaveTagDialog(
                tagNum = musicList.size,
                onCancel = { viewModel.setShowSaveDialog(false) },
                onConfirm = {
                    viewModel.onSave(
                        activity = activity,
                        context = context,
                        launcher = getPermissionResult,
                        snackbarHostState = snackbarHostState,
                        onCancelText = onCancelText,
                        onOkText = onOkText,
                        onErrorText = onErrorText,
                        actionText = actionText
                    )
                    viewModel.setShowSaveDialog(false)
                }
            )
        }
        if(showBackDialog){
            BackWarningDialog(
                onCancel = { viewModel.setShowBackDialog(false) },
                onLeave = {
                    viewModel.setShowBackDialog(false)
                    onNavigateBack()
                }
            )
        }
        if(showLogDialog){
            LogDialog(
                log = log,
                onDismissRequest = {
                    viewModel.setShowLogDialog(false)
                }
            )
        }
        if(showAddFieldDialog){
            AddFieldDialog(
                entryList = searchResults,
                onSearch = viewModel::onSearch,
                onAdd = viewModel::addField
            ) { viewModel.setShowAddFieldDialog(false) }
        }
        if(showHelpDialog){
            HelpDialog { viewModel.setShowHelpDialog(false) }
        }
        if(showLyricsSheet){
            LyricsEditorSheet(
                songTitle = musicList[0].title,
                artist = musicList[0].artist,
                lyrics = lyrics ?: "",
                setLyrics = viewModel::setLyrics
            ) {
                viewModel.setShowLyricsSheet(false)
            }
        }
    }
}
