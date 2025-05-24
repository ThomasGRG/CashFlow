package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import java.util.Locale

data class UpsertCounterPartyScreenState(
    val counterParty: CounterParty = CounterParty(),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val transactionCount: Int = 0,
    val formattedTransactionCount: String = "",
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
