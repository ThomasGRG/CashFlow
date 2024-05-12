package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.ArrowUpCircle
import jp.ikigai.cash.flow.R

enum class TransactionType(
    val id: Int,
    val icon: ImageVector,
    val color: Color,
    @StringRes val label: Int
) {
    DEBIT(1, TablerIcons.ArrowUpCircle, Color(0xFFF44336), R.string.debit_label),
    CREDIT(2, TablerIcons.ArrowDownCircle, Color(0xFF4CAF50), R.string.credit_label)
}