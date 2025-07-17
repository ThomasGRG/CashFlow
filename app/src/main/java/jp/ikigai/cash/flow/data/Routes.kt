package jp.ikigai.cash.flow.data

sealed class Routes(val route: String) {

    data object Settings : Routes("settings")

    data object AuditLogs : Routes("auditLogs")

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