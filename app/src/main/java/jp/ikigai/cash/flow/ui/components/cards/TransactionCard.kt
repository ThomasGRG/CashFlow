package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.ui.components.common.CustomChip

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transactionWithIcons: TransactionWithIcons,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = transactionWithIcons.annotatedTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (transactionWithIcons.annotatedDescription.text.isNotBlank()) {
                Text(
                    text = transactionWithIcons.annotatedDescription,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.6f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = transactionWithIcons.typeIcon,
                    contentDescription = "type icon",
                    tint = transactionWithIcons.typeIconColor,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = transactionWithIcons.amount,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                transactionWithIcons.chips.forEach {
                    CustomChip(
                        icon = it.icon,
                        label = stringResource(id = it.resId, it.value)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    checked: Boolean,
    enabled: Boolean,
    transactionWithIcons: TransactionWithIcons,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val alpha by remember(key1 = enabled) {
        mutableFloatStateOf(if (enabled) 1f else 0.38f)
    }

    val subAlpha by remember(key1 = enabled) {
        mutableFloatStateOf(if (enabled) 0.6f else 0.38f)
    }

    val animatedIconColor by animateColorAsState(
        targetValue = if (enabled) {
            transactionWithIcons.typeIconColor
        } else {
            MaterialTheme.colorScheme.onBackground.copy(0.38f)
        },
        label = "animated icon color"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = enabled
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transactionWithIcons.annotatedTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .alpha(alpha)
                )
                Checkbox(
                    checked = checked,
                    onCheckedChange = null,
                    enabled = enabled,
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp)
                )
            }
            if (transactionWithIcons.annotatedDescription.text.isNotBlank()) {
                Text(
                    text = transactionWithIcons.annotatedDescription,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(subAlpha)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = transactionWithIcons.typeIcon,
                    contentDescription = "type icon",
                    tint = animatedIconColor,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = transactionWithIcons.amount,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(alpha)
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                transactionWithIcons.chips.forEach {
                    CustomChip(
                        enabled = enabled,
                        icon = it.icon,
                        label = stringResource(id = it.resId, it.value)
                    )
                }
            }
        }
    }
}

@Preview(device = "spec:width=2340px,height=2340px,dpi=440")
@Composable
fun TransactionCardPreview() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
        ) {
            TransactionCard(
                checked = false,
                enabled = true,
                transactionWithIcons = TransactionWithIcons(
                    annotatedTitle = AnnotatedString("Test askjdef anedkufb awindas uifb urnit"),
                    annotatedDescription = AnnotatedString("Desc"),
                    amount = "423.09",
                    chips = listOf(
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                    ),
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            TransactionCard(
                checked = false,
                enabled = false,
                transactionWithIcons = TransactionWithIcons(
                    annotatedTitle = AnnotatedString("Test askjdef anedkufb awindas uifb urnit"),
                    annotatedDescription = AnnotatedString("Desc"),
                    amount = "423.09",
                    chips = listOf(
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                    ),
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TransactionCard(
                transactionWithIcons = TransactionWithIcons(
                    annotatedTitle = AnnotatedString("Test"),
                    annotatedDescription = AnnotatedString("Desc"),
                    amount = "423.09",
                    chips = listOf(
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                    ),
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            TransactionCard(
                checked = true,
                enabled = true,
                transactionWithIcons = TransactionWithIcons(
                    annotatedTitle = AnnotatedString("Test askjdef anedkufb awindas uifb urnit"),
                    annotatedDescription = AnnotatedString("Desc"),
                    amount = "423.09",
                    chips = listOf(
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                    ),
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            TransactionCard(
                checked = true,
                enabled = false,
                transactionWithIcons = TransactionWithIcons(
                    annotatedTitle = AnnotatedString("Test askjdef anedkufb awindas uifb urnit"),
                    annotatedDescription = AnnotatedString("Desc"),
                    amount = "423.09",
                    chips = listOf(
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                        ChipInfo(R.string.placeholder, "Category", TablerIcons.Archive),
                    ),
                ),
                onClick = {},
                onLongClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}