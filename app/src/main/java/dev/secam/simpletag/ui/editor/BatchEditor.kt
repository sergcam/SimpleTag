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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.data.media.MusicData
import org.jaudiotagger.tag.images.Artwork

@Composable
fun BatchEditor(
    musicList: List<MusicData>,
    artwork: Artwork?,
    setArtwork: (Artwork?) -> Unit,
    roundCovers: Boolean?,
    fieldStates: Map<SimpleTagField, TextFieldState>,
    pickArtwork: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    advancedEditor: Boolean,
    removeField: (SimpleTagField) -> Unit,
    modifier: Modifier
){
    //TODO: Implement Batch Editor
}