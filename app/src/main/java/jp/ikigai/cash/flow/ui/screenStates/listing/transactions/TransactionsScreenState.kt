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

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.TransactionHeader
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.util.Locale

data class TransactionsScreenState(
    val totalTransactionCount: Long = 0,
    val transactionCount: Long = 0,
    val transactionsHashCode: Int = 0,
    val transactions: Map<TransactionHeader, List<TransactionWithChips>> = emptyMap(),
    val income: String = "",
    val incomeTransactionsCount: String = "0",
    val expense: String = "",
    val expenseTransactionsCount: String = "0",
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.entries,
    val templates: List<SelectTemplateInfoDTO> = emptyList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val sortField: String = "transactionDateTime",
    val sortDirection: SortDirection = SortDirection.DESC,
    val balance: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
