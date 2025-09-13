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

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.data.SimpleTagField
import dev.secam.simpletag.data.preferences.PreferencesRepo
import dev.secam.simpletag.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.asf.AsfTag
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.images.AndroidArtwork.createArtworkFromFile
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.mp4.Mp4Tag
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag
import java.io.File
import java.nio.file.AccessDeniedException
import javax.inject.Inject


@HiltViewModel
class EditorViewModel @Inject constructor(
    preferencesRepo: PreferencesRepo
): ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()
    val prefState = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )
    val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope = viewModelScope.plus(Dispatchers.Default + coroutineExceptionHandler)

    fun createTag(ext: String): Tag {
        return when (ext){
            "flac" -> FlacTag()
            "mp4", "m4a", "m4p" -> Mp4Tag()
            "ogg" -> VorbisCommentTag()
            "wma" -> AsfTag()
            "mp3", "wav", "wave", "dsf", "aiff", "aif", "aifc" -> ID3v24Tag()
            else -> null
        }!!
    }

    fun isCompatible(ext: String): Boolean {
        val types = listOf("mp3", "wav", "wave", "dsf", "aiff", "aif", "aifc", "wma", "ogg", "mp4", "m4a", "m4p", "flac")
        return types.contains(ext)
    }

    fun getArtworkFromUri(contentResolver: ContentResolver, uri: Uri): Artwork?{
        var path: String? = null
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        }
        val artwork = createArtworkFromFile(File(path!!))
        _uiState.update { currentState ->
            currentState.copy(
                artwork = artwork
            )
        }
        return artwork
    }

    fun writeTags(): Deferred<Unit> {
        return backgroundScope.async{
            val fields = uiState.value.fieldStates
            val artwork = uiState.value.artwork
            val musicList = uiState.value.musicList
            if (musicList.size == 1) {
                val file: AudioFile = AudioFileIO.read(File(musicList[0].path))
                if(file.tag == null) {
                    file.tag = createTag(file.ext)
                }
                val tag = file.tag
                if (artwork == null) tag.deleteArtworkField() else tag.setField(artwork)
                for (field in fields) {
                    tag.setField(field.key.fieldKey, field.value.text as String)
                }
                AudioFileIO.write(file)
            } else {
                //TODO: batch tagging support
            }
        }
    }

    fun onSave(
        activity: Activity?,
        context: Context?,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>?,
        snackbarHostState: SnackbarHostState
    ) {
        if (activity != null && context != null) {
            backgroundScope.launch {
                // request permission for api 30+
                if (launcher != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val musicList = uiState.value.musicList
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.getContentUri("external"),
                        musicList[0].id
                    )
                    val request = IntentSenderRequest.Builder(
                        MediaStore.createWriteRequest(
                            context.contentResolver,
                            arrayListOf(uri)
                        )
                    ).build()
                    launcher.launch(request)
                }
                // permission request not needed for api 29 and below
                else {
                    try {
                        writeTags().await()
                        snackbarHostState.showSnackbar("Tag(s) written successfully")
                    } catch (e: AccessDeniedException) {
                        snackbarHostState.showSnackbar("Permission Denied")
                    }
                }
            }
        }
    }

    fun setArtwork(artwork: Artwork?){
        _uiState.update { currentState ->
            currentState.copy(
                artwork = artwork
            )
        }
    }

    fun setFieldStates(fieldStates: Map<SimpleTagField,TextFieldState>){
        _uiState.update { currentState ->
            currentState.copy(
                fieldStates = fieldStates
            )
        }
    }

    fun setMusicList(musicList: List<MusicData>){
        _uiState.update { currentState ->
            currentState.copy(
                musicList = musicList
            )
        }
    }

    fun setInitialized(initialized: Boolean){
        _uiState.update { currentState ->
            currentState.copy(
                initialized = initialized
            )
        }
    }
}

data class EditorUiState(
    val initialized: Boolean = false,
    val artwork: Artwork? = null,
    val fieldStates: Map<SimpleTagField,TextFieldState> = mapOf(),
    val musicList: List<MusicData> = listOf()
)