package dev.secam.simpletag.ui.editor.dialogs

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions
import kotlinx.coroutines.launch

@Composable
fun LogDialog(
    log: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    SimpleDialog(
        title = stringResource(R.string.log),
        onDismiss = onDismissRequest
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .size(300.dp)
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = log,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp)
            )
        }
        SimpleDialogOptions(
            option1 = stringResource(R.string.dialog_close),
            option2 = stringResource(R.string.dialog_copy),
            action1 = { onDismissRequest() },
            action2 = {
                scope.launch {
                    clipboardManager.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText(
                                "plain text",
                                log
                            )
                        )
                    )
                }
            }
        )
    }
}