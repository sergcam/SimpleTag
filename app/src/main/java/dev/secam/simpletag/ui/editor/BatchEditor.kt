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

//import androidx.compose.animation.Animatable
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.editor.components.EditorArtwork
import dev.secam.simpletag.ui.editor.components.EditorArtworkButtons
import dev.secam.simpletag.ui.editor.components.EditorTextField
import dev.secam.simpletag.ui.editor.components.NoFieldTip
import org.jaudiotagger.tag.images.Artwork

@Composable
fun BatchEditor(
    musicList: List<MusicData>,
    artwork: Artwork?,
    setArtwork: (Artwork?) -> Unit,
    roundCovers: Boolean?,
    fieldStates: Map<SimpleTagField, EditorFieldState>,
    pickArtwork: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    advancedEditor: Boolean,
    removeField: (SimpleTagField) -> Unit,
    modifier: Modifier
){
    //TODO: Implement Batch Editor
    var artEnabled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        EditorArtwork(
            enabled = artEnabled,
            roundCovers = roundCovers ?: true,
            artwork = artwork,
            modifier = Modifier
                .padding(top = 16.dp)
        )
        Row {
            EditorArtworkButtons(
                onAdd = { pickArtwork.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
                onDelete = { setArtwork(null) },
                deleteEnabled = artwork != null,
                togglable = true,
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp),
                onToggle = {it -> artEnabled = it},
                enabled = artEnabled
            )

        }

        //  Field List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 72.dp)
        ) {
            if(fieldStates.isEmpty()){
                NoFieldTip()
            } else {
                LazyColumn() {
                    items(
                        items = fieldStates.map { Pair(it.key, it.value) }
                    ) { field ->
                        val visibleState = remember { MutableTransitionState<Boolean>(true) }
                        var visible by remember { mutableStateOf(true) }
//                    AnimatedVisibility() { }
                        if(!visibleState.currentState) {
                            removeField(field.first)
                        }
                        AnimatedVisibility(
                            visibleState = visibleState,
//                        enter = TODO(),
                            exit = fadeOut() + shrinkVertically(tween(150)),
                        ){

                            EditorTextField(
                                state = field.second.textState,
                                label = stringResource(field.first.displayNameRes),
                                hasDelete = advancedEditor,
                                action = {
//                                scope.launch {
////                                animation.animateTo(
////                                    targetValue = 300f,
////                                    animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
////                                )
                                    visibleState.targetState = false

//                                }

                                },
                                togglable = true,
                                onToggle = { it ->
                                    field.second.enabledState.value = it
                                },
                                enabled = field.second.enabledState.value,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
//                for (field in fieldStates) {
////                    val animation = remember { Animatable(0f) }
//
//                }
            }
        }
    }
}