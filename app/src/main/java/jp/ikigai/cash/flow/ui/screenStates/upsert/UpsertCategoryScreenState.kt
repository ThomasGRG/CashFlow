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

package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.Category
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import java.util.Locale

data class UpsertCategoryScreenState(
    val category: Category = Category(
        0L,
        "",
        TablerIcons.Archive
    ),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val selectedIcon: ImageVector = Constants.DEFAULT_CATEGORY_ICON,
    val hasUnsavedChanges: Boolean = false,
    val transactionCount: Long = 0,
    val formattedTransactionCount: String = "",
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
