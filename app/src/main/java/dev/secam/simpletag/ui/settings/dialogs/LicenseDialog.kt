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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions

@Composable
fun LicenseDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit
) {
    SimpleDialog(
        title = stringResource(R.string.license),
        optionsPadding = true,
        onDismiss = onDismissRequest
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .size(300.dp)
        ) {
            Text(
                text = stringResource(R.string.gplv3_fulltext),
                modifier = modifier
                    .verticalScroll(rememberScrollState())
            )
        }
        SimpleDialogOptions(
            option = stringResource(R.string.dialog_close)
        ) { onDismissRequest }
    }

    Dialog(onDismissRequest = { onDismissRequest() }) {
//        SetDialogDim()
        Card(
            modifier = modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
                modifier = modifier.padding(horizontal = 26.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.license),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = modifier.padding(bottom = 26.dp),
                    )

                }
                Row(
                    modifier = modifier
                        .padding(top = 12.dp, bottom = 14.dp)
                ) {
                    TextButton(
                        onClick = { onDismissRequest() }
                    ) {
                        Text(stringResource(R.string.dialog_close))
                    }
                }
            }
        }
    }
}