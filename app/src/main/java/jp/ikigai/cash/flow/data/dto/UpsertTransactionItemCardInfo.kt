package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.enums.ItemUnit

data class UpsertTransactionItemCardInfo(
    val item: Item,
    val unit: ItemUnit,
    val price: Double = 0.0,
    val displayPrice: String = "",
    val priceValid: Boolean = true,
    val totalPrice: Double = 0.0,
    val totalDisplayPrice: String = "",
    val quantity: Double = 0.0,
    val displayQuantity: String = "",
    val quantityValid: Boolean = true,
)
