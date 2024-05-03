package jp.ikigai.cash.flow.ui.screenStates.upsert

import android.icu.util.Currency
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.entity.Source

data class UpsertSourceScreenState(
    val source: Source = Source(),
    val currencies: List<Currency> = Constants.currencyList,
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
