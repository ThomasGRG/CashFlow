package jp.ikigai.cash.flow.ui.screenStates.common

import io.realm.kotlin.query.Sort

data class SortOptionsScreenState(
    val sortDirection: Sort = Sort.DESCENDING,
    val sortField: String = "frequency"
)
