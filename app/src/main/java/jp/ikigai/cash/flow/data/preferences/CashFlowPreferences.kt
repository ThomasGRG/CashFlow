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
