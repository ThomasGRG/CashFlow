package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import jp.ikigai.cash.flow.data.Constants

data class TransactionTemplateWithIcons(
    val uuid: String = "",
    val name: String = "",
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val typeIcon: ImageVector = Constants.DEFAULT_TYPE_ICON,
    val typeIconColor: Color = Color(0xFFF44336),
    val frequency: Int = 0,
    val currency: String = "",
    val itemCount: Int = 0,
    val chips: List<Pair<String, ImageVector>> = emptyList()
)
