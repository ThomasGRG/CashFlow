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

import jp.ikigai.cash.flow.data.enums.ChartType

sealed class Routes(val route: String) {

    data object Settings : Routes("settings")

    data object AuditLogs : Routes("auditLogs")

    data object Insights : Routes("insights?type={type}") {
        fun getRoute(type: ChartType = ChartType.TRANSACTION_TYPE_AMOUNT_BAR_CHART): String {
            return "insights?type=${type.name}"
        }
    }

    data object ExportTransactions : Routes("exportTransactions")
    data object ImportBackup : Routes("importBackup")

    data object Transactions : Routes("transactions")
    data object UpsertTransaction : Routes("upsertTransaction?id={id}&templateId={templateId}") {
        fun getRoute(id: Long = 0L, templateId: Long = 0L): String {
            return "upsertTransaction?id=${id}&templateId=${templateId}"
        }
    }

    data object CounterParties : Routes("counterParties")
    data object UpsertCounterParty : Routes("upsertCounterParty?id={id}") {
        fun getRoute(id: Long = 0L): String {
            return "upsertCounterParty?id=${id}"
        }
    }

    data object MigrateCounterParty : Routes("migrateCounterParty?id={id}") {
        fun getRoute(id: Long): String {
            return "migrateCounterParty?id=${id}"
        }
    }

    data object Accounts : Routes("accounts")
    data object UpsertAccount : Routes("upsertAccount?id={id}") {
        fun getRoute(id: Long = 0L): String {
            return "upsertAccount?id=${id}"
        }
    }

    data object Methods : Routes("methods")
    data object UpsertMethod : Routes("upsertMethod?id={id}") {
        fun getRoute(id: Long = 0L): String {
            return "upsertMethod?id=${id}"
        }
    }

    data object MigrateMethod : Routes("migrateMethod?id={id}") {
        fun getRoute(id: Long): String {
            return "migrateMethod?id=${id}"
        }
    }

    data object Categories : Routes("categories")
    data object UpsertCategory : Routes("upsertCategory?id={id}") {
        fun getRoute(id: Long = 0L): String {
            return "upsertCategory?id=${id}"
        }
    }

    data object MigrateCategory : Routes("migrateCategory?id={id}") {
        fun getRoute(id: Long): String {
            return "migrateCategory?id=${id}"
        }
    }

    data object Templates : Routes("templates")
    data object UpsertTemplate : Routes("upsertTemplate?id={id}") {
        fun getRoute(id: Long = 0L): String {
            return "upsertTemplate?id=${id}"
        }
    }
}