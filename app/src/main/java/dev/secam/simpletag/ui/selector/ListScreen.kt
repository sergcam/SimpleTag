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

package dev.secam.simpletag.ui.selector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.components.SimpleMusicItem


@Composable
fun ListScreen(
    musicList: List<MusicData>,
    modifier: Modifier = Modifier,
    onNavigateToEditor: (List<MusicData>) -> Unit,
    updateHasArt: (Int) -> Unit
) {
//    var musicList by remember { mutableStateOf(musicList) }
//    val scope = rememberCoroutineScope()

//    if(musicList.isNotEmpty()) {
//        if(musicList[musicList.size-1].title == null){
//            loading = false
//        }
//    }


    Column(
        modifier = modifier
    ) {
        if (musicList.isEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                items(
                    count = musicList.size,
                    key = { musicList[it].id },
                    contentType = { MusicData }
                ) { index ->

//                    LaunchedEffect(Unit) {
////                            scope.launch {
//                        if (musicList[index].title == null) {
//                            loadTagData(index)
//                        }
////
//                    }
//                    }

                    if(musicList[index].hasArtwork == null){
                        updateHasArt(index)
                    }
                    SimpleMusicItem(musicList[index]) {
                        onNavigateToEditor(listOf(musicList[index]))
                    }
//                    }
                }
            }
        }
    }
}



