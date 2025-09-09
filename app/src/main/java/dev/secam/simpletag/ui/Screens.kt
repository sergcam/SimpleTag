package dev.secam.simpletag.ui

import dev.secam.simpletag.data.MusicData
import kotlinx.serialization.Serializable


@Serializable
object Selector

@Serializable
object Settings

@Serializable
object About

@Serializable
data class Editor(
    val musicList: List<MusicData>
)



