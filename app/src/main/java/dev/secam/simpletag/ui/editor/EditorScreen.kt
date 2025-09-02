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

package dev.secam.simpletag.ui.editor

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.secam.simpletag.R
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.data.SimpleTagField
import dev.secam.simpletag.ui.components.SimpleTopBar
import org.jaudiotagger.tag.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    musicList: List<MusicData>,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // prefs
    val prefs = viewModel.prefState.collectAsState().value
    val advancedEditor = prefs.advancedEditor
    val roundCovers = prefs.roundCovers

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Edit Tag",
                actionIcon = painterResource(R.drawable.ic_save_24px),
                contentDescription = "save tag",
                onBack = onNavigateBack,
                action = {},
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        if(musicList.size == 1) {
            SingleEditor(
                tag = viewModel.getTag( musicList[0]),
                advancedEditor = advancedEditor,
                roundCovers = roundCovers,
                modifier = modifier
                    .padding(contentPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
            )
        }
        else {
            BatchEditor(musicList)
        }
    }
}

@Composable
fun SingleEditor(tag: Tag?, advancedEditor: Boolean, roundCovers: Boolean, modifier: Modifier = Modifier){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()

    ) {
        Box() {
            if (tag?.firstArtwork != null) {
                Image(
                    bitmap = BitmapFactory.decodeByteArray(
                        tag.firstArtwork.binaryData,
                        0,
                        tag.firstArtwork.binaryData.size
                    ).asImageBitmap(),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(
                            shape =
                                if(roundCovers) RoundedCornerShape(16.dp)
                                else RectangleShape
                        )
                )
            }
            Row {
                IconButton(
                    onClick = {},
//                modifier = TODO(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    //                shape = TODO(),

                ) {
                    Icon(
                        painter = painterResource((R.drawable.ic_photo_24px)),
                        contentDescription = null //TODO
                    )
                }
                IconButton(
                    onClick = {},
//                modifier = TODO(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
//                shape = TODO(),
                ) {
                    Icon(
                        painter = painterResource((R.drawable.ic_delete_24px)),
                        contentDescription = null //TODO
                    )
                }
            }
        }
        val textStates: Map<SimpleTagField,TextFieldState> = buildMap {
            for(field in SimpleTagField.entries){
                put(field, rememberTextFieldState(tag?.getFirst( field.fieldKey) ?: ""))
            }
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp)
        ){
            for (field in textStates) {
                if(field.key.ordinal < 10){
                    SimpleTextField(
                        state = field.value,
                        label = stringResource(field.key.localizedNameRes),
                        modifier = Modifier
                            .padding(top = 8.dp)
                    )
                }
            }
            if(advancedEditor) {
                Text(
                    text = stringResource(R.string.advanced_fields),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(top = 28.dp)
                )
                for (field in textStates){
                    if (field.key.ordinal > 9) {
                        SimpleTextField(
                            state = field.value,
                            label = stringResource(field.key.localizedNameRes),
                            modifier = Modifier
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatchEditor(musicList: List<MusicData>){
    //TODO
}
@Composable
fun SimpleTextField(
    state: TextFieldState,
    label: String,
    modifier: Modifier = Modifier
){
    OutlinedTextField(
        state = state,
        label = {
            Text(
                text = label
            )
        },
        modifier = modifier
            .fillMaxWidth()
    )
}
//
//@Preview
//@Composable
//fun EditorPrev(){
//    SimpleTagTheme {
//        EditorScreen()
//    }
//}