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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.util.durationFormatter

const val ANIMATION_DURATION = 200
@Composable
fun SimpleAlbumItem(
    musicList: List<MusicData>,
    onSongClick: (MusicData) -> Unit,
    expanded: Boolean,
    onSongLongClick: (MusicData) -> Unit,
    onClick: () -> Unit,
    selectedSet: Set<MusicData>,
    onLongClick: (List<MusicData>) -> Unit
) {
    val selected = selectedSet.containsAll(musicList)
    val containerColor by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.background,
        label = "selected",
        animationSpec = tween(150)
    )
    Column {
        ListItem(
            headlineContent = {
                Text(text = musicList[0].album, fontWeight = FontWeight.Medium)
            },
            supportingContent = {
                Text(
                    text = musicList[0].artist
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    SimpleAlbumArtwork(musicList[0],selected = selected)
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = containerColor
            ),
            modifier = Modifier
                .combinedClickable(
                    enabled = true,
                    onClick = onClick,
                    onLongClick = { onLongClick(musicList) }
                )
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(tween(ANIMATION_DURATION)),
            exit = fadeOut() + shrinkVertically(tween(ANIMATION_DURATION)),
        ) {
            Column(
//                modifier = modifier
            ) {
                for (song in musicList.sortedBy { it.track }) {
                    AlbumSubItem(
                        musicData = song,
                        onClick = onSongClick,
                        onLongClick =  onSongLongClick,
                        selected = selectedSet.contains(song)
                    )
                }
            }
//            AlbumSubItems(musicList, onSongClick, onLongClick, selected = false)
        }
//        if (expanded) {
//            AlbumSubItems(musicList, onSongClick)
//        }
    }
}

// TODO: get selected state and visually update item
@Composable
fun AlbumSubItem(
    musicData: MusicData,
    onClick: (MusicData) -> Unit,
    onLongClick: (MusicData) -> Unit,
//    modifier: Modifier = Modifier,
    selected: Boolean = false,

) {
    val containerColor by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.surfaceContainer,
        label = "selected",
        animationSpec = tween(150)
    )
    ListItem(
        headlineContent = {
            Text(text = musicData.title)
        },
        leadingContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)

            ) {
                AnimatedContent(
                    targetState = selected
                ) { targetSelected ->
                    if (!targetSelected) {
                        Text(
                            text = musicData.track.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }else {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_24px),
                            contentDescription = stringResource(R.string.cd_selected)
                        )
                    }
                }

            }
        },
        supportingContent = {
            Text(
                text = durationFormatter(musicData.duration),
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .combinedClickable(
                enabled = true,
                onClick = { onClick(musicData) },
                onLongClick = { onLongClick(musicData) }
            )
    )
}

