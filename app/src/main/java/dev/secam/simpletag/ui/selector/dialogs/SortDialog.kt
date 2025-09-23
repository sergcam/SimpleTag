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

package dev.secam.simpletag.ui.selector.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.data.enums.SortOrder
import dev.secam.simpletag.data.enums.SortDirection
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions

@Composable
fun SortDialog(
    sortOrder: SortOrder,
    sortDirection: SortDirection,
    onConfirm: (SortOrder, SortDirection) -> Unit,
    onCancel: () -> Unit
) {
    SimpleDialog(
        title = stringResource(R.string.sort_by),
        onDismiss = onCancel,
        manualPadding = true
    ) {
        var newSortOrder by remember { mutableStateOf(sortOrder) }
        var newSortDirection by remember { mutableStateOf(sortDirection) }
        val orderOptions = SortOrder.entries.toList()
        val directionOptions = SortDirection.entries.toList()

        Column(
            modifier = Modifier
                .padding(horizontal = 26.dp)
        ) {
            OrderRadioList(
                sortOrder = sortOrder,
                options = orderOptions
            ) { order ->
                newSortOrder = order
            }
            DirectionButton(
                sortDirection = sortDirection,
                options = directionOptions,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 12.dp)
            ) { direction ->
                newSortDirection = direction
            }
        }
        SimpleDialogOptions(
            option1 = stringResource(R.string.dialog_cancel),
            option2 = stringResource(R.string.dialog_ok),
            action1 = { onCancel() },
            action2 = {
                onCancel()
                onConfirm(newSortOrder, newSortDirection)
            },
            manualPadding = true
        )
    }
}

@Composable
fun OrderRadioList(
    sortOrder: SortOrder,
    options: List<SortOrder>,
    setOrder: (SortOrder) -> Unit
) {
    var selected by remember { mutableStateOf(sortOrder) }
    options.forEach { option ->
        Row(
            Modifier
                .fillMaxWidth()
                .height(46.dp)
                .selectable(
                    selected = (option == selected),
                    onClick = { selected = option },
                    role = Role.RadioButton
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (option == selected),
                onClick = null // null recommended for accessibility with screen readers
            )
            Text(
                text = stringResource(option.displayNameRes),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
    setOrder(selected)
}

@Composable
fun DirectionButton(
    sortDirection: SortDirection,
    options: List<SortDirection>,
    modifier: Modifier = Modifier,
    setDirection: (SortDirection) -> Unit,
) {
    var selected by remember { mutableStateOf(sortDirection) }
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { selected = option },
                selected = option == selected,
                label = { Text(stringResource(option.displayNameRes)) }
            )
        }
    }
    setDirection(selected)
}

@Preview
@Composable
fun SortPrev() {
    SortDialog(
        sortOrder = SortOrder.Title,
        sortDirection = SortDirection.Ascending,
        onConfirm = { one, two -> }
    ) { }
}