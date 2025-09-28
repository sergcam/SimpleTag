package dev.secam.simpletag.ui.editor.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.data.enums.SimpleTagField
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions
import dev.secam.simpletag.ui.editor.components.DialogListItem
import dev.secam.simpletag.ui.editor.components.DialogSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldDialog(entryList: List<SimpleTagField>, onSearch: (String) -> Unit, onAdd: (SimpleTagField) -> Unit, onDismiss: () -> Unit){
    SimpleDialog(
        title = "Add Field",
        onDismiss = onDismiss,
        manualPadding = true
    ) {
        val textFieldState = rememberTextFieldState()
        LaunchedEffect(textFieldState.text) {
            onSearch(textFieldState.text as String)
        }
        DialogSearchBar(textFieldState)
        //  Field list
        Column(
            modifier = Modifier
                .heightIn(max = 240.dp)
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for (entry in entryList) {
                DialogListItem(
                    text = stringResource(entry.displayNameRes)
                ) {
                    onAdd(entry)
                    onDismiss()
                }
            }
        }
        SimpleDialogOptions(
            option = "Cancel",
            action = onDismiss,
            manualPadding = true
        )
    }
}

@Preview
@Composable
fun AddFieldPrev() {
    AddFieldDialog(
        onAdd = {},
        onSearch = {},
        entryList = SimpleTagField.entries.toList()
    ) { }
}