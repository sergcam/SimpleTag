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
import androidx.compose.ui.tooling.preview.Preview
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
        title = "Filter",
        onDismiss = onCancel
    ) {
        ToggleDialogItem (
            currentState = toggleState,
            headlineContent = "Only show untagged files",
        ) {
            toggleState = !toggleState
        }
        SimpleDialogOptions(
            option1 = "Cancel",
            option2 = "OK",
            action1 = { onCancel() },
            action2 = {
                onCancel()
                onConfirm(toggleState)
            }
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