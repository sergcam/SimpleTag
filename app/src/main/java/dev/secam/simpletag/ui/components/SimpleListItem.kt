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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SimpleListItem(
    headlineContent: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    iconColor: Color = ListItemDefaults.colors().leadingIconColor,
    supportingContent: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = { Text(headlineContent,fontWeight = FontWeight.Medium) },
        supportingContent = {
            if (supportingContent != null) {
                Text(supportingContent)
            }
        },
        leadingContent = {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = modifier.size(24.dp)
            )
        },
        colors = if (enabled) ListItemDefaults.colors(
            leadingIconColor = iconColor
        ) else ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.outline,
            supportingColor = MaterialTheme.colorScheme.outlineVariant,
            leadingIconColor = MaterialTheme.colorScheme.outline,
        ),
        modifier = modifier
            .clickable(enabled = enabled, onClick = {
                onClick()
            })
            .fillMaxWidth()
            .padding()
            .background(MaterialTheme.colorScheme.primary)
    )
}