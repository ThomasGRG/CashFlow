package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Method
import java.util.Locale

data class UpsertMethodScreenState(
    val method: Method = Method(),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val transactionCount: String = "",
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val writeOngoing: Boolean = false,
    val locale: Locale? = null
)
