/*
 * Copyright (C) 2025  Sergio Camacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.secam.simpletag.data

import dev.secam.simpletag.R
import org.jaudiotagger.tag.FieldKey

enum class SimpleTagField (val localizedNameRes: Int, val fieldKey: FieldKey) {
    Title(R.string.title_field, FieldKey.TITLE),
    Artist(R.string.artist_field, FieldKey.ARTIST),
    Album(R.string.album_field, FieldKey.ALBUM),
    Year(R.string.year_field, FieldKey.YEAR),
    Track(R.string.track_field, FieldKey.TRACK),
    Genre(R.string.genre_field, FieldKey.GENRE),
    AlbumArtist(R.string.album_artist_field, FieldKey.ALBUM_ARTIST),
    Composer(R.string.composer_field, FieldKey.COMPOSER),
    DiscNumber(R.string.disc_number_field, FieldKey.DISC_NO),
    Comment(R.string.comment_field, FieldKey.COMMENT)
}