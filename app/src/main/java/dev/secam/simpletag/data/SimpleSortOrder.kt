package dev.secam.simpletag.data

import dev.secam.simpletag.R

enum class SimpleSortOrder(val displayNameRes: Int) {
    Title(R.string.title_field),
    Artist(R.string.artist_field),
    Album(R.string.album_field),
//    ReleaseDate(),
    DateAdded(R.string.date_added),
}