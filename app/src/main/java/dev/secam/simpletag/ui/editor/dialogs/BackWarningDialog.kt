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
import androidx.compose.ui.tooling.preview.Preview
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions

@Composable
fun BackWarningDialog(onCancel: () -> Unit, onLeave: () -> Unit) {
    SimpleDialog(
        title = "Unsaved Changes",
        onDismiss = onCancel
    ) {
        Text(
            text = "There are unsaved changes to the file(s). Leave without saving?"
        )
        SimpleDialogOptions(
            option1 = "Cancel",
            option2 = "Leave",
            action1 = onCancel,
            action2 = onLeave
        )
    }
}

@Preview
@Composable
fun BackWarnPrev(){
    BackWarningDialog({}) { }
}