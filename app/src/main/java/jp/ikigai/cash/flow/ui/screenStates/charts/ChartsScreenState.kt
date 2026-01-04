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

package jp.ikigai.cash.flow.ui.screenStates.charts

import androidx.annotation.StringRes
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.enums.ChartType
import java.time.YearMonth
import java.util.Locale

data class ChartsScreenState(
    val locale: Locale? = null,
    val hasData: Boolean = false,
    val chartTypes: List<ChartType> = ChartType.entries,
    val selectedChartType: ChartType = ChartType.CATEGORY_TRANSACTION_COUNT_BAR_CHART,
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val selectedCurrency: String = "INR",
    val yearMonthSelectionStart: YearMonth? = null,
    val yearMonthSelectionEnd: YearMonth? = null,
    val startYearMonthString: String = "",
    val endYearMonthString: String = "",
    @StringRes val yearMonthRangeStringRes: Int = R.string.all_time_date_range_label,
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val selectedAccount: AccountWithTransactionMetadata = AccountWithTransactionMetadata(
        accountId = 0L,
        accountName = "",
        currency = "INR",
        balance = 0.0,
        formattedBalance = "",
        transactionCount = 0L,
        lastUsed = null
    ),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val selectedCategory: CategoryWithTransactionMetadata = CategoryWithTransactionMetadata(
        categoryId = 0L,
        categoryName = "",
        icon = TablerIcons.Archive,
        transactionCount = 0L,
        lastUsed = null
    ),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val selectedCounterParty: CounterPartyWithTransactionMetadata = CounterPartyWithTransactionMetadata(
        counterPartyId = 0L,
        counterPartyName = "",
        transactionCount = 0L,
        lastUsed = null
    ),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val selectedMethod: MethodWithTransactionMetadata = MethodWithTransactionMetadata(
        methodId = 0L,
        methodName = "",
        transactionCount = 0L,
        lastUsed = null
    )
)
