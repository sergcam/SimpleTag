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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.ui.components.SimpleSectionHeader
import dev.secam.simpletag.ui.editor.components.EditorArtwork
import dev.secam.simpletag.ui.editor.components.EditorArtworkButtons
import dev.secam.simpletag.ui.editor.components.EditorTextField
import org.jaudiotagger.tag.images.Artwork

@Composable
fun SingleEditor(
    path: String,
    roundCovers: Boolean?,
    modifier: Modifier = Modifier,
    setArtwork: (Artwork?) -> Unit,
    artwork: Artwork?,
    fieldStates: Map<SimpleTagField, TextFieldState>,
    pickArtwork: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>
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
                .padding(top = 8.dp)
        )
        SimpleSectionHeader(
            text = stringResource(R.string.file_location),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Text(
            text = path,
            modifier = Modifier
                .padding(bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            for (field in fieldStates) {
                EditorTextField(
                    state = field.value,
                    label = stringResource(field.key.displayNameRes),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}



