package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTemplateSheet(
    templates: List<TransactionTemplate>,
    addNewTransaction: (String) -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
) {
    val scope = rememberCoroutineScope()

    val rowCount by remember(key1 = templates) {
        mutableStateOf(
            (templates.size / 2).coerceIn(minimumValue = 1, maximumValue = 4)
        )
    }

    val maxHeight by remember(key1 = rowCount) {
        mutableStateOf(rowCount * 50.0)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                dismiss()
            }
        },
        shape = RoundedCornerShape(10)
    ) {
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(rowCount),
            horizontalItemSpacing = 6.dp,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .heightIn(max = maxHeight.dp)
                .padding(start = 10.dp, end = 10.dp)
        ) {
            items(
                items = templates,
                key = { template -> template.uuid }
            ) { template ->
                ToggleButton(
                    label = template.name,
                    selected = false,
                    toggle = {
                        scope.launch {
                            sheetState.hide()
                            dismiss()
                            addNewTransaction(template.uuid)
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        addNewTransaction("")
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.new_transaction_button_label))
            }
        }
    }
}