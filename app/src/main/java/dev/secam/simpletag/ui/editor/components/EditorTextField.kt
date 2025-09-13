package dev.secam.simpletag.ui.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditorTextField(
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