package dev.secam.simpletag.data.media

import kotlinx.serialization.Serializable

@Serializable
data class MusicData(
    val id: Long,
    val path: String,
    val bitrate: Int,
    val duration: Int,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val hasArtwork: Boolean? = null,
    val tagged: Boolean
)