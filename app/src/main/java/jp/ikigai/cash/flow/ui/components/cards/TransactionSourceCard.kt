package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.ChartLine
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SourceListingDTO

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionSourceCard(
    modifier: Modifier,
    data: SourceListingDTO,
    onClick: (String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(data.uuid)
        },
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = data.annotatedName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 10.dp)
                )
                Icon(
                    imageVector = data.icon,
                    contentDescription = data.icon.name,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.98f)
                )
            }
            Text(
                text = data.balance,
                style = MaterialTheme.typography.displaySmall
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.Top
            ) {
                data.chips.forEach {
                    FilledTonalButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick(data.uuid)
                        },
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.icon.name,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = stringResource(id = it.resId, it.value),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionSourceCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TransactionSourceCard(
            modifier = Modifier,
            data = SourceListingDTO(
                uuid = "",
                annotatedName = AnnotatedString("Food & Drinks"),
                icon = TablerIcons.BuildingBank,
                currency = "INR",
                balance = "45.34",
                chips = listOf(
                    ChipInfo(
                        resId = R.string.frequency_of_use_label,
                        value = "0",
                        icon = TablerIcons.ChartLine
                    ),
                    ChipInfo(
                        resId = R.string.frequency_of_use_label,
                        value = "0",
                        icon = TablerIcons.ChartLine
                    )
                )
            ),
            onClick = {},
        )
    }
}