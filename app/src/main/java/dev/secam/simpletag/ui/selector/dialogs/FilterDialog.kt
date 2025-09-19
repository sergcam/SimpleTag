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

package dev.secam.simpletag.ui.selector.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions
import dev.secam.simpletag.ui.components.ToggleDialogItem

@Composable
fun FilterDialog(
    hasTag: Boolean,
    onConfirm: (Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var toggleState by remember { mutableStateOf(hasTag) }
    SimpleDialog(
        title = stringResource(R.string.filter),
        onDismiss = onCancel,
        manualPadding = true
    ) {
        ToggleDialogItem (
            currentState = toggleState,
            headlineContent = stringResource(R.string.untagged_files_filter),
        ) {
            toggleState = !toggleState
        }
        SimpleDialogOptions(
            option1 = stringResource(R.string.dialog_cancel),
            option2 = stringResource(R.string.dialog_ok),
            action1 = { onCancel() },
            action2 = {
                onCancel()
                onConfirm(toggleState)
            },
            manualPadding = true
        )
    }
}

@Preview
@Composable
fun FilterPrev(){
    FilterDialog(
        hasTag = false,
        onConfirm = { }
    ) { }
}