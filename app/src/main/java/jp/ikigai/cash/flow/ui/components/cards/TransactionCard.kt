package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.Stack
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transactionWithIcons: TransactionWithIcons,
    amount: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val itemCount by remember(key1 = transactionWithIcons.itemCount) {
        mutableStateOf(transactionWithIcons.itemCount)
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        if (transactionWithIcons.title.isNotBlank()) {
            Text(
                text = transactionWithIcons.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
            )
        }
        if (transactionWithIcons.description.isNotBlank()) {
            Text(
                text = transactionWithIcons.description,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
                    .alpha(0.6f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
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
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transactionWithIcons.currency,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transactionWithIcons.chips.forEach {
                FilledTonalButton(onClick = onClick) {
                    Icon(
                        imageVector = it.second,
                        contentDescription = it.second.name
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = it.first, style = MaterialTheme.typography.titleMedium)
                }
            }
            if (itemCount != 0) {
                FilledTonalButton(onClick = onClick) {
                    Icon(
                        imageVector = TablerIcons.Stack,
                        contentDescription = TablerIcons.Stack.name,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "$itemCount items", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Preview
@Composable
fun TransactionCardPreview() {
    TransactionCard(
        transactionWithIcons = TransactionWithIcons(
            title = "Test",
            description = "Desc",
            amount = 423.09,
            chips = listOf(
                Pair("Category", TablerIcons.Archive),
                Pair("Category", TablerIcons.Archive),
                Pair("Category", TablerIcons.Archive),
                Pair("Category", TablerIcons.Archive),
            ),
        ),
        amount = "423.09",
        onClick = {},
        onLongClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}