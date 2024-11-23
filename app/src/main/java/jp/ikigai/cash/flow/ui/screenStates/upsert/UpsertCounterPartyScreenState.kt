package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.CounterParty

data class UpsertCounterPartyScreenState(
    val counterParty: CounterParty = CounterParty(),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
