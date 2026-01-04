/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
    expenses: String,
    expensesCount: String,
    income: String,
    incomeCount: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TotalTransactionInfoCard(
            modifier = Modifier.weight(1f),
            total = expenses,
            count = expensesCount,
            color = Color(0xFFF44336),
            icon = TablerIcons.ArrowUpCircle
        )
        TotalTransactionInfoCard(
            modifier = Modifier.weight(1f),
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
        expenses = "341",
        expensesCount = "4",
        income = "112",
        incomeCount = "6"
    )
}