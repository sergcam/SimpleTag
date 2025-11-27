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

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import org.jaudiotagger.tag.images.Artwork

@Composable
fun EditorArtwork(
    roundCovers: Boolean,
    artwork: Artwork?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(
                shape =
                    if (roundCovers) RoundedCornerShape(16.dp)
                    else RectangleShape
            )
    ) {
        if (artwork != null) {
            Image(
                alpha = if( enabled )1f else .5f,
                bitmap = BitmapFactory.decodeByteArray(
                    artwork.binaryData,
                    0,
                    artwork.binaryData.size
                ).asImageBitmap(),
                contentDescription = stringResource(R.string.cd_album_art),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
            )

            if (!enabled){
                Icon(
                    painter = painterResource(R.drawable.edit_off_24px),
                    contentDescription = stringResource(R.string.cd_art_disabled),
                tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(48.dp)
                )
            }

        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_music_note_24),
                    contentDescription = stringResource(R.string.cd_empty_art),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxSize(.5f)
                )
            }

        }

    }
}

@Preview
@Composable
fun AlbumArtPrev(){
    EditorArtwork(
        true,null
    )
}