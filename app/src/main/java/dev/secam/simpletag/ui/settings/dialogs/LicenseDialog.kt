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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        onDismiss = onDismissRequest
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 26.dp)
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
}

@Preview
@Composable
fun LicPrev(){
    LicenseDialog {  }
}