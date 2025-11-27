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

package dev.secam.simpletag.ui.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.components.SimpleSectionHeader

@Composable
fun FileInfo(musicData: MusicData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column (
        modifier = modifier
    ) {
        SimpleSectionHeader(
            text = stringResource(R.string.file_location),
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = musicData.path,
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .weight(.5f)
            ) {
                SimpleSectionHeader(
                    text = stringResource(R.string.duration),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                val duration = musicData.getDuration(context)
                Text(
                    text = (duration / 60000).toString() + "m " + ((duration / 1000) % 60) + "s",
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Column(
                modifier = Modifier
                    .weight(.5f)
            ) {
                SimpleSectionHeader(
                    text = stringResource(R.string.bitrate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                val bitrate = musicData.getBitrate(context)
                Text(
                    text = (bitrate / 1000).toString() + " kbps",
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}