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

package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class ChartType(
    @StringRes val label: Int
) {
    CATEGORY_TRANSACTION_COUNT_BAR_CHART(R.string.categories_transaction_count_bar_chart),
    CATEGORY_DEBIT_CREDIT_BAR_CHART(R.string.categories_debit_credit_bar_chart),
    COUNTERPARTY_TRANSACTION_COUNT_BAR_CHART(R.string.counter_parties_transaction_count_bar_chart),
    COUNTERPARTY_DEBIT_CREDIT_BAR_CHART(R.string.counter_parties_debit_credit_bar_chart),
    METHOD_TRANSACTION_COUNT_BAR_CHART(R.string.methods_transaction_count_bar_chart),
    METHOD_DEBIT_CREDIT_BAR_CHART(R.string.methods_debit_credit_bar_chart),
    ACCOUNT_TRANSACTION_COUNT_BAR_CHART(R.string.accounts_transaction_count_bar_chart),
    ACCOUNT_DEBIT_CREDIT_BAR_CHART(R.string.accounts_debit_credit_bar_chart),
    TRANSACTION_TYPE_AMOUNT_BAR_CHART(R.string.transaction_type_amount_bar_chart),
    TEMPLATE_TRANSACTION_COUNT_BAR_CHART(R.string.template_transaction_count_bar_chart),
    CATEGORY_TRENDS_LINE_CHART(R.string.category_trends_line_chart),
    COUNTERPARTY_TRENDS_LINE_CHART(R.string.counterparty_trends_line_chart),
    METHOD_TRENDS_LINE_CHART(R.string.method_trends_line_chart),
    ACCOUNT_TRENDS_LINE_CHART(R.string.account_trends_line_chart);

    fun isLineChart(): Boolean {
        return lineCharts.contains(this)
    }

    companion object {
        val lineCharts = listOf(
            ACCOUNT_TRENDS_LINE_CHART,
            CATEGORY_TRENDS_LINE_CHART,
            COUNTERPARTY_TRENDS_LINE_CHART,
            METHOD_TRENDS_LINE_CHART,
        )
    }
}