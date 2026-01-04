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
import jp.ikigai.cash.flow.Account
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo

data class UpsertAccountScreenState(
    val account: Account = Account(
        accountId = 0L,
        accountName = "",
        currency = "INR",
        balance = 0.0
    ),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val balance: String = "0",
    val balanceValid: Boolean = true,
    val selectedCurrency: String = "INR",
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val hasUnsavedChanges: Boolean = false,
    val hasTransactions: Boolean = false,
    val loading: Boolean = true,
    val enabled: Boolean = false
)
