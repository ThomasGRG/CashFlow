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

package jp.ikigai.cash.flow.ui.screenStates.listing.transactions

import android.icu.util.Currency
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.getMonthEndDate
import jp.ikigai.cash.flow.utils.getMonthStartDate
import java.time.ZonedDateTime

data class FiltersState(
    val selectedAccounts: Map<Long, Boolean> = emptyMap(),
    val selectedAccountCount: String = "0",
    val selectedCategories: Map<Long, Boolean> = emptyMap(),
    val selectedCategoryCount: String = "0",
    val selectedCounterParties: Map<Long, Boolean> = emptyMap(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val selectedMethods: Map<Long, Boolean> = emptyMap(),
    val selectedMethodCount: String = "0",
    val selectedTransactionTypes: List<TransactionType> = TransactionType.entries,
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val selectedCurrency: String = Currency.getInstance("INR").currencyCode,
    val startDate: ZonedDateTime = getMonthStartDate(),
    val endDate: ZonedDateTime = getMonthEndDate(),
    val startDateString: String = "",
    val endDateString: String = ""
)
