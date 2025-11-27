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

package dev.secam.simpletag.ui.selector.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.media.MusicData

@Composable
fun SimpleMusicItem(
    musicData: MusicData,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
//    val selected by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.background,
        animationSpec = tween(150)
    )
    ListItem(
        headlineContent = {
            Text(text = musicData.title, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text(
                text = "${musicData.album} - ${musicData.artist}"
            )
        },
        leadingContent = {
            SimpleAlbumArtwork(
                musicData = musicData,
                selected = selected,
                modifier = modifier
                    .size(48.dp)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = containerColor

        ),
        modifier = modifier
            .combinedClickable(
                enabled = true,
                onClick = onClick,
                onLongClick = onLongClick
            )
    )
}

@Composable
fun SelectCheckCircle(){
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .aspectRatio(1f)


    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}