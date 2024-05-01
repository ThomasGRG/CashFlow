package jp.ikigai.cash.flow.data.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.Method

data class UpsertMethodScreenState(
    val method: Method = Method(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
