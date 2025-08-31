package dev.secam.simpletag.ui.selector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.components.SimpleMusicItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun ListScreen(
    musicList: List<MusicData>,
    modifier: Modifier = Modifier,
    onNavigateToEditor: (List<MusicData>) -> Unit,
    updateHasArt: (Int) -> Unit
) {
//    var musicList by remember { mutableStateOf(musicList) }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
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

                    if(!musicList[index].hasArtwork){
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



