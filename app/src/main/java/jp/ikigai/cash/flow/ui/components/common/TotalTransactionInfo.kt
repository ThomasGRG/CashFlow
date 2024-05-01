package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.ArrowUpCircle
import jp.ikigai.cash.flow.ui.components.cards.TotalTransactionInfoCard

@Composable
fun TotalTransactionInfo(
    currency: String,
    expenses: String,
    expensesCount: String,
    income: String,
    incomeCount: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TotalTransactionInfoCard(
            modifier = Modifier.weight(1f),
            currency = currency,
            total = expenses,
            count = expensesCount,
            color = Color(0xFFF44336),
            icon = TablerIcons.ArrowUpCircle
        )
        TotalTransactionInfoCard(
            modifier = Modifier.weight(1f),
            currency = currency,
            total = income,
            count = incomeCount,
            color = Color(0xFF4CAF50),
            icon = TablerIcons.ArrowDownCircle
        )
    }
}

@Preview
@Composable
fun TotalTransactionInfoPreview() {
    TotalTransactionInfo(
        currency = "INR",
        expenses = "341",
        expensesCount = "4",
        income = "112",
        incomeCount = "6"
    )
}