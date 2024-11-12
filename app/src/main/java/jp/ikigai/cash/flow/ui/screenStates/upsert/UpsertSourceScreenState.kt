package jp.ikigai.cash.flow.ui.screenStates.upsert

import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.entity.Source

data class UpsertSourceScreenState(
    val source: Source = Source(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
