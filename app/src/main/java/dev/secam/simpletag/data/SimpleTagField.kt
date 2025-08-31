package dev.secam.simpletag.data

import dev.secam.simpletag.R

enum class SimpleTagField (val localizedNameRes: Int) {
    Title(R.string.title_field),
    Artist(R.string.artist_field),
    Album(R.string.album_field),
    Year(R.string.year_field),
    Track(R.string.track_field),
    Genre(R.string.genre_field),
    AlbumArtist(R.string.album_artist_field),
    Composer(R.string.composer_field),
    DiscNumber(R.string.disc_number_field),
    Comment(R.string.comment_field)
}