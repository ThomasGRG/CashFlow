package jp.ikigai.cash.flow.data.dto

import android.icu.util.Currency
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import jp.ikigai.cash.flow.data.Constants

data class TransactionWithChips(
    val id: Long = 0L,
    val annotatedTitle: AnnotatedString = AnnotatedString(""),
    val annotatedDescription: AnnotatedString = AnnotatedString(""),
    val amount: String,
    val typeIcon: ImageVector = Constants.DEFAULT_TYPE_ICON,
    val typeIconColor: Color = Color(0xFFF44336),
    val currency: String = Currency.getInstance("INR").currencyCode,
    val chips: List<ChipInfo> = emptyList()
)
