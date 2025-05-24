package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
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
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ChartLine
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.CommonListingDTO
import jp.ikigai.cash.flow.ui.components.common.CustomChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoCard(
    modifier: Modifier,
    data: CommonListingDTO,
    onClick: (Long) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(data.id)
        },
        modifier = modifier
            .fillMaxWidth()
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
                    contentDescription = data.icon.name
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                data.chips.forEach {
                    CustomChip(
                        icon = it.icon,
                        label = stringResource(id = it.resId, it.value)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun InfoCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoCard(
            modifier = Modifier,
            data = CommonListingDTO(
                id = 0L,
                annotatedName = AnnotatedString("Food & Drinks"),
                icon = TablerIcons.Archive,
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
        InfoCard(
            modifier = Modifier,
            data = CommonListingDTO(
                id = 0L,
                annotatedName = AnnotatedString("Food & Drinks ae fef a efa ef ae gae gae afe"),
                icon = TablerIcons.Archive,
                chips = listOf(
                    ChipInfo(
                        resId = R.string.frequency_of_use_label,
                        value = "3204",
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
        InfoCard(
            modifier = Modifier,
            data = CommonListingDTO(
                id = 0L,
                annotatedName = AnnotatedString("Food & Drinksasdgefeadedawdawdesfaedaefgawdaefe"),
                icon = TablerIcons.Archive,
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