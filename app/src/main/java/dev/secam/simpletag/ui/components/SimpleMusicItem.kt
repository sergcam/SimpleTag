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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.data.MusicData

@Composable
fun SimpleMusicItem(musicData: MusicData, modifier: Modifier = Modifier, onClick: () -> Unit){
    if(musicData.title == null){
        ListItem(
            headlineContent = {},
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }
    else {
        ListItem(
            headlineContent = {
                Text(musicData.title)
            },
            supportingContent = {
                Text(
                    text = "${musicData.album} - ${musicData.artist}"
                )
            },
            leadingContent = {
                Box(
                    modifier = modifier
                        .size(48.dp)
                        .clip(
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                SimpleAlbumArtwork(musicData)
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = modifier
                .clickable(
                    enabled = true,
                    onClick = onClick
                )



        )
    }
}