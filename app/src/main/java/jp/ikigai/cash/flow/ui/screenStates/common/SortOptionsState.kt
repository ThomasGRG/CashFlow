package jp.ikigai.cash.flow.ui.screenStates.common

import io.objectbox.Property
import io.objectbox.query.QueryBuilder

data class SortOptionsState<T>(
    val sortFlags: Int = QueryBuilder.DESCENDING,
    val sortField: Property<T>
)
