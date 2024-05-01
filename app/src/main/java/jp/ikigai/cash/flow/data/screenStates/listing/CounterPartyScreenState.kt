package jp.ikigai.cash.flow.data.screenStates.listing

import jp.ikigai.cash.flow.data.entity.CounterParty

data class CounterPartyScreenState(
    val counterParties: List<CounterParty> = emptyList(),
    val loading: Boolean = true
)
