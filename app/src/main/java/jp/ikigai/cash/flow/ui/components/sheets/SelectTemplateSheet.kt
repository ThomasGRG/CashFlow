package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTemplateSheet(
    templates: List<TransactionTemplate>,
    addNewTransaction: (String) -> Unit,
    dismiss: () -> Unit,
    maxHeight: Double,
    sheetState: SheetState,
) {
    val scope = rememberCoroutineScope()

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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight.dp)
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = templates,
                key = { template -> template.uuid }
            ) { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            enabled = true,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    dismiss()
                                    addNewTransaction(template.uuid)
                                }
                            }
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
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