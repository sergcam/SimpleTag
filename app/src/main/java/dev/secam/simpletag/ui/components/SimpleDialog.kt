package dev.secam.simpletag.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.secam.simpletag.ui.theme.SimpleTagTheme
import dev.secam.simpletag.util.SetDialogDim

/**
 * For options use with `SimpleDialogOptions()`
 */
@Composable
fun SimpleDialog(
    title: String,
//    optionsPadding: Boolean = false,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        SetDialogDim()
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
//                    .padding(
//                        paddingValues =
//                            if (optionsPadding) PaddingValues(top = 24.dp, bottom = 16.dp, start = 26.dp, end = 18.dp)
//                            else PaddingValues(vertical = 24.dp,  horizontal = 26.dp,)
//                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(

                                    top = 24.dp,
                                    start = 26.dp,
//                                else PaddingValues(vertical = 24.dp, horizontal = 26.dp)
                        )
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                content()
            }
        }
    }
}

/**
 * for use with `SimpleDialog`
 */
@Composable
fun SimpleDialogOptions(
    option1: String,
    option2: String,
    action1: () -> Unit,
    action2: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp, end = 16.dp)
            .height(40.dp)
    ) {
        TextButton(
            onClick = { action1() },
            ) {
            Text(option1)
        }
        TextButton(
            onClick = { action2() },
        ) {
            Text(option2)
        }
    }
}
@Composable
fun SimpleDialogOptions(
    option: String,
    action: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp, end = 16.dp)
            .height(40.dp)
    ) {
        TextButton(
            onClick = { action },
        ) {
            Text(option)
        }
    }
}

@Preview
@Composable
fun DialogPrev(){
    SimpleTagTheme {
        SimpleDialog(
            title = "Hi",
            onDismiss = {},
//            optionsPadding = true
        ) {
            Text("hihihihiihhihihihiihhihihihiihhihihihiihhihihihiihhihihihiih")
            SimpleDialogOptions(
                "Cancel",


            ) { }
        }
    }
}