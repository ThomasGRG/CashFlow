package jp.ikigai.cash.flow.data.screenStates.listing

import jp.ikigai.cash.flow.data.entity.Method

data class MethodScreenState(
    val methods: List<Method> = emptyList(),
    val loading: Boolean = true
)
