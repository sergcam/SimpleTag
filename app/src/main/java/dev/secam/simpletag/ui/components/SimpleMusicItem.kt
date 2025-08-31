package dev.secam.simpletag.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.data.MusicData
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.audio.mp3.MP3FileReader
import java.io.File

@Composable
fun SimpleMusicItem(musicData: MusicData, modifier: Modifier = Modifier, onClick: () -> Unit){
    if(musicData.title == null){
        ListItem(
            headlineContent = {}
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
            modifier = modifier
                .clickable(
                    enabled = true,
                    onClick = onClick
                )
//        colors =


        )
    }
}