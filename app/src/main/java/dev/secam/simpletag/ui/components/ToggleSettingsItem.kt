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

package dev.secam.simpletag.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ToggleSettingsItem(
    modifier: Modifier = Modifier,
    currentState: Boolean,
    headlineContent: String,
    supportingContent: String? = null,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(headlineContent,fontWeight = FontWeight.Medium) },
        supportingContent = {
            if (supportingContent != null) {
                Text(supportingContent)
            }
        },
        trailingContent = {
            Switch(
                checked = currentState,
                onCheckedChange = null
            )
        },
        colors = if (enabled) ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ) else ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.outline,
            supportingColor = MaterialTheme.colorScheme.outlineVariant,
            leadingIconColor = MaterialTheme.colorScheme.outline,
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
            .clickable(enabled = enabled, onClick = {
                onToggle(!currentState)
            })
            .padding(horizontal = 0.dp, vertical = 0.dp)
    )
}