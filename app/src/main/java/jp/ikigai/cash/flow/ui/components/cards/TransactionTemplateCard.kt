package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import jp.ikigai.cash.flow.data.enums.TransactionType

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionTemplateCard(
    transactionTemplateWithIcons: TransactionTemplateWithIcons,
    amount: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val itemCount by remember(key1 = transactionTemplateWithIcons.itemCount) {
        mutableStateOf(transactionTemplateWithIcons.itemCount)
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
        Text(
            text = transactionTemplateWithIcons.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
        )
        if (transactionTemplateWithIcons.title.isNotBlank()) {
            Text(
                text = transactionTemplateWithIcons.title,
                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
            )
        }
        if (transactionTemplateWithIcons.description.isNotBlank()) {
            Text(
                text = transactionTemplateWithIcons.description,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
                    .alpha(0.6f)
            )
        }
        if (transactionTemplateWithIcons.amount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = transactionTemplateWithIcons.typeIcon,
                    contentDescription = "type icon",
                    tint = transactionTemplateWithIcons.typeIconColor,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transactionTemplateWithIcons.currency,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transactionTemplateWithIcons.chips.forEach {
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
fun TransactionTemplateCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TransactionTemplateCard(
            transactionTemplateWithIcons = TransactionTemplateWithIcons(
                uuid = "",
                name = "Name",
                title = "template.title",
                description = "template.description",
                amount = 12.0,
                typeIcon = TransactionType.DEBIT.icon,
                typeIconColor = TransactionType.DEBIT.color,
                frequency = 0,
                currency = "",
                itemCount = 0,
                chips = emptyList()
            ),
            amount = "12",
            onClick = {},
            onLongClick = {}
        )
        TransactionTemplateCard(
            transactionTemplateWithIcons = TransactionTemplateWithIcons(
                uuid = "",
                name = "Name",
                title = "template.title",
                description = "template.description",
                amount = 0.0,
                typeIcon = TransactionType.DEBIT.icon,
                typeIconColor = TransactionType.DEBIT.color,
                frequency = 0,
                currency = "",
                itemCount = 0,
                chips = emptyList()
            ),
            amount = "0",
            onClick = {},
            onLongClick = {}
        )
        TransactionTemplateCard(
            transactionTemplateWithIcons = TransactionTemplateWithIcons(
                uuid = "",
                name = "template",
                title = "template.title",
                description = "template.description",
                amount = 12.0,
                typeIcon = TransactionType.DEBIT.icon,
                typeIconColor = TransactionType.DEBIT.color,
                frequency = 0,
                currency = "INR",
                itemCount = 1,
                chips = emptyList()
            ),
            amount = "12",
            onClick = {},
            onLongClick = {}
        )
        TransactionTemplateCard(
            transactionTemplateWithIcons = TransactionTemplateWithIcons(
                uuid = "",
                name = "template",
                title = "template.title",
                description = "template.description",
                amount = 12.0,
                typeIcon = TransactionType.DEBIT.icon,
                typeIconColor = TransactionType.DEBIT.color,
                frequency = 1,
                currency = "INR",
                itemCount = 1,
                chips = listOf(
                    Pair("category", TablerIcons.Archive)
                )
            ),
            amount = "12",
            onClick = {},
            onLongClick = {}
        )
    }
}