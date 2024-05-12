package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCard(
    title: String,
    frequency: String,
    pricePerUnit: String,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            )
            Row(
                modifier = Modifier.padding(end = 4.dp)
            ) {
                FilledTonalButton(onClick = onClick) {
                    Text(
                        text = stringResource(id = R.string.frequency_of_use_label, frequency),
                    )
                }
            }
            if (pricePerUnit.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    FilledTonalButton(onClick = onClick) {
                        Text(
                            text = stringResource(id = R.string.last_known_price_label, pricePerUnit),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ItemCardPreview() {
    ItemCard(title = "Title", frequency = "2", onClick = {}, pricePerUnit = "90.0 INR/g")
}