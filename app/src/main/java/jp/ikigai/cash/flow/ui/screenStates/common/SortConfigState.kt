package jp.ikigai.cash.flow.ui.screenStates.common

import jp.ikigai.cash.flow.data.enums.SortDirection

data class SortConfigState(
    val sortField: String,
    val sortDirection: SortDirection = SortDirection.DESC
)