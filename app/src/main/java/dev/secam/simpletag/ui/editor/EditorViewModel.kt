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
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.data.media.MediaRepo
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.data.preferences.PreferencesRepo
import dev.secam.simpletag.util.tag.oggFileWriter
import dev.secam.simpletag.util.tag.setArtworkField
import dev.secam.simpletag.util.tag.simpleFileReader
import dev.secam.simpletag.util.tag.simpleFileWriter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import org.jaudiotagger.audio.AudioFile
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

const val WRITE_TIMEOUT = 1000L

@HiltViewModel
class EditorViewModel @Inject constructor(
    preferencesRepo: PreferencesRepo,
    private val mediaRepo: MediaRepo
): ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()
    val prefState = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val backgroundScope =
        viewModelScope.plus(Dispatchers.Default + coroutineExceptionHandler)

    fun initEditor(musicList: List<MusicData>, tagNames: Map<SimpleTagField, String>) {
        backgroundScope.launch {
            val advancedEditor = prefState.value?.advancedEditor
            if (advancedEditor != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        invisibleTags = SimpleTagField.entries.toSet(),
                        tagNames = tagNames
                        )
                }
                val firstTag = simpleFileReader(musicList[0].path).tag
                setEditorMusicList(musicList)
                setArtwork(firstTag?.firstArtwork)
                SimpleTagField.entries.forEachIndexed { index, field ->
                    if(!advancedEditor && index <= SimpleTagField.ADVANCED_CUTOFF){
                        addField(field, firstTag.getFirst(field.fieldKey))
                    }
                    else {
                        if(!firstTag.getFirst(field.fieldKey).isEmpty()) {
                            addField(field, firstTag.getFirst(field.fieldKey))
                        }
                    }
                }
                setSavedFields()
                setArtworkChanged(false)
                setInitialized(true)
                onSearch()
            }
        }
    }

    fun addField(field: SimpleTagField, content: String = ""){
        _uiState.update {
            it.copy(
                fieldStates = uiState.value.fieldStates + Pair(field, TextFieldState(content)),
                invisibleTags = uiState.value.invisibleTags - field
            )
        }
    }
    fun removeField(field: SimpleTagField){
        _uiState.update {
            it.copy(
                fieldStates = uiState.value.fieldStates - field,
                invisibleTags = uiState.value.invisibleTags + field,
                deletedFields = uiState.value.deletedFields + field
            )
        }
    }
    fun createTag(ext: String): Tag {
        return when (ext) {
            "flac" -> FlacTag()
            "mp4", "m4a", "m4p", "aac" -> Mp4Tag()
            "ogg" -> VorbisCommentTag()
            "wma" -> AsfTag()
            "mp3", "wav", "wave", "dsf", "aiff", "aif", "aifc" -> ID3v24Tag()
            else -> null
        }!!
    }

    fun getArtworkFromUri(contentResolver: ContentResolver, uri: Uri): Artwork? {
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

    suspend fun writeTags(context: Context): Boolean {
        _uiState.update { it.copy(log = "") }
        backgroundScope.async {
            var log = ""
            try {
                withTimeout(WRITE_TIMEOUT) {
                    log = "Entered writeTags()\n"
                    val fields = uiState.value.fieldStates
                    val artwork = uiState.value.artwork
                    val musicList = uiState.value.editorMusicList
                    if (musicList.size == 1) {
                        log += "Writing single file\n"
                        val file: AudioFile = simpleFileReader(musicList[0].path)
                        log += "Opened file: ${file.file.path}\n"
                        if (file.tag == null) {
                            log += "No tag. Creating tag\n"
                            file.tag = createTag(file.ext)
                            log += "Tag created\n"
                        }
                        val tag = file.tag
                        log += "Tag opened\n"
                        tag.deleteArtworkField()
                        log += "Deleted old artwork\n"
                        if (artwork != null) {
                            when (tag.javaClass) {
                                FlacTag().javaClass -> {
                                    (tag as FlacTag).setArtworkField(artwork)
                                }
                                VorbisCommentTag().javaClass -> {
                                    (tag as VorbisCommentTag).setArtworkField(artwork)
                                }
                                else -> tag.setField(artwork)
                            }
                            log += "Wrote new artwork as ${tag.javaClass}\n"
                        }
                        for(field in uiState.value.deletedFields){
                            tag.deleteField(field.fieldKey)
                        }
                        for (field in fields) {
                            if (!field.value.text.isEmpty()) {
                                tag.setField(field.key.fieldKey, field.value.text as String)
                                log += "Wrote field ${field.key.fieldKey} with content: ${field.value.text}\n"
                            } else {
                                tag.deleteField(field.key.fieldKey)
                                log += "Cleared field ${field.key.fieldKey}\n"
                            }
                        }
                        if (file.ext == "ogg") {
                            log += "Writing file using ogg writer\n"
                            oggFileWriter(file, context)
                            log += "Wrote file: ${file.file.path} \n"
                        } else {
                            log += "Writing file using default writer\n"
                            simpleFileWriter(file)
                            log += "Wrote file: ${file.file.path} \n"
                        }
                    } else {
                        log += "Writing multiple files\n"
                        //TODO: batch tagging support
                    }
                    log += "Resetting change tracking\n"
                    //  reset change tracking
                    setArtworkChanged(false)
                    setSavedFields()
                    log += "Change tracking reset\n"
                    //  refresh mediastore to reflect changes
                    log += "Refreshing mediastore\n"
                    mediaRepo.refreshMediaStore(uiState.value.editorMusicList)
                    log += "Mediastore refreshed\n"
                    log += "Finished saving\n"
                    Log.d("EditorVM", log)
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.update { it.copy(log = e.toString() + "\n$log") }
            }
        }.await()
        return uiState.value.log == ""
    }

    fun onSave(
        activity: Activity?,
        context: Context?,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>?,
        snackbarHostState: SnackbarHostState,
        onCancelText: String,
        onOkText: String,
        onErrorText: String,
        actionText: String
    ) {
        if (activity != null && context != null) {
            backgroundScope.launch {
                // request permission for api 30+
                if (launcher != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val musicList = uiState.value.editorMusicList
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
                        if (!writeTags(context)) {
                            if (snackbarHostState.showSnackbar(
                                    onErrorText,
                                    actionText
                                ) == SnackbarResult.ActionPerformed
                            )
                                setShowLogDialog(true)
                        } else snackbarHostState.showSnackbar(onOkText)

                    } catch (e: AccessDeniedException) {
                        e.printStackTrace()
                        snackbarHostState.showSnackbar(onCancelText)
                    }
                }
            }
        }
    }

    fun changesMade(): Boolean {
        val currentFields = uiState.value.fieldStates.map { mapEntry ->
            mapEntry.value.text as String
        }
        return (uiState.value.artworkChanged || uiState.value.savedFields != currentFields)
    }

    fun onSearch(query: String = "") {
        val list = uiState.value.tagNames .filterKeys { it in uiState.value.invisibleTags }
        if(!query.isEmpty()){
            val list1 = mutableListOf<SimpleTagField>()
            val list2 = mutableListOf<SimpleTagField>()
            for (entry in list) {
                if(entry.key in uiState.value.invisibleTags){
                    if (entry.value.startsWith(query, true)) {
                        list1.add(entry.key)
                    } else if (entry.value.contains(query, true)) {
                        list2.add(entry.key)
                    }
                }
            }
            _uiState.update { it.copy(searchResults = list1 + list2) }
        } else _uiState.update { currentState ->
            currentState.copy(searchResults = list.map { it.key })
        }
    }

    /*------- Setters -------*/
    fun setArtwork(artwork: Artwork?) {
        _uiState.update { currentState ->
            currentState.copy(
                artwork = artwork,
                artworkChanged = true
            )
        }
    }

//    fun setFieldStates(fieldStates: Map<SimpleTagField, TextFieldState>) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                fieldStates = fieldStates
//            )
//        }
//    }

    fun setEditorMusicList(editorMusicList: List<MusicData>) {
        _uiState.update { currentState ->
            currentState.copy(
                editorMusicList = editorMusicList
            )
        }
    }

    fun setInitialized(initialized: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                initialized = initialized
            )
        }
    }

    fun setShowBackDialog(showBackDialog: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showBackDialog = showBackDialog
            )
        }
    }

    fun setShowSaveDialog(showSaveDialog: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showSaveDialog = showSaveDialog
            )
        }
    }

    fun setSavedFields() {
        _uiState.update {
            it.copy(
                savedFields = uiState.value.fieldStates.map { mapEntry ->
                    mapEntry.value.text as String
                }
            )
        }
    }
    fun setArtworkChanged(artworkChanged: Boolean) {
        _uiState.update { it.copy(artworkChanged = artworkChanged) }
    }

    fun setShowLogDialog(showLogDialog: Boolean) {
        _uiState.update { it.copy(showLogDialog = showLogDialog) }
    }

    fun setShowAddFieldDialog(showAddFieldDialog: Boolean){
        _uiState.update { it.copy(showAddFieldDialog = showAddFieldDialog) }
    }

}

data class EditorUiState(
    val initialized: Boolean = false,
    val artwork: Artwork? = null,
    val fieldStates: Map<SimpleTagField, TextFieldState> = mapOf(),
    val editorMusicList: List<MusicData> = listOf(),
    val artworkChanged: Boolean = false,
    val showBackDialog: Boolean = false,
    val showSaveDialog: Boolean = false,
    val showLogDialog: Boolean = false,
    val showAddFieldDialog: Boolean = false,
    val savedFields: List<String> = listOf(),
    val log: String = "",
    val tagNames: Map<SimpleTagField, String> = mapOf(),
    val invisibleTags: Set<SimpleTagField> = setOf(),
    val searchResults: List<SimpleTagField> = listOf(),
    val deletedFields: Set<SimpleTagField> = setOf()
)

