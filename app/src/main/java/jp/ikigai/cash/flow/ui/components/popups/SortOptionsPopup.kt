package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.SelectableCard

@Composable
fun SortOptionsPopup(
    selectedOption: String,
    selectedDirection: Sort,
    options: Map<String, String>,
    sort: (String, Sort) -> Unit,
    dismiss: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    var option by remember {
        mutableStateOf(selectedOption)
    }

    var direction by remember {
        mutableStateOf(selectedDirection)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            options.forEach {
                SelectableCard(
                    checked = { it.value == option },
                    label = buildAnnotatedString { append(it.key) },
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        option = it.value
                    },
                    modifier = Modifier
                )
            }
        }
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = direction == Sort.ASCENDING,
                onClick = {
                    direction = Sort.ASCENDING
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(text = "Ascending")
            }
            SegmentedButton(
                selected = direction == Sort.DESCENDING,
                onClick = {
                    direction = Sort.DESCENDING
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(text = "Descending")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sort(option, direction)
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.sort_button_label))
            }
        }
    }
}

@Preview
@Composable
fun SortOptionsPopupPreview() {
    SortOptionsPopup(
        selectedOption = "Frequency",
        selectedDirection = Sort.DESCENDING,
        options = mapOf(
            "Frequency" to "frequency",
            "Last used" to "lastUsed"
        ),
        sort = { _, _ -> },
        dismiss = {}
    )
}