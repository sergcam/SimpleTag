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

package dev.secam.simpletag.util.tag

import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.reference.PictureTypes
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag
import org.jaudiotagger.tag.vorbiscomment.util.Base64Coder

fun FlacTag.setArtworkField(artwork: Artwork) {
    setField(
        createArtworkField(
            /* imageData = */ artwork.binaryData,
            /* pictureType = */ PictureTypes.DEFAULT_ID,
            /* mimeType = */ artwork.mimeType,
            /* description = */ "cover",
            /* width = */ artwork.width,
            /* height = */ artwork.height,
            /* colourDepth = */ 24,
            /* indexedColouredCount = */ 0
        )
    )
}

fun VorbisCommentTag.setArtworkField(artwork: Artwork) {
    val imageData = artwork.binaryData
    val testdata = Base64Coder.encode(imageData)
    val base64image = String(testdata)
    setField(createField(VorbisCommentFieldKey.COVERART,base64image))
    setField(createField(VorbisCommentFieldKey.COVERARTMIME,artwork.mimeType))
}