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

package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import jp.ikigai.cash.flow.data.Constants

data class TemplateWithChips(
    val id: Long = 0L,
    val annotatedName: AnnotatedString = AnnotatedString(""),
    val amount: String = "",
    val typeIcon: ImageVector = Constants.DEFAULT_TYPE_ICON,
    val typeIconColor: Color = Color(0xFFF44336),
    val chips: List<ChipInfo> = emptyList()
)
