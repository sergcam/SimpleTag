package dev.secam.simpletag.ui.editor.dialogs

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditorSheet(
    songTitle: String,
    artist: String,
    lyrics: String,
    setLyrics: (String?) -> Unit,
    onDismiss: () -> Unit,
){
    val scope = rememberCoroutineScope()
    val lyricsState = rememberTextFieldState(lyrics)
    val lyricsActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val receivedLyrics = result.data?.getStringExtra("lyrics")
                if (!receivedLyrics.isNullOrBlank()) {
                    lyricsState.setTextAndPlaceCursorAtEnd(receivedLyrics)
                } else {
                    scope.launch {
//                        TODO: on lyrics retrieve fail
                    }
                }
            }

            Activity.RESULT_CANCELED -> {
                scope.launch {
//                    TODO: on lyrics retrieve cancel
                }
            }

            else -> {
                scope.launch {
//                    TODO: unknown error
                }
            }
        }
    }

    val lyricsRetrieveIntent = Intent("android.intent.action.SEND").apply {
        putExtra("songName", songTitle)
        putExtra("artistName", artist)
        type = "text/plain"
        setPackage("pl.lambada.songsync")
    }

    fun launchLyricsRetrieveIntent() {
        try {
            lyricsActivityLauncher.launch(lyricsRetrieveIntent)
        } catch (e: Exception) {
//            TODO: SongSync not installed
//            when (e) {
//                is ActivityNotFoundException -> showInstallSongSyncDialog = true
//                else -> scope.launch {
//                    sonner.show(
//                        message = context.getString(R.string.something_unexpected_occurred),
//                        type = ToastType.Error
//                    )
//                }
//            }
        }
    }
    val sheetState = rememberModalBottomSheetState(true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                state = lyricsState,
                placeholder = {Text(stringResource(R.string.lyrics_field))},
                lineLimits = TextFieldLineLimits.MultiLine(6,16),
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(bottom = 24.dp)
            )
            TextButton(
                onClick = {launchLyricsRetrieveIntent()},
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 8.dp)
            ){
                Icon(
                    painter = painterResource(R.drawable.ic_lyrics_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                )
                Text(
                    text = "Fetch Lyrics with SongSync",
                    fontWeight = FontWeight.Bold
                )


            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        setLyrics(null)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(.5f, false)
                        .fillMaxWidth(.98f)

                ) {
                    Icon(
                        painter = painterResource((R.drawable.ic_delete_24px)),
                        contentDescription = stringResource(R.string.cd_delete_cover_icon),
                        modifier = Modifier
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.delete_lyrics),
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        setLyrics(lyricsState.text as String)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(.5f, false)
                        .fillMaxWidth(.98f)
                ) {
                    Icon(
                        painter = painterResource((R.drawable.ic_save_24px)),
                        contentDescription = stringResource(R.string.cd_add_cover_icon),
                        modifier = Modifier
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.save_lyrics),
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    }
}
@Preview
@Composable
fun PrevLyrics(){
//    LyricsEditor("numb", "linkin park",) { }
}