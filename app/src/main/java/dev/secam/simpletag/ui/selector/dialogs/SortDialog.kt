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
import dev.secam.simpletag.data.SimpleSortOrder
import dev.secam.simpletag.data.SortDirection
import dev.secam.simpletag.ui.components.SimpleDialog
import dev.secam.simpletag.ui.components.SimpleDialogOptions

@Composable
fun SortDialog(
    sortOrder: SimpleSortOrder,
    sortDirection: SortDirection,
    onConfirm: (SimpleSortOrder, SortDirection) -> Unit,
    onCancel: () -> Unit
) {
    SimpleDialog(
        title = "Sort by",
        onDismiss = onCancel
    ) {
        var newSortOrder by remember { mutableStateOf(sortOrder) }
        var newSortDirection by remember { mutableStateOf(sortDirection) }
        val orderOptions = SimpleSortOrder.entries.toList()
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
                    .padding(top = 16.dp)
            ) { direction ->
                newSortDirection = direction
            }
        }
        SimpleDialogOptions(
            option1 = "Cancel",
            option2 = "OK",
            action1 = { onCancel() },
            action2 = {
                onCancel()
                onConfirm(newSortOrder, newSortDirection)
            }
        )
    }
}

@Composable
fun OrderRadioList(
    sortOrder: SimpleSortOrder,
    options: List<SimpleSortOrder>,
    setOrder: (SimpleSortOrder) -> Unit
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
        sortOrder = SimpleSortOrder.Title,
        sortDirection = SortDirection.Ascending,
        onConfirm = { one, two -> }
    ) { }
}