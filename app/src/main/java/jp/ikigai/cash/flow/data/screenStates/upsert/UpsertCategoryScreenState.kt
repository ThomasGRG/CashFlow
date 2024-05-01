package jp.ikigai.cash.flow.data.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.Category

data class UpsertCategoryScreenState(
    val category: Category = Category(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
