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

package dev.secam.simpletag.ui.settings.dialogs

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.enums.FolderSelectMode
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions
import dev.secam.simpletag.ui.components.SimpleSectionHeader
import dev.secam.simpletag.util.uriToPath

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilePickerDialog(
    selectMode: FolderSelectMode,
    setSelectionMode: (FolderSelectMode) -> Unit,
    onDismiss: () -> Unit,
    setUriList: (Set<String>) -> Unit,
    uriList: Set<String>,
    updateFilters: (Set<String>, FolderSelectMode) -> Unit
) {
    val uriListTemp = remember { mutableStateListOf(*uriList.toTypedArray()) }
    val filePickerActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.also { uri ->
                    uriListTemp.add(uriToPath(uri.data!!))
                }
            }
            Activity.RESULT_CANCELED -> {

            }

            else -> {

            }
        }
    }
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    var selected by remember { mutableStateOf(selectMode) }
    val options = FolderSelectMode.entries.toList()
    SimpleDialog(
        "Choose Folders",
        manualPadding = true,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 26.dp)
        ) {
            /*----- Selection Mode -----*/
            SimpleSectionHeader(
                text = "Selection Mode",
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = { selected = option },
                        selected = option == selected,
                        label = {
                            Text(
                                text = stringResource(option.displayNameRes),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
            /*----- Select Folders -----*/
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SimpleSectionHeader(
                    text = stringResource(R.string.select_folders)
                )
                IconButton(
                    onClick = {
                        filePickerActivityLauncher.launch(intent)

                    },
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_24px),
                        contentDescription = stringResource(R.string.cd_add_folder),
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                if (uriListTemp.isEmpty()) {
                    Text(
                        text = "No Folders Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else
                    for (uri in uriListTemp) {
                        val path = uriFormatter(uri)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                path,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier
                                    .fillMaxWidth(.8f)

                            )
                            IconButton(
                                onClick = { uriListTemp.remove(uri) },
//                                    modifier = Modifier
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete_24px),
                                    contentDescription = stringResource(R.string.cd_delete),

                                    )
                            }
                        }
                    }
            }
        }
        SimpleDialogOptions(
            option1 = stringResource(R.string.dialog_cancel),
            option2 = stringResource(R.string.dialog_save),
            manualPadding = true,
            action1 = onDismiss,
            action2 = {
                setSelectionMode(selected)
                setUriList(uriListTemp.toSet())
                updateFilters(uriListTemp.toSet(), selected)
                onDismiss()
            }
        )

    }

}

fun uriFormatter(uri: String): String {
    //TODO check sd card path
    val path = uri.substringAfter("/storage/emulated/0/")
    return "Internal Storage/$path"
}