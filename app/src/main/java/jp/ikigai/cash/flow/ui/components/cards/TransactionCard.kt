package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(
                    5.dp,
                    alignment = Alignment.CenterVertically
                )
            ) {
                transactionWithIcons.chips.forEach {
                    FilledTonalButton(
                        onClick = onClick,
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
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionCardPreview() {
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
    }
}