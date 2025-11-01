package dev.secam.simpletag.ui.selector.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.util.durationFormatter

const val ANIMATION_DURATION = 200
@Composable
fun SimpleAlbumItem(
    musicList: List<MusicData>,
    onSongClick: (MusicData) -> Unit,
    expanded: Boolean,
    onLongClick: (MusicData) -> Unit,
    onClick: () -> Unit
) {
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
                    SimpleAlbumArtwork(musicList[0])
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier
                .combinedClickable(
                    enabled = true,
                    onClick = onClick,
                    // TODO: add long click for album item
                    //onLongClick = onLongClick
                )
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(tween(ANIMATION_DURATION)),
            exit = fadeOut() + shrinkVertically(tween(ANIMATION_DURATION)),
//            modifier = Modifier
//                .height(100.dp)
        ) {
            AlbumSubItems(musicList, onSongClick, onLongClick)
        }
//        if (expanded) {
//            AlbumSubItems(musicList, onSongClick)
//        }
    }
}

// TODO: get selected state and visually update item
@Composable
fun AlbumSubItems(
    musicList: List<MusicData>,
    onClick: (MusicData) -> Unit,
    onLongClick: (MusicData) -> Unit,
    modifier: Modifier = Modifier
) {
    val newList = musicList.sortedBy { it.track }
    Column(
        modifier = modifier
    ) {
        for (song in newList) {
            ListItem(
                headlineContent = {
                    Text(text = song.title)
                },
                leadingContent = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)

                    ) {
                        Text(
                            text = song.track.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                supportingContent = {
                    Text(
                        text = durationFormatter(song.duration),
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .combinedClickable(
                        enabled = true,
                        onClick = { onClick(song) },
                        onLongClick = { onLongClick(song) }
                    )
            )
        }
    }
}
//
//val eg = listOf(
//    MusicData(
//        id = 2,
//        path = "null",
//        title = "two",
//        artist = "artist name",
//        album = "album title",
//        hasArtwork = false,
//        track = 2,
//        tagged = true
//    ),
//    MusicData(
//        id = 3,
//        path = "null",
//        title = "three",
//        artist = "artist name",
//        album = "album title",
//        hasArtwork = false,
//        track = 3,
//        tagged = true
//    ),
//    MusicData(
//        id = 1,
//        path = "null",
//        title = "one",
//        artist = "artist name",
//        album = "album title",
//        hasArtwork = false,
//        track = 1,
//        tagged = true
//    ),
//)
//
//@Preview
//@Composable
//fun AlbumItemPrev() {
//    Column {
//        SimpleAlbumItem(eg) { }
//        SimpleAlbumItem(eg) { }
//    }
//}

