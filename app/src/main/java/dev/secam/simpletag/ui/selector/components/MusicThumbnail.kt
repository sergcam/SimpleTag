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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import dev.secam.simpletag.R
import dev.secam.simpletag.data.media.MusicData

@Composable
fun SimpleAlbumArtwork(musicData: MusicData, modifier: Modifier = Modifier, selected: Boolean) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier
            .clip(
                shape = RoundedCornerShape(8.dp)
            ),
        ) {
        if (musicData.hasArtwork == null || !musicData.hasArtwork) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note_24),
                    contentDescription = null
                )
            }
        } else {
            Image(
                rememberAsyncImagePainter(musicData),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(tween(150)) + fadeIn(),
            exit = scaleOut(tween(150)) + fadeOut(),
        ) {
            SelectCheckCircle()
        }
    }
}