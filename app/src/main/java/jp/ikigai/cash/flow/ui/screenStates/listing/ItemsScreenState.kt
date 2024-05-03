package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.entity.Item

data class ItemsScreenState(
    val items: List<Item> = emptyList(),
    val loading: Boolean = true,
    val enabled: Boolean = true,
    val item: Item = Item(),
)
