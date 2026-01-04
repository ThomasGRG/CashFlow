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

package jp.ikigai.cash.flow.ui.screenStates.common

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.util.Locale

data class ExportTransactionsScreenState(
    val totalTransactionsCount: Long = 0L,
    val transactionsHashCode: Int = 0,
    val filteredTransactions: Map<LocalDate, List<TransactionWithChips>> = emptyMap(),
    val selectedTransactions: Set<Long> = emptySet(),
    val selectedLocalDates: Set<LocalDate> = emptySet(),
    val allSelected: Boolean = false,
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.entries,
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
