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

package jp.ikigai.cash.flow.data.preferences

import jp.ikigai.cash.flow.data.enums.SortDirection
import kotlinx.serialization.Serializable

@Serializable
data class CashFlowPreferences(
    val accountsScreenSortField: String = "transactionCount",
    val accountsScreenSortDirection: SortDirection = SortDirection.DESC,
    val categoriesScreenSortField: String = "transactionCount",
    val categoriesScreenSortDirection: SortDirection = SortDirection.DESC,
    val counterPartiesScreenSortField: String = "transactionCount",
    val counterPartiesScreenSortDirection: SortDirection = SortDirection.DESC,
    val methodsScreenSortField: String = "transactionCount",
    val methodsScreenSortDirection: SortDirection = SortDirection.DESC,
    val templatesScreenSortField: String = "transactionCount",
    val templatesScreenSortDirection: SortDirection = SortDirection.DESC,
    val transactionsScreenSortField: String = "transactionDateTime",
    val transactionsScreenSortDirection: SortDirection = SortDirection.DESC,
)
