package dev.secam.simpletag.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import dev.secam.simpletag.R
import dev.secam.simpletag.data.MusicData
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

@Composable
fun SimpleAlbumArtwork(musicData: MusicData, modifier: Modifier = Modifier) {


    Box(
        modifier = modifier
    ) {
        if (!musicData.hasArtwork) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_music_note_24),
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
    }
}