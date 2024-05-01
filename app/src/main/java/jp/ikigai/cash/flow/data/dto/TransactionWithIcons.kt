package jp.ikigai.cash.flow.data.dto

import android.icu.util.Currency
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import jp.ikigai.cash.flow.data.Constants

data class TransactionWithIcons(
    val uuid: String = "",
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val typeIcon: ImageVector = Constants.DEFAULT_TYPE_ICON,
    val typeIconColor: Color = Color(0xFFF44336),
    val currency: String = Currency.getInstance("INR").currencyCode,
    val itemCount: Int = 0,
    val chips: List<Pair<String, ImageVector>> = emptyList()
)
