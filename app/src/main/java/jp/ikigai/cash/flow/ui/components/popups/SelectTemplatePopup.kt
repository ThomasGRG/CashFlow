package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow

@Composable
fun SelectTemplatePopup(
    templates: List<TransactionTemplate>,
    addNewTransaction: (String) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = templates,
                key = { template -> "template-${template.uuid}" }
            ) { template ->
                ToggleRow(
                    identifier = template.uuid,
                    label = template.name,
                    selected = false,
                    onClick = { templateUUID ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        addNewTransaction(templateUUID)
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    addNewTransaction("")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.new_transaction_button_label))
            }
        }
    }
}

@Preview
@Composable
fun SelectTemplatePopupPreview() {
    SelectTemplatePopup(
        templates = listOf(
            TransactionTemplate().apply {
                uuid = "qwe"
                name = "Split expenses"
            },
            TransactionTemplate().apply {
                uuid = "asd"
                name = "Server"
            },
            TransactionTemplate().apply {
                uuid = "zx"
                name = "Al Taza"
            },
            TransactionTemplate().apply {
                uuid = "dgh"
                name = "Groceries"
            }
        ),
        addNewTransaction = {},
        dismiss = {}
    )
}