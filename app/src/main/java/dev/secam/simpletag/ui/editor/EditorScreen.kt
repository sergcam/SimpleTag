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
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import dev.secam.simpletag.R
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.data.SimpleTagField
import dev.secam.simpletag.ui.components.SimpleTopBar
import dev.secam.simpletag.util.rememberActivityResult
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
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
    val advancedEditor = prefs.advancedEditor
    val roundCovers = prefs.roundCovers

    // ui state
    val uiState = viewModel.uiState.collectAsState().value
    val initialized = uiState.initialized
    val artwork = uiState.artwork
    val fieldStates = uiState.fieldStates

    // permission request (API30+)
    val getPermissionResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        rememberActivityResult(
            onResultCanceled = {
                scope.launch {
                    snackbarHostState.showSnackbar("Permission Denied")
                }
            }
        ) {
            scope.launch {
                viewModel.writeTags().await()
                snackbarHostState.showSnackbar("Tag(s) written successfully")
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
    if(!initialized){
        val firstFile: AudioFile = AudioFileIO.read(File(musicList[0].path))
        val firstTag = firstFile.tag
        viewModel.setMusicList(musicList)
        viewModel.setArtwork(firstTag?.firstArtwork)
        viewModel.setFieldStates(
            buildMap {
                SimpleTagField.entries.forEachIndexed { index, field ->
                    if(index <= SimpleTagField.ADVANCED_CUTOFF || advancedEditor) {
                        put(field, rememberTextFieldState(firstTag?.getFirst(field.fieldKey) ?: ""))
                    }
                }
            }
        )
        viewModel.setInitialized(true)
    }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Edit Tag",
                actionIcon = painterResource(R.drawable.ic_save_24px),
                contentDescription = "save tag",
                onBack = onNavigateBack,
                action = {
                    viewModel.onSave(
                        activity = activity,
                        context = context,
                        launcher = getPermissionResult,
                        snackbarHostState = snackbarHostState
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)

        }
    ) { contentPadding ->
        if(musicList.size == 1) {
            SingleEditor(
                artwork = artwork,
                setArtwork = viewModel::setArtwork,
                roundCovers = roundCovers,
                fieldStates = fieldStates,
                modifier = modifier
                    .padding(contentPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState()),
                pickArtwork = pickArtwork
            )
        }
        else {
            BatchEditor(musicList)
        }
    }
}