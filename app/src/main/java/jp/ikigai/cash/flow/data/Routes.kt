package jp.ikigai.cash.flow.data

sealed class Routes(val route: String) {

    object Settings : Routes("settings")

    object ExportTransactions : Routes("exportTransactions")
    object ImportBackup : Routes("importBackup")

    object Transactions: Routes("transactions")
    object UpsertTransaction: Routes("upsertTransaction?id={id}&templateId={templateId}") {
        fun getRoute(id: String = "", templateId: String = ""): String {
            return "upsertTransaction?id=${id}&templateId=${templateId}"
        }
    }

    object ChooseIcon: Routes("chooseIcon?defaultIcon={defaultIcon}") {
        fun getRoute(defaultIcon: String): String {
            return "chooseIcon?defaultIcon=$defaultIcon"
        }
    }

    object CounterParties: Routes("counterParties")
    object UpsertCounterParty: Routes("upsertCounterParty?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertCounterParty?id=${id}"
        }
    }
    object MigrateCounterParty : Routes("migrateCounterParty?id={id}") {
        fun getRoute(id: String = ""): String {
            return "migrateCounterParty?id=${id}"
        }
    }

    object Sources: Routes("sources")
    object UpsertSource: Routes("upsertSource?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertSource?id=${id}"
        }
    }

    object Methods: Routes("methods")
    object UpsertMethod: Routes("upsertMethod?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertMethod?id=${id}"
        }
    }
    object MigrateMethod : Routes("migrateMethod?id={id}") {
        fun getRoute(id: String = ""): String {
            return "migrateMethod?id=${id}"
        }
    }

    object Categories: Routes("categories")
    object UpsertCategory: Routes("upsertCategory?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertCategory?id=${id}"
        }
    }
    object MigrateCategory : Routes("migrateCategory?id={id}") {
        fun getRoute(id: String = ""): String {
            return "migrateCategory?id=${id}"
        }
    }

    object Templates: Routes("templates")
    object UpsertTemplate: Routes("upsertTemplate?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertTemplate?id=${id}"
        }
    }
}