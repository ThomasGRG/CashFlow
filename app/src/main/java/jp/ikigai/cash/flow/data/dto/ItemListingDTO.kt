package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.text.AnnotatedString
import jp.ikigai.cash.flow.data.enums.ItemUnit

data class ItemListingDTO(
    val uuid: String,
    val annotatedName: AnnotatedString,
    val price: String,
    val unit: ItemUnit,
    val chips: List<ChipInfo> = emptyList()
)
