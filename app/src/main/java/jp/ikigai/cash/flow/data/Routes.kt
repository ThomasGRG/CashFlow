package jp.ikigai.cash.flow.data

sealed class Routes(val route: String) {

    object Items: Routes("items")

    object Transactions: Routes("transactions")
    object UpsertTransaction: Routes("upsertTransaction?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertTransaction?id=${id}"
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

    object Categories: Routes("categories")
    object UpsertCategory: Routes("upsertCategory?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertCategory?id=${id}"
        }
    }

    object Templates: Routes("templates")
    object UpsertTemplate: Routes("upsertTemplate?id={id}") {
        fun getRoute(id: String = ""): String {
            return "upsertTemplate?id=${id}"
        }
    }
}