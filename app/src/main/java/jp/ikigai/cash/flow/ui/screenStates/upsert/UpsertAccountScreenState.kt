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
