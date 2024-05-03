package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.entity.Source

data class SourceScreenState(
    val sources: List<Source> = emptyList(),
    val loading: Boolean = true
)
