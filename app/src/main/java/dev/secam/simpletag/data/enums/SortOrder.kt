package dev.secam.simpletag.data.enums

import dev.secam.simpletag.R

enum class SortOrder(val displayNameRes: Int) {
    Title(R.string.title_field),
    Artist(R.string.artist_field),
    Album(R.string.album_field),
//    ReleaseDate(),
//    DateAdded(R.string.date_added),
}