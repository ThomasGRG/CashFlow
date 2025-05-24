package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString

data class CommonListingDTO(
    val id: Long,
    val annotatedName: AnnotatedString,
    val icon: ImageVector,
    val chips: List<ChipInfo> = emptyList()
)
