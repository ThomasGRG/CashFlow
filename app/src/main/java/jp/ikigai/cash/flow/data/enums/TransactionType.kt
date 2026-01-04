/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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