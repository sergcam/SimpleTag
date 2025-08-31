package dev.secam.simpletag.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import javax.inject.Inject
import androidx.core.graphics.scale

class MusicDataFetcher(private val musicData: MusicData): Fetcher {

    override suspend fun fetch(): FetchResult? {
        val artwork = if (musicData.path == null) null else {
            AudioFileIO.read(File(musicData.path)).tag.firstArtwork?.binaryData
        }

        return if(artwork == null) {
            null
        } else {
            val bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.size)
            ImageFetchResult(
                image = bitmap.scale(120, 120, false).asImage(),
                isSampled = true,
                dataSource = DataSource.DISK
            )
        }
    }

    class Factory @Inject constructor() : Fetcher.Factory<MusicData> {
        override fun create(data: MusicData, options: Options, imageLoader: ImageLoader): Fetcher? {
            return MusicDataFetcher(data)
        }
    }
}

object MusicDataKeyer : Keyer<MusicData> {
    override fun key(data: MusicData, options: Options): String {
        return data.id.toString()
    }
}