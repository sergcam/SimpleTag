package dev.secam.simpletag.data.media

import kotlinx.serialization.Serializable

@Serializable
data class MusicData(
    val id: Long,
    val path: String,
    val bitrate: Int,
    val duration: Int,
    val title: String,
    val artist: String,
    val album: String,
    val hasArtwork: Boolean? = null,
    val track: Int? = null,
    val tagged: Boolean
)