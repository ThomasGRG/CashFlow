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

package jp.ikigai.cash.flow.data

import androidx.navigation3.runtime.NavKey
import jp.ikigai.cash.flow.data.enums.ChartType
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object AuditLogsRoute : NavKey

@Serializable
data class InsightsRoute(val type: ChartType = ChartType.TRANSACTION_TYPE_AMOUNT_BAR_CHART) : NavKey

@Serializable
data object ExportTransactionsRoute : NavKey

@Serializable
data object ImportBackupRoute : NavKey

@Serializable
data object TransactionsRoute : NavKey

@Serializable
data class UpsertTransactionRoute(val id: Long = 0L, val templateId: Long = 0L) : NavKey

@Serializable
data object CounterPartiesRoute : NavKey

@Serializable
data class UpsertCounterPartyRoute(val id: Long = 0L) : NavKey

@Serializable
data class MigrateCounterPartyRoute(val id: Long) : NavKey

@Serializable
data object AccountsRoute : NavKey

@Serializable
data class UpsertAccountRoute(val id: Long = 0L) : NavKey

@Serializable
data object MethodsRoute : NavKey

@Serializable
data class UpsertMethodRoute(val id: Long = 0L) : NavKey

@Serializable
data class MigrateMethodRoute(val id: Long) : NavKey

@Serializable
data object CategoriesRoute : NavKey

@Serializable
data class UpsertCategoryRoute(val id: Long = 0L) : NavKey

@Serializable
data class MigrateCategoryRoute(val id: Long) : NavKey

@Serializable
data object TemplatesRoute : NavKey

@Serializable
data class UpsertTemplateRoute(val id: Long = 0L) : NavKey
