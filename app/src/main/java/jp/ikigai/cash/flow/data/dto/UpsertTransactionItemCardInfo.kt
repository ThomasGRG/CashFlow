package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.enums.ItemUnit

data class UpsertTransactionItemCardInfo(
    val initialItem: Item,
    val item: Item,
    val unit: ItemUnit,
    val initialUnit: ItemUnit,
    val price: Double = 0.0,
    val initialPrice: Double,
    val displayPrice: String = "",
    val priceValid: Boolean = true,
    val totalPrice: Double = 0.0,
    val totalDisplayPrice: String = "",
    val quantity: Double = 0.0,
    val initialQuantity: Double,
    val displayQuantity: String = "",
    val quantityValid: Boolean = true,
)
