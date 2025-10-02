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

package dev.secam.simpletag.ui.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import dev.secam.simpletag.R

@Composable
fun EditorTextField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier,
    hasDelete: Boolean = false,
    action: (() -> Unit)? = null,
) {
    OutlinedTextField(
        state = state,
        label = {
            Text(
                text = label
            )
        },
        trailingIcon = if (hasDelete && action != null) {
            {
                IconButton(action) {
                    Icon(painterResource(R.drawable.ic_delete_24px), "delete")
                }
            }
        } else null,
        modifier = modifier
            .fillMaxWidth()
    )
}