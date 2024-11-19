package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import jp.ikigai.cash.flow.data.Constants

data class TransactionTemplateWithIcons(
    val uuid: String = "",
    val annotatedName: AnnotatedString = AnnotatedString(""),
    val amount: String = "",
    val typeIcon: ImageVector = Constants.DEFAULT_TYPE_ICON,
    val typeIconColor: Color = Color(0xFFF44336),
    val chips: List<ChipInfo> = emptyList()
)
