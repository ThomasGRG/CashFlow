package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString

data class AccountListingDTO(
    val id: Long,
    val annotatedName: AnnotatedString,
    val currency: String,
    val balance: String,
    val icon: ImageVector,
    val chips: List<ChipInfo> = emptyList()
)
