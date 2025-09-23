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

package dev.secam.simpletag.data.coil

import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.util.tag.simpleFileReader
import javax.inject.Inject

class MusicDataFetcher(private val musicData: MusicData): Fetcher {
    override suspend fun fetch(): FetchResult? {
        val artwork = simpleFileReader(musicData.path).tag.firstArtwork?.binaryData

        return if(artwork == null) {
            null
        } else {
            val bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.size)
            ImageFetchResult(
                image = bitmap.scale(120, 120, false).asImage(),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        }
    }

    class Factory @Inject constructor() : Fetcher.Factory<MusicData> {
        override fun create(data: MusicData, options: Options, imageLoader: ImageLoader): Fetcher? {
            return MusicDataFetcher(data)
        }
    }
}

