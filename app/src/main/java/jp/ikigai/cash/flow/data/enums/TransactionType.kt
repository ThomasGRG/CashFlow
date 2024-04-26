package jp.ikigai.cash.flow.data.enums

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.ArrowUpCircle

enum class TransactionType(val id: Int, val icon: ImageVector, val color: Color) {
    DEBIT(1, TablerIcons.ArrowUpCircle, Color(0xFFF44336)),
    CREDIT(2, TablerIcons.ArrowDownCircle, Color(0xFF4CAF50))
}