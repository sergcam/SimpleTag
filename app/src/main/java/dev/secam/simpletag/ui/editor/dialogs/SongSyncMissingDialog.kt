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


package dev.secam.simpletag.ui.editor.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions

@Composable
fun SongSyncMissingDialog(
    onDismiss: () -> Unit
){
    SimpleDialog(
        title = stringResource(R.string.song_sync_missing),
        manualPadding = false,
        onDismiss = onDismiss
    ) {
        Text(
            text = stringResource(R.string.song_sync_long)
        )
        SimpleDialogOptions(
            option = stringResource(R.string.dialog_ok),
            action = onDismiss,
        )
    }
}