package dev.secam.simpletag.data.media

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import kotlinx.serialization.Serializable

@Serializable
data class MusicData(
    val id: Long,
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val hasArtwork: Boolean? = null,
    val track: Int? = null,
    val tagged: Boolean,
    val duration: Int = -1,
) {
    fun getBitrate(context: Context) : Int {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.getContentUri("external"),
            id
        )
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)!!.toInt()
        return bitrate
    }

    fun getDuration(context: Context) : Int {
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.getContentUri("external"),
            id
        )
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(context, uri)
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toInt()
        return duration
    }
}