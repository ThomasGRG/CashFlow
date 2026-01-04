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

package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.Transaction
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

data class UpsertTransactionScreenState(
    val transaction: Transaction = Transaction(
        transactionId = 0,
        transactionTitle = "",
        transactionDescription = "",
        transactionAmount = 0.0,
        transactionCurrency = "INR",
        transactionType = TransactionType.DEBIT,
        transactionDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
        transactionAccountId = 0,
        transactionCategoryId = 0,
        transactionCounterPartyId = 0,
        transactionMethodId = 0,
        transactionTemplateId = 0
    ),
    val title: String = "",
    val titleValid: Boolean = true,
    val description: String = "",
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val amountValid: Boolean = true,
    val dateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    val dateString: String = "",
    val timeString: String = "",
    val timeValid: Boolean = true,
    val type: TransactionType = TransactionType.DEBIT,
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
    val accountValid: Boolean = true,
    @StringRes val accountErrorStringRes: Int = R.string.field_required_error_label,
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val selectedCategory: CategoryWithTransactionMetadata = CategoryWithTransactionMetadata(
        categoryId = 0L,
        categoryName = "",
        icon = TablerIcons.Archive,
        transactionCount = 0L,
        lastUsed = null
    ),
    val categoryValid: Boolean = true,
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
    ),
    val methodValid: Boolean = true,
    val transactionTitles: List<String> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
