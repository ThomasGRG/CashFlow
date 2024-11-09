package jp.ikigai.cash.flow.data.dto

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ChipInfo(
    @StringRes
    val resId: Int,
    val value: String,
    val icon: ImageVector
)
