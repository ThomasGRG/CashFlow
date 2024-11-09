package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
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
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.Tag
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.ItemListingDTO
import jp.ikigai.cash.flow.data.enums.ItemUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCard(
    modifier: Modifier,
    data: ItemListingDTO,
    onClick: (ItemListingDTO) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(data)
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
            Text(
                text = data.annotatedName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.Top
            ) {
                if (data.price.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick(data)
                        },
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.Tag,
                            contentDescription = "price icon",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = stringResource(
                                id = R.string.last_known_price_label,
                                data.price,
                                stringResource(id = data.unit.code)
                            )
                        )
                    }
                }
                data.chips.forEach {
                    FilledTonalButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick(data)
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
fun ItemCardPreview() {
    ItemCard(
        modifier = Modifier,
        data = ItemListingDTO(
            uuid = "",
            annotatedName = AnnotatedString("Title"),
            price = "90.0 INR",
            unit = ItemUnit.GRAM,
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
        onClick = {}
    )
}