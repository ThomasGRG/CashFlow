package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.entity.Category

data class CategoryScreenState(
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true
)
