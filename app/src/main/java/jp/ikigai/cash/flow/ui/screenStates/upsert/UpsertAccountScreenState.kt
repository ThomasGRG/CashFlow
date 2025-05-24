package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.store.entity.Account

data class UpsertAccountScreenState(
    val account: Account = Account(currency = "INR"),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val hasTransactions: Boolean = false,
    val loading: Boolean = true,
    val enabled: Boolean = false
)
