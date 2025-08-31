package dev.secam.simpletag.data

import kotlinx.serialization.Serializable
import org.jaudiotagger.tag.images.Artwork

@Serializable
data class MusicData(
    val id: Long,
    val path: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val hasArtwork: Boolean = false
)