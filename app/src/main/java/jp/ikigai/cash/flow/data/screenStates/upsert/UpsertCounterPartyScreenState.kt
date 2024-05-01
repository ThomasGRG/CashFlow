package jp.ikigai.cash.flow.data.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.CounterParty

data class UpsertCounterPartyScreenState(
    val counterParty: CounterParty = CounterParty(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
