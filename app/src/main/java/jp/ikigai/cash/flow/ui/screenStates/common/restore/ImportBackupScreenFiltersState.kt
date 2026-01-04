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

package jp.ikigai.cash.flow.ui.screenStates.common.restore

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.ZonedDateTime

data class ImportBackupScreenFiltersState(
    val selectedTransactionTypes: List<TransactionType> = TransactionType.entries,
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val selectedCurrencies: Set<String> = Constants.currencyList.map { it.currency.currencyCode }
        .toSet(),
    val selectedCurrencyCount: String = "0",
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val startDateString: String = "",
    val endDateString: String = "",
    @StringRes val dateRangeStringRes: Int = R.string.all_time_date_range_label
)
