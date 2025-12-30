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

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.editor.components.EditorArtwork
import dev.secam.simpletag.ui.editor.components.EditorArtworkButtons
import dev.secam.simpletag.ui.editor.components.EditorTextField
import dev.secam.simpletag.ui.editor.components.FileInfo
import dev.secam.simpletag.ui.editor.components.NoFieldTip
import org.jaudiotagger.tag.images.Artwork

@Composable
fun SingleEditor(
    musicData: MusicData,
    roundCovers: Boolean?,
    modifier: Modifier = Modifier,
    setArtwork: (Artwork?) -> Unit,
    artwork: Artwork?,
    fieldStates: Map<SimpleTagField, EditorFieldState>,
    pickArtwork: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    removeField: (SimpleTagField) -> Unit,
    simpleEditor: Boolean,
    deletedFields: Set<SimpleTagField>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        EditorArtwork(
            roundCovers = roundCovers ?: true,
            artwork = artwork,
            modifier = Modifier
                .padding(top = 16.dp)
        )
        EditorArtworkButtons(
            onAdd = { pickArtwork.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            onDelete = { setArtwork(null) },
            deleteEnabled = artwork != null,
            modifier = Modifier
                .padding(top = 10.dp, bottom = 6.dp)
        )
        if (!simpleEditor){
            FileInfo(
                musicData = musicData,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )
        }

        //  Field List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 84.dp)
                .animateContentSize()
        ) {
            if(fieldStates.isEmpty()){
                NoFieldTip()
            } else {
                for (field in fieldStates) {
                    val visibleState = remember { MutableTransitionState<Boolean>(true) }
                    if(!visibleState.targetState) {
                        if(!deletedFields.contains(field.key)){
                            visibleState.targetState = true
                        }
                    }
                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn() + expandVertically(tween(150)),
                        exit = fadeOut() + shrinkVertically(tween(150)),
                    ) {
                        EditorTextField(
                            state = field.value.textState,
                            label = stringResource(field.key.displayNameRes),
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            hasDelete = !simpleEditor,
                            action = {
                                removeField(field.key)
                                visibleState.targetState = false
                            },
                            onToggle = { removeField(field.key) },
                        )
                    }
                }
            }
        }
    }
}